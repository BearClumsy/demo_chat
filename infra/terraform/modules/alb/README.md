# alb (retained reference — not instantiated)

Application Load Balancer with an **IP** target group, sized for ECS Fargate `awsvpc` tasks. The
active deploy path uses `../alb-k8s` instead — an **instance** target group that registers the
kubeadm worker ASG on the ingress-nginx NodePort and health-checks the controller's `/healthz`.

`envs/staging` and `envs/prod` call `alb-k8s`, not this module. Kept as a reviewed, lint-clean
reference alongside `../ecs-service` and `../bedrock-iam`; `tflint --recursive` still checks it.
