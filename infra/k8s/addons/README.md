# Cluster add-ons

Third-party components the app manifests assume are present. They are **pinned upstream installs**,
not hand-written manifests — `install.sh` applies them in order with the versions in
`versions.env`.

| Order | Add-on | Why | Notes |
|------:|--------|-----|-------|
| 1 | **Calico** | CNI / NetworkPolicy enforcement | VXLAN mode (`CALICO_IPV4POOL_VXLAN=Always`), no BGP — only UDP 4789 between nodes, matching the `k8s-cluster` security groups. Pod CIDR `192.168.0.0/16`. |
| 2 | **metrics-server** | source for the `HorizontalPodAutoscaler`s | patched with `--kubelet-insecure-tls` (kubeadm kubelets serve a self-signed cert). |
| 3 | **ingress-nginx** | the NGINX Ingress Controller the `Ingress` objects target | bare-metal manifest, converted to a **DaemonSet** with `externalTrafficPolicy: Local`; Service NodePorts pinned to 30080 / 30443 / 30254 so the ALB target group is stable; `worker-shutdown-timeout: 300s` for in-flight SSE. |
| 4 | **aws-node-termination-handler** | cordon + drain before an ASG scale-in / rebalance / spot reclaim kills a node | a hard instance loss still drops active SSE streams — the browser `EventSource` reconnects. |
| 5 | **cluster-autoscaler** *(optional)* | the fixed worker ASG can't hold the server HPA ceiling + client + ingress DaemonSet + add-ons | `WITH_AUTOSCALER=1 CLUSTER_NAME=demo-chat-staging AWS_REGION=eu-central-1 ./install.sh`. Node-role perms are already granted in `k8s-cluster/iam.tf`; the worker ASG already carries the `k8s.io/cluster-autoscaler/*` discovery tags. If you skip it, cap the HPA `maxReplicas` to what the ASG holds. |

## Running it

Never been run (no AWS account). When there is one:

```sh
# from a bastion in the VPC, or:  aws ssm start-session --target <control-plane-instance-id>
export KUBECONFIG=/etc/kubernetes/admin.conf
infra/k8s/addons/install.sh
```

Then apply the app: `kubectl apply -f infra/k8s/manifest-staging.yaml` (the deploy workflow does
this via SSM — see `infra/k8s/README.md`).

## Node AMI prerequisites

`install.sh` handles the cluster add-ons, but the **nodes** need tooling baked into `node_ami_id`
(`infra/terraform/modules/k8s-cluster`). The user-data expects these scripts to exist on the image:

- `/opt/bootstrap/install-kube.sh <minor>` — installs containerd, `kubeadm`, `kubelet`, `kubectl`
  at the given minor version and `apt-mark hold` / `yum versionlock`s them; enables `chrony`.
- `/opt/bootstrap/install-ecr-credential-provider.sh` — installs the `ecr-credential-provider`
  binary and `--image-credential-provider-config` so kubelet can pull from ECR with the node
  instance role (no `imagePullSecret`). Bundled on EKS-optimised AMIs; absent on plain Ubuntu.

Building that AMI (Packer/EC2 Image Builder) is a `TODO` — see the `node_ami_id` gap in
`infra/terraform/README.md`.
