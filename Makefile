# Thin wrapper over the commands this repo already uses. Gradle is still the build system for
# :server, npm for modules/client — make just gives them one discoverable entry point, since the
# two live in different directories and the client is not part of the Gradle build.
#
# Run `make` (or `make help`) for the target list.

GRADLEW   := ./gradlew
CLIENT_DIR := modules/client
IMAGE_TAG ?= dev
APP_PORT  ?= 8080

# Prefer the `docker compose` CLI plugin, falling back to the standalone `docker-compose` binary —
# some setups (Colima without Docker Desktop, Homebrew's docker-compose) only have the latter, where
# `docker compose` fails with "unknown command". Assigned with `=`, not `:=`, so the probe only runs
# for the targets below that actually use it.
COMPOSE_BIN = $(shell docker compose version >/dev/null 2>&1 && echo docker compose || echo docker-compose)
COMPOSE = $(COMPOSE_BIN) -f modules/server/src/main/resources/local/docker-compose.yml

.DEFAULT_GOAL := help

# NOTE: recipe lines below must be indented with a TAB, not spaces.

.PHONY: help
help: ## List the available targets
	@grep -hE '^[a-zA-Z0-9_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

# ---------------------------------------------------------------------------
# Local infrastructure (Postgres, Cassandra, Qdrant, Kafka)
# ---------------------------------------------------------------------------

.PHONY: up
up: ## Start the local Postgres/Cassandra/Qdrant/Kafka containers
	$(COMPOSE) up -d

.PHONY: up-offline
up-offline: ## Start the local containers + Ollama; on first run pulls its models (~5 GB) and blocks until done
	$(COMPOSE) --profile offline up -d --remove-orphans
	@echo ">>> Ensuring the offline models are in the Ollama container (first run / post-nuke downloads ~5 GB)..."
	$(COMPOSE) exec -T ollama sh -c 'until ollama list >/dev/null 2>&1; do sleep 1; done; ollama pull llama3.1 && ollama pull nomic-embed-text'

.PHONY: down
down: ## Stop the local containers (keeps volumes)
	$(COMPOSE) --profile offline down --remove-orphans

.PHONY: logs
logs: ## Follow the local container logs
	$(COMPOSE) logs -f

.PHONY: nuke
nuke: ## Stop the local containers AND delete their data volumes (external ollama-models is kept)
	$(COMPOSE) --profile offline down -v --remove-orphans

.PHONY: colima-offline
colima-offline: ## Resize the Colima VM to 6 CPU / 16 GiB (what the offline llama3.1 needs) and restart it
	-colima stop
	colima start --cpu 6 --memory 16
	@colima list
	@echo ">>> VM resized. Next: make up-offline && make run-offline && make verify-chat"

# ---------------------------------------------------------------------------
# Database migrations (Postgres / Flyway)
# ---------------------------------------------------------------------------
#
# The app applies pending migrations on startup, so `make run` already covers the normal case. These
# targets are for applying or inspecting the schema without booting the app. Both need `make up`
# first, and both default to the local docker-compose Postgres — see the `flyway` block in
# modules/server/build.gradle for the -Pflyway.* / FLYWAY_* overrides.

.PHONY: migrate
migrate: ## Apply pending Flyway migrations to local Postgres
	$(GRADLEW) :server:flywayMigrate

.PHONY: migrate-info
migrate-info: ## Show applied/pending migration state
	$(GRADLEW) :server:flywayInfo

# ---------------------------------------------------------------------------
# Server (Gradle, :server module)
# ---------------------------------------------------------------------------
#
# `build` and `test` need a running Docker daemon — the tests start Postgres, Cassandra and Qdrant
# through Testcontainers. They do NOT need AWS credentials: application-test.properties sets
# spring.ai.model.{chat,embedding}=none and the tests supply stub Bedrock beans.
#
# If Docker is not on the default socket (e.g. Colima), export DOCKER_HOST and
# TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock first.

.PHONY: build
build: ## Build the server (compiles + runs the tests)
	$(GRADLEW) :server:build

.PHONY: run
run: ## Run the server with the `local` profile (needs `make up` first)
	$(GRADLEW) :server:bootRun

.PHONY: run-offline
run-offline: ## Run the server with no AWS — containerised Ollama for chat + embeddings (needs `make up-offline` first)
	SPRING_PROFILES_ACTIVE=local,offline $(GRADLEW) :server:bootRun

.PHONY: stop
stop: ## Stop the locally running Java app (whatever is listening on APP_PORT); containers are untouched, see `make down`
	@PID="$$(lsof -ti tcp:$(APP_PORT) -sTCP:LISTEN 2>/dev/null)"; \
	if [ -z "$$PID" ]; then \
		echo "No Java app listening on port $(APP_PORT) — nothing to stop."; \
	else \
		echo "Stopping Java app (pid(s): $$PID) on port $(APP_PORT)..."; \
		kill $$PID; \
	fi

.PHONY: ollama-pull
ollama-pull: ## Re-pull the `offline` profile models into the running Ollama container
	$(COMPOSE) exec ollama ollama pull llama3.1
	$(COMPOSE) exec ollama ollama pull nomic-embed-text

.PHONY: verify-chat
verify-chat: ## Smoke-test the RAG chat end to end against a running backend (seeded testuser)
	./scripts/verify-chat.sh

.PHONY: test
test: ## Run all server tests
	$(GRADLEW) :server:test

.PHONY: test-class
test-class: ## Run one test class or method, e.g. make test-class TEST=com.example.demo_chat.ChatServiceTest
	@test -n "$(TEST)" || { echo "usage: make test-class TEST=<fully.qualified.Class[.method]>"; exit 2; }
	$(GRADLEW) :server:test --tests "$(TEST)"

.PHONY: fmt
fmt: ## Apply Spotless / Google Java Format to the server sources
	$(GRADLEW) :server:spotlessApply

.PHONY: fmt-check
fmt-check: ## Check Spotless formatting without rewriting files
	$(GRADLEW) :server:spotlessCheck

.PHONY: clean
clean: ## Delete the Gradle build output
	$(GRADLEW) clean

.PHONY: sources
sources: ## Resolve dependency -sources / -javadoc jars into the Gradle cache
	$(GRADLEW) :server:downloadDependencySources

# ---------------------------------------------------------------------------
# Client (npm, modules/client — not wired into the Gradle build)
# ---------------------------------------------------------------------------

.PHONY: client-install
client-install: ## Install client dependencies from the lockfile
	cd $(CLIENT_DIR) && npm ci

.PHONY: client-dev
client-dev: ## Run the Vite dev server (proxies /api to localhost:8080)
	cd $(CLIENT_DIR) && npm run dev

.PHONY: client-lint
client-lint: ## Lint the client with ESLint
	cd $(CLIENT_DIR) && npm run lint

.PHONY: client-build
client-build: ## Type-check and bundle the client (tsc -b && vite build)
	cd $(CLIENT_DIR) && npm run build

.PHONY: client-preview
client-preview: ## Serve the production client build locally
	cd $(CLIENT_DIR) && npm run preview

# ---------------------------------------------------------------------------
# Knowledge base
# ---------------------------------------------------------------------------

.PHONY: validate-intents
validate-intents: ## Validate the intent JSON files the way CI does
	node scripts/validate-intents.mjs

# ---------------------------------------------------------------------------
# Container images
# ---------------------------------------------------------------------------

.PHONY: docker-server
docker-server: ## Build the server image (build context is the repo root)
	docker build -f modules/server/Dockerfile -t demo-chat-server:$(IMAGE_TAG) .

.PHONY: docker-client
docker-client: ## Build the client image (nginx + static build)
	docker build -t demo-chat-client:$(IMAGE_TAG) $(CLIENT_DIR)

.PHONY: docker
docker: docker-server docker-client ## Build both container images

# ---------------------------------------------------------------------------
# Infrastructure (Terraform, infra/terraform — lint only, never applied)
# ---------------------------------------------------------------------------
#
# There is no AWS account for this project yet, so the Terraform is CI-linted but not applied.
# Needs `terraform` (or `tofu`) and `tflint` on PATH. Mirrors the terraform-lint GitHub workflow.
# No AWS credentials are used: fmt/validate/tflint never call AWS and init runs with -backend=false.

TF_DIR := infra/terraform
TF_BIN = $(shell command -v terraform >/dev/null 2>&1 && echo terraform || echo tofu)

.PHONY: tf-lint
tf-lint: ## Lint the Terraform (fmt check + validate staging/prod + tflint)
	$(TF_BIN) fmt -check -recursive $(TF_DIR)
	$(TF_BIN) -chdir=$(TF_DIR)/envs/staging init -backend=false -input=false
	$(TF_BIN) -chdir=$(TF_DIR)/envs/staging validate
	$(TF_BIN) -chdir=$(TF_DIR)/envs/prod init -backend=false -input=false
	$(TF_BIN) -chdir=$(TF_DIR)/envs/prod validate
	cd $(TF_DIR) && tflint --init && tflint --recursive

.PHONY: tf-fmt
tf-fmt: ## Rewrite the Terraform files to canonical format
	$(TF_BIN) fmt -recursive $(TF_DIR)

# ---------------------------------------------------------------------------
# Kubernetes (infra/k8s — lint only, never applied; see docs/wiki/Plan/kubernetes.md)
# ---------------------------------------------------------------------------
#
# Needs `kubeconform`, `kubectl` and `shellcheck` on PATH. Mirrors the manifests-lint workflow.
# No cluster and no AWS credentials are involved.

K8S_DIR   := infra/k8s
ECR_REGISTRY ?= REGISTRY_UNSET

.PHONY: k8s-lint
k8s-lint: ## Lint the k8s manifests (kubeconform + kubectl dry-run + shellcheck)
	kubeconform -strict -ignore-missing-schemas -summary $(K8S_DIR)/manifest-staging.yaml $(K8S_DIR)/manifest-prod.yaml
	kubectl apply --dry-run=client -f $(K8S_DIR)/manifest-staging.yaml
	kubectl apply --dry-run=client -f $(K8S_DIR)/manifest-prod.yaml
	shellcheck $(K8S_DIR)/addons/install.sh

.PHONY: k8s-render-staging
k8s-render-staging: ## Substitute the image placeholders with dummies and re-check the YAML
	sed -e 's#IMAGE_PLACEHOLDER_SERVER#demo-chat-server:dev#' -e 's#IMAGE_PLACEHOLDER_CLIENT#demo-chat-client:dev#' \
		$(K8S_DIR)/manifest-staging.yaml | kubeconform -strict -ignore-missing-schemas -summary -

.PHONY: k8s-render-prod
k8s-render-prod: ## Substitute the image placeholders with dummies and re-check the YAML
	sed -e 's#IMAGE_PLACEHOLDER_SERVER#demo-chat-server:dev#' -e 's#IMAGE_PLACEHOLDER_CLIENT#demo-chat-client:dev#' \
		$(K8S_DIR)/manifest-prod.yaml | kubeconform -strict -ignore-missing-schemas -summary -

.PHONY: k8s-addons
k8s-addons: ## Install the cluster add-ons (Calico, metrics-server, ingress-nginx, NTH) — needs kube context
	$(K8S_DIR)/addons/install.sh

.PHONY: k8s-apply-staging
k8s-apply-staging: ## BREAK-GLASS: kubectl apply the staging manifest against the current context
	kubectl apply -f $(K8S_DIR)/manifest-staging.yaml

.PHONY: k8s-apply-prod
k8s-apply-prod: ## BREAK-GLASS: kubectl apply the prod manifest against the current context
	kubectl apply -f $(K8S_DIR)/manifest-prod.yaml

.PHONY: docker-push
docker-push: docker ## Tag both images with $(IMAGE_TAG) and push to $(ECR_REGISTRY)
	docker tag demo-chat-server:$(IMAGE_TAG) $(ECR_REGISTRY)/demo-chat-server:$(IMAGE_TAG)
	docker tag demo-chat-client:$(IMAGE_TAG) $(ECR_REGISTRY)/demo-chat-client:$(IMAGE_TAG)
	docker push $(ECR_REGISTRY)/demo-chat-server:$(IMAGE_TAG)
	docker push $(ECR_REGISTRY)/demo-chat-client:$(IMAGE_TAG)

# ---------------------------------------------------------------------------
# Aggregate
# ---------------------------------------------------------------------------

# Mirrors the checks in backend-ci / frontend-ci / knowledge-base-lint. The image builds those
# workflows also do are left out because they are slow locally — run `make ci docker` for the lot.
.PHONY: ci
ci: fmt-check build client-lint client-build validate-intents ## Run everything CI checks (no image builds)
