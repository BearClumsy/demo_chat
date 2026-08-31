# CI/CD: GitHub Actions

[← Back to README](README.md) · [AWS infrastructure](infrastructure.md)

**Status:** partially implemented. Four CI/lint workflows exist and run; the deploy workflows do not,
because they need an AWS account and an ECR repository that don't exist yet (Phase 3b in
[roadmap.md](roadmap.md)). `terraform-lint` is the AWS-credential-free half of the Terraform work —
it lints the `infra/terraform/` skeleton but nothing is planned or applied.

## Workflow file structure

```
.github/
└── workflows/
    ├── backend-ci.yml           # IMPLEMENTED - spotlessCheck, tests, server image build
    ├── frontend-ci.yml          # IMPLEMENTED - eslint, tsc/vite build, client image build
    ├── knowledge-base-lint.yml  # IMPLEMENTED - scripts/validate-intents.mjs
    ├── terraform-lint.yml       # IMPLEMENTED - fmt -check, validate (staging+prod), tflint; no AWS creds
    ├── deploy-staging.yml       # planned - deploy to ECS staging
    └── deploy-prod.yml          # planned - deploy to ECS prod on a release tag
```

There is no separate `pr-checks.yml`: formatting and static analysis run inside `backend-ci` /
`frontend-ci` rather than in a fourth workflow that would repeat their setup. All three trigger on
`pull_request` and on `push` to `main`, path-filtered to their own module (`develop` doesn't exist as a
branch in this repo).

## `backend-ci.yml` — stages (implemented)

```
trigger: pull_request, push to main (paths: modules/server/**, gradle/**, settings.gradle, gradlew)

1. checkout
2. setup-java (temurin 26) + gradle/actions/setup-gradle for dependency caching
3. ./gradlew :server:spotlessCheck :server:build
   - spotlessCheck IS the static analysis here (googleJavaFormat); no Checkstyle/SpotBugs
   - build runs all 8 test classes, including the Testcontainers ones
4. upload the test report as an artifact on failure
5. docker build of modules/server/Dockerfile (no push - no ECR yet)
```

Testcontainers uses the runner's Docker for Postgres (`user/UserRepositoryTest`) and for Postgres +
Cassandra + Qdrant (`DemoChatApplicationTests`). Bedrock is stubbed in the tests, so the job needs no
AWS credentials. Still to add in Phase 3b: ECR push (tag = git sha) and a Trivy scan of the image.

## `frontend-ci.yml` — stages (implemented)

```
trigger: pull_request, push to main (path: modules/client/**)

1. checkout
2. setup-node (22, npm cache keyed on modules/client/package-lock.json)
3. npm ci
4. npm run lint (ESLint 9 flat config: typescript-eslint + react-hooks + react-refresh)
5. npm run build - `tsc -b && vite build`, so this type-checks as well as bundles
6. docker build of modules/client/Dockerfile (no push - no ECR yet)
```

No unit-test step: there is still no test runner configured for the client.

## `knowledge-base-lint.yml` (implemented)

```
trigger: pull_request, push to main
         (paths: modules/server/src/main/resources/knowledge-base/**, scripts/validate-intents.mjs)

1. checkout
2. setup-node
3. node scripts/validate-intents.mjs
```

The validator is dependency-free Node, and its rules mirror the `IntentDefinition` record so a file
that passes also loads at startup:

1. required fields — `intent_id`, `canonical_questions` (non-empty), `required_slots`,
   `knowledge_snippet`, `system_instruction`, `allowed`, `answer_template`, `escalation_fallback`
   (earlier drafts of this doc listed a shorter, outdated set)
2. no unknown fields — the record would silently ignore them
3. `intent_id` unique across files and equal to the filename stem
4. no duplicate canonical questions across intents
5. every `{placeholder}` in `answer_template` is listed in `required_slots`

A separate workflow, because the knowledge base changes more frequently
and by different people (content managers/support staff) than the code —
it needs a lightweight gate without a full application build.

## `terraform-lint.yml` (implemented)

```
trigger: pull_request, push to main
         (paths: infra/**, .github/workflows/terraform-lint.yml)

1. checkout
2. hashicorp/setup-terraform (pinned)
3. terraform fmt -check -recursive infra/terraform
4. terraform -chdir=infra/terraform/envs/staging init -backend=false && ... validate
5. terraform -chdir=infra/terraform/envs/prod    init -backend=false && ... validate
6. terraform-linters/setup-tflint (pinned) → tflint --init && tflint --recursive
```

The AWS-credential-free half of Phase 3b's Terraform item. **No AWS credentials are configured** in
the job: `fmt`, `validate` and `tflint` never call AWS, and `init -backend=false` skips the S3
state backend. Nothing is planned or applied. Own lightweight lane, path-filtered to `infra/**`, so
it does not drag in a Gradle build — same rationale as `knowledge-base-lint`. The eventual
`deploy-staging` / `deploy-prod` workflows are what will actually run `terraform plan`/`apply`
(under OIDC, against a real account).

## `deploy-staging.yml`

```
trigger: push to develop (after backend-ci/frontend-ci succeed)

1. assume AWS IAM role (OIDC, no long-lived secrets)
2. terraform plan (envs/staging) — for review
3. terraform apply (envs/staging) — automatic
4. ECS: update-service --force-new-deployment (backend)
5. reindex knowledge-base → Qdrant staging
   (job calls QdrantDocumentLoader.reindex() through an admin endpoint)
6. smoke tests against the staging URL (key intent scenarios)
7. Slack notification with the result
```

## `deploy-prod.yml`

```
trigger: git tag vX.Y.Z

1. assume AWS IAM role (OIDC)
2. terraform plan (envs/prod) — requires manual approval (GitHub Environments)
3. terraform apply (envs/prod)
4. ECS blue/green deployment (CodeDeploy or native ECS)
5. reindex knowledge-base → Qdrant prod (after the application deploy succeeds)
6. health check + rollback trigger on failure
7. Slack notification + release changelog
```

## GitHub Environments and protections

| Environment | Reviewers required | Secrets |
|---|---|---|
| `staging` | no | `AWS_ROLE_STAGING`, `ECR_REPO_STAGING` |
| `production` | yes (min 1 approver) | `AWS_ROLE_PROD`, `ECR_REPO_PROD` |

Secrets are limited to the AWS IAM Role ARN used for OIDC authentication
(GitHub → AWS with no static keys); no AWS credentials are stored on the runner.

## Related documents

- [AWS infrastructure](infrastructure.md)
- [Vector Store schema](vector-store-schema.md)
