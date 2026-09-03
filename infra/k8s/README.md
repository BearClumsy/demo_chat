# infra/k8s

Kubernetes manifests for running `demo_chat` on the **self-managed kubeadm cluster on EC2**
provisioned by `infra/terraform/modules/k8s-cluster`. See `docs/wiki/Plan/kubernetes.md` for the
architecture.

## Status: lint-only

No AWS account, no cluster — these manifests are **written and CI-linted, never applied**
(`manifests-lint` workflow: `kubeconform` + `kubectl --dry-run=client` + `shellcheck` +
`actionlint`). Same posture as `infra/terraform/`.

## Layout

```
infra/k8s/
├── manifest-staging.yaml   the whole app for staging (one multi-doc file)
├── manifest-prod.yaml      the whole app for prod — deliberately duplicated, diverges only in
│                           replica counts, HPA bounds, resource requests and the Ingress host
└── addons/
    ├── versions.env        pinned versions for the third-party add-ons
    ├── install.sh          installs Calico, metrics-server, ingress-nginx, NTH, (opt) autoscaler
    └── README.md           add-on details + node-AMI prerequisites
```

## What's in a manifest

| Object | Notes |
|--------|-------|
| `Namespace demo-chat` | PodSecurity `enforce: restricted`. Both images run as non-root (server uid 10001, client `nginx-unprivileged` uid 101). |
| `ConfigMap demo-chat-config` | non-secret env — keys mirror `infra/terraform/envs/<env>` `container_env` and `application-<env>.properties`. `REPLACE_*` values are patched at deploy time from the Terraform outputs. |
| `Secret demo-chat-secrets` | **placeholders committed.** The deploy workflow overwrites it with real values from AWS Secrets Manager. Keys: `POSTGRES_PASSWORD`, `CASSANDRA_USER`, `CASSANDRA_PASSWORD`, `QDRANT_API_KEY` (`CASSANDRA_USER` closes the gap noted in `docs/wiki/Daily/2026-08-31.md`). |
| `Deployment demo-chat-server` | `startupProbe` → `/actuator/health/readiness` (~300 s budget); `livenessProbe` → `/actuator/health/liveness` (no restart storm on a downstream outage); `readinessProbe` → `/actuator/health/readiness`. `terminationGracePeriodSeconds: 300` for in-flight SSE. Image `IMAGE_PLACEHOLDER_SERVER`. |
| `Deployment demo-chat-client` | nginx on 8080, probe `GET /healthz`. Image `IMAGE_PLACEHOLDER_CLIENT`. |
| `Ingress demo-chat-web` / `demo-chat-api` | two objects sharing the host: `/` → client, `/api` → server. SSE annotations (`proxy-buffering off`, `proxy-read-timeout 300`, …) are on the API one only. `ssl-redirect: "false"` — the ALB already did 80→443. |
| `HorizontalPodAutoscaler` ×2 | CPU 70%. server 2→6 (staging) / 3→10 (prod); client 2→4. Needs metrics-server. |
| `PodDisruptionBudget` ×2, `NetworkPolicy` ×3 | default-deny ingress, allow from `ingress-nginx` ns, allow-list egress (DNS, data-tier VPC CIDR, 443). |
| `Job demo-chat-kb-bootstrap` | one-shot: runs the server image with `--reindex-and-exit` to seed Qdrant `support_kb` (staging/prod have `reindex-on-startup=false`). `activeDeadlineSeconds: 600`, `ttlSecondsAfterFinished: 1d`. |

## Deploy flow (what the workflow does)

1. OIDC-assume the `github_deploy_role_arn`; `docker build` + push both images to ECR
   (`:<git-sha>` staging, `:<release-tag>` prod).
2. `sed` `IMAGE_PLACEHOLDER_SERVER` / `IMAGE_PLACEHOLDER_CLIENT` and the `REPLACE_*` ConfigMap
   values (from `terraform output`) in the env manifest.
3. `aws secretsmanager get-secret-value` for the 4 secrets → render `demo-chat-secrets` with
   `kubectl create secret generic --dry-run=client -o yaml` into `secret.yaml`.
4. Upload `manifest.yaml` + `secret.yaml` to `s3://<k8s_deploy_bucket>/deploy/<sha>/`.
5. `aws ssm send-command` with the `<name>-kubectl-apply` document, targeting one control-plane
   node (tag `k8s-role=control-plane`). The document `aws s3 cp`s the prefix, `flock`s, `kubectl
   apply`s both files, and waits on `rollout status`.
6. Apply the bootstrap Job, `kubectl wait --for=condition=complete`, then smoke-test
   (`/actuator/health`, one intent turn, an SSE `curl -N`).

CI never opens a connection to kube-apiserver — there is no inbound 6443 from the internet.

## Local check

```sh
make k8s-lint      # kubeconform + kubectl --dry-run=client + shellcheck (mirrors CI)
make k8s-render-staging   # sed placeholders with dummies, re-run kubeconform on the output
```

`kubectl apply -f infra/k8s/manifest-staging.yaml` by hand is the documented **break-glass** path
(`make k8s-apply-staging`) — normal deploys go through the workflow.
