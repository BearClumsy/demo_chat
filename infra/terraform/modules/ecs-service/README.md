# ecs-service (retained reference — not instantiated)

This module models the app on **ECS Fargate**. The project's deploy path is now **self-managed
Kubernetes (kubeadm) on EC2** — see `infra/terraform/modules/k8s-cluster/`, `infra/k8s/`, and
`docs/wiki/Plan/kubernetes.md`.

Neither `envs/staging` nor `envs/prod` calls this module any more. It is kept as a reviewed,
lint-clean reference (`tflint --recursive` still checks it) so the ECS option stays documented and
recoverable. Do not delete it or strip its variables — an emptied module trips
`terraform_unused_declarations`.

Paired with `../alb` (`target_type = "ip"`) and `../bedrock-iam` (ECS task/execution roles), which
are retained on the same basis.
