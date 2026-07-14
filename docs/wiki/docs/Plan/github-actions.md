# CI/CD: GitHub Actions

[← Back to README](README.md) · [AWS infrastructure](infrastructure.md)

## Workflow file structure

```
.github/
└── workflows/
    ├── backend-ci.yml           # tests + build the backend image
    ├── frontend-ci.yml          # tests + build the frontend image
    ├── knowledge-base-lint.yml  # validates the JSON schema of intents/*.json
    ├── deploy-staging.yml       # deploy to ECS staging on merge to develop
    ├── deploy-prod.yml          # deploy to ECS prod on a release tag
    └── pr-checks.yml            # lint, static analysis on every PR
```

## `backend-ci.yml` — stages

```
trigger: pull_request, push to main/develop (path: backend/**)

1. checkout
2. setup-java (temurin, version from build.gradle.kts)
3. gradle build --info
4. unit tests (service/*, prompt builder, scope filter)
5. integration tests (Testcontainers: Qdrant + Cassandra — no Redis in this project)
6. static analysis (Checkstyle/SpotBugs)
7. build Docker image → push to Amazon ECR (tag = git sha)
8. Trivy scan of the image for vulnerabilities
```

## `frontend-ci.yml` — stages

```
trigger: pull_request, push to main/develop (path: frontend/**)

1. checkout
2. setup-node
3. npm ci
4. eslint + typecheck
5. unit tests (vitest/jest)
6. build (vite build)
7. build Docker image (nginx + static) → push to ECR
```

## `knowledge-base-lint.yml`

```
trigger: pull_request (path: backend/src/main/resources/knowledge-base/**)

1. checkout
2. validate JSON schema of each intent file
   (required fields: intent_id, canonical_questions, required_slots,
   knowledge_snippet, allowed)
3. check intent_id uniqueness
4. check for duplicate canonical_questions across topics
```

A separate workflow, because the knowledge base changes more frequently
and by different people (content managers/support staff) than the code —
it needs a lightweight gate without a full application build.

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
- [Vector Store schema](../data/vector-store-schema.md)
