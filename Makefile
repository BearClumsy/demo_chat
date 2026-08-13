# Thin wrapper over the commands this repo already uses. Gradle is still the build system for
# :server, npm for modules/client — make just gives them one discoverable entry point, since the
# two live in different directories and the client is not part of the Gradle build.
#
# Run `make` (or `make help`) for the target list.

GRADLEW   := ./gradlew
CLIENT_DIR := modules/client
IMAGE_TAG ?= dev

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
	@grep -hE '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

# ---------------------------------------------------------------------------
# Local infrastructure (Postgres, Cassandra, Qdrant, Kafka)
# ---------------------------------------------------------------------------

.PHONY: up
up: ## Start the local Postgres/Cassandra/Qdrant/Kafka containers
	$(COMPOSE) up -d

.PHONY: down
down: ## Stop the local containers (keeps volumes)
	$(COMPOSE) down

.PHONY: logs
logs: ## Follow the local container logs
	$(COMPOSE) logs -f

.PHONY: nuke
nuke: ## Stop the local containers AND delete their data volumes
	$(COMPOSE) down -v

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
# Aggregate
# ---------------------------------------------------------------------------

# Mirrors the checks in backend-ci / frontend-ci / knowledge-base-lint. The image builds those
# workflows also do are left out because they are slow locally — run `make ci docker` for the lot.
.PHONY: ci
ci: fmt-check build client-lint client-build validate-intents ## Run everything CI checks (no image builds)
