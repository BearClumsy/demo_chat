# bedrock-iam (retained reference — not instantiated)

ECS **task role** (`bedrock:InvokeModel`) + **execution role** (image pull + task-secret read).
Both are ECS-shaped (`assume_role_policy` principal `ecs-tasks.amazonaws.com`).

Under the Kubernetes deploy path there is no ECS task: Bedrock invoke permission is granted to the
kubeadm **worker node instance role** directly, inside `../k8s-cluster` (`iam.tf`, statement
`InvokeBedrockModels`), because kubeadm has no IRSA. So `envs/*` no longer call this module.

Kept as a reviewed, lint-clean reference alongside `../ecs-service` and `../alb`.
