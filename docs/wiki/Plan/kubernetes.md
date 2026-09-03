# Kubernetes deploy layer (kubeadm on EC2)

[← Back to README](README.md) · [AWS infrastructure](infrastructure.md) · [CI/CD](github-actions.md)

**Status:** lint-only skeleton (2026-09-03). No AWS account, no cluster. The Terraform
(`infra/terraform/modules/{k8s-cluster,alb-k8s,ecr,github-oidc}`) and the manifests
(`infra/k8s/manifest-{staging,prod}.yaml`, `infra/k8s/addons/`) are written and CI-linted
(`terraform-lint`, `manifests-lint`) but never applied. Same posture as the rest of Phase 3b.

## Why kubeadm, not ECS or EKS

The original target ([infrastructure.md](infrastructure.md), [local-vs-aws.md](local-vs-aws.md))
was **ECS Fargate behind an ALB**. The deploy target is now **self-managed Kubernetes via kubeadm
on EC2**: containers run as Deployments on EC2 worker nodes, orchestrated by a kubeadm control
plane, fronted by the community **NGINX Ingress Controller**. No EKS — the control plane runs on
EC2 too, and Bedrock credentials reach pods through the **worker node instance profile** (kubeadm
has no IRSA).

The ECS modules (`ecs-service`, `alb` with its `ip` target group, `bedrock-iam` with ECS
task/exec roles) are **retained as lint-clean references** — each has a README saying so — but no
env instantiates them.

## Shape

```
Internet
  │  HTTPS :443 (ACM cert on the ALB)   ── :80 → 301 redirect
  ▼
ALB  (alb-k8s: internet-facing, public subnets, target_type=instance,
      idle_timeout=300, deregistration_delay=300 — SSE-safe)
  │  → worker ASG instances, NodePort 30080     health check → NodePort 30254 (ingress-nginx /healthz)
  ▼
ingress-nginx  (DaemonSet, externalTrafficPolicy: Local, HTTP-only, ssl-redirect off)
  │  Ingress demo-chat-web   host chat.<env>…   /      → Service demo-chat-client :8080
  │  Ingress demo-chat-api   host chat.<env>…   /api   → Service demo-chat-server :8080
  │        (SSE annotations on demo-chat-api only: proxy-buffering/request-buffering off,
  │         proxy-read/send-timeout 300, proxy-http-version 1.1)
  ▼
ns demo-chat  (private app subnets, on worker EC2 nodes)
  demo-chat-server  ×2/3   envFrom ConfigMap + Secret    probes → /actuator/health/{liveness,readiness}
  demo-chat-client  ×2     nginx-unprivileged :8080
  Job demo-chat-kb-bootstrap   server image, --reindex-and-exit → seeds Qdrant support_kb
  │
  └── egress → RDS Postgres · Amazon Keyspaces · Qdrant-on-EC2 · MSK · Bedrock  (data tier unchanged)

Control plane: 1 (staging) / 3 (prod) EC2 in an ASG, stacked etcd on a dedicated gp3 volume,
  --control-plane-endpoint = internal API NLB DNS.
CI → cluster: GitHub OIDC role → build/push ECR → `aws ssm send-command` to a control-plane node
  (tag k8s-role=control-plane) runs `kubectl apply` from an S3-staged rendered manifest.
  No inbound 6443 from the internet.
```

## Cluster bootstrap (`modules/k8s-cluster`)

- **Seed election without a designated node** — control-plane and worker user-data race on
  `aws ssm put-parameter --name /demo-chat/<env>/k8s/init-lock` **without `--overwrite`**. The one
  winner runs `kubeadm init --upload-certs` against the internal API NLB DNS and publishes
  `worker-join-command` / `controlplane-join-command` to SSM SecureString (KMS-encrypted). Everyone
  else bounded-polls SSM and `kubeadm join`. Staging (1 CP) skips the lock.
- **Worker join race** — an ASG `EC2_INSTANCE_LAUNCHING` lifecycle hook holds the node in
  `Pending:Wait`; the worker user-data calls `complete-lifecycle-action --continue` / `--abandon`,
  so a half-joined node never enters the ingress target group.
- **Token / cert expiry** — bootstrap tokens last 24 h and `--certificate-key` 2 h, so a node
  replaced by the ASG weeks later can't join. A **systemd timer on every control-plane node**
  refreshes both join commands into SSM every 6 h; a second timer ships `etcdctl snapshot save` to
  S3.
- **Internal API NLB** — `aws_lb` (network, internal), HTTPS `/readyz` health check, cross-zone on.
  Its DNS is the stable `--control-plane-endpoint` baked into every kubeconfig.
- **CNI** — Calico in VXLAN mode (`CALICO_IPV4POOL_VXLAN=Always`), no BGP; only UDP 4789 between
  nodes. Pod CIDR `192.168.0.0/16` (no VPC overlap).
- **Secrets at rest** — kubeadm `EncryptionConfiguration` (aescbc); the `demo-chat-secrets` object
  is encrypted in etcd on the EC2 volume.
- **ECR pull** — the worker node role gets read-only ECR, so no `imagePullSecrets`. On a non-EKS
  AMI the user-data also installs the `ecr-credential-provider` binary
  (`/opt/bootstrap/install-ecr-credential-provider.sh`, baked into `node_ami_id`).
- **SSM VPC endpoints** (`ssm`, `ssmmessages`, `ec2messages`) so deploys work even if NAT egress
  is degraded.
- **Staging single-CP is deliberate** (disposable env). The data plane survives a CP outage; a
  naive ASG replacement gives an empty cluster → recovery runbook = new CP, `kubeadm init`
  (optionally restore an etcd snapshot), `make k8s-addons`, CI re-applies. The staging CP ASG sets
  `suspended_processes = ["AZRebalance","ReplaceUnhealthy"]`. Prod runs 3 CP; CP replacement is a
  manual `etcdctl member remove` runbook and instance-refresh must never be enabled on that ASG.

## Ingress / ALB (`modules/alb-k8s`)

Separate from `modules/alb` because the divergence is large: `target_type = "instance"`, target
port 30080 (`aws_autoscaling_group.target_group_arns`), health check on NodePort 30254 →
ingress-nginx `/healthz`, `deregistration_delay = 300` (= the app's SSE stream ceiling). Keeps the
80→443 redirect, ACM cert, and `idle_timeout = 300`. TLS terminates at the ALB; ingress-nginx runs
HTTP-only with `ssl-redirect: "false"`.

ingress-nginx runs as a **DaemonSet** with `externalTrafficPolicy: Local` — every worker is a real
ALB target, the client IP is preserved, and the `/healthz` NodePort check doubles as
"controller-ready-on-this-node". `aws-node-termination-handler` cordons + drains on ASG scale-in /
rebalance / spot before the instance dies; a hard instance loss still drops active SSE streams (the
browser `EventSource` reconnects).

## Manifests (`infra/k8s/manifest-{staging,prod}.yaml`)

One consolidated multi-doc file per env, **deliberately duplicated** (same rationale as
`application-{staging,prod}.properties` and `envs/{staging,prod}`). Divergences: replica counts,
HPA bounds, resource requests, the Ingress `host`.

- **`demo-chat` namespace** — PodSecurity `enforce: restricted`. The client image switched to
  `nginxinc/nginx-unprivileged` (uid 101, listens 8080) and the server Dockerfile pins uid 10001,
  so `restricted` is satisfiable.
- **ConfigMap `demo-chat-config`** — non-secret env, keys 1:1 with the Terraform `container_env`
  output and `application-<env>.properties`. `REPLACE_*` values substituted at deploy time.
- **Secret `demo-chat-secrets`** — `POSTGRES_PASSWORD`, **`CASSANDRA_USER`**, `CASSANDRA_PASSWORD`,
  `QDRANT_API_KEY`. `CASSANDRA_USER` closes the gap flagged in [[2026-08-31]] (Amazon Keyspaces
  issues a username+password pair). Ships with `REPLACE_AT_DEPLOY` placeholders on purpose — a
  deploy that skips the secret-render step then fails fast (the app has no defaults for these).
- **Probes** — `startupProbe` → `/actuator/health/readiness`, `failureThreshold: 30` (~300 s
  budget: the k8s equivalent of the `health_check_grace_period_seconds` fix that resolved the
  [[2026-08-31]] "ALB killed the task before readiness" incident). `livenessProbe` →
  `/actuator/health/liveness` (only `livenessState`; stays UP during a Cassandra/Qdrant/Bedrock
  outage → no restart storm). `readinessProbe` → `/actuator/health/readiness`.
- **KB bootstrap Job** — staging/prod keep `reindex-on-startup=false` and
  `QDRANT_INITIALIZE_SCHEMA=false`, so a fresh Qdrant `support_kb` is empty and every RAG turn
  escalates. `KnowledgeBaseIndexer` gained a `--reindex-and-exit` one-shot mode; the Job runs the
  server image with that arg, seeds the collection, and exits 0.

## CI → cluster (SSM Run Command, no inbound exposure)

Not an internet-facing API NLB (kube-apiserver CVEs), not a self-hosted runner (overkill for two
Deployments). A **custom `aws_ssm_document`** (`<name>-kubectl-apply`, plus `<name>-kubectl-rollback`
for prod) does only `aws s3 cp` + `flock` + `kubectl apply` + `rollout status`. The deploy role's
`ssm:SendCommand` is scoped to those document ARNs and to instances tagged
`k8s-role=control-plane`. Output goes to S3 (inline SSM output truncates at ~2.5 KB); the workflow
polls `get-command-invocation` and fails on non-zero.

- **`deploy-staging.yml`** — push to `main`; OIDC → build/push `:<git-sha>` to ECR (Trivy
  non-blocking) → render manifest + Secret → S3 → SSM apply → `rollout status` → smoke test.
- **`deploy-prod.yml`** — tag `v*`, `environment: production` (manual approval); `:<tag>`; Trivy
  blocking on CRITICAL; runs `<name>-kubectl-rollback` via SSM if the smoke test fails.

Both reference GitHub Environment `vars.*` (`AWS_DEPLOY_ROLE_ARN`, `ECR_*`, `K8S_DEPLOY_BUCKET`,
`SSM_*`, the four `SECRET_ARN_*`, `RDS_ENDPOINT` / `QDRANT_HOST` / `KAFKA_BOOTSTRAP_SERVERS`,
`CHAT_HOSTNAME`) that only exist once Terraform is applied — see the workflow header comments.

## Scaling

- **HPA** (CPU 70%) — server 2→6 (staging) / 3→10 (prod), client 2→4.
- **cluster-autoscaler** — the fixed worker ASG can't hold the server HPA ceiling + client +
  ingress DaemonSet + add-ons. Node-role perms are already granted (`k8s-cluster/iam.tf`) and the
  worker ASG carries the `k8s.io/cluster-autoscaler/*` discovery tags; installing it is the one
  optional step in `make k8s-addons` (`WITH_AUTOSCALER=1`). If skipped, cap the HPA `maxReplicas`
  to what the ASG holds.
- Roadmap Phase 4's "auto-scaling by latency/RPS" would layer custom/external metrics on top.

## Still deferred (needs an AWS account)

`terraform apply`; the S3 state backend + lock table; real `node_ami_id` (an Ubuntu/AL2023 image
with containerd + kubeadm + `ecr-credential-provider` baked in — a Packer/Image Builder TODO),
`acm_certificate_arn`, secret ARNs, `github_org`, `admin_cidr`; the actual `kubeadm init` and
`make k8s-addons`; DNS for the ALB hostname; the full Keyspaces schemas; and any end-to-end smoke
test.
