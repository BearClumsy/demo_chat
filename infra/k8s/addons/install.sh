#!/usr/bin/env bash
# Install the cluster add-ons, in order, against the current kubectl context.
#
# Run once after `kubeadm init` + all nodes have joined, from a host with kubectl access to the
# cluster (a bastion in the VPC, or via `aws ssm start-session` to a control-plane node with
# KUBECONFIG=/etc/kubernetes/admin.conf). NEVER been run — this cluster has no AWS account yet.
#
#   ./install.sh            # core add-ons
#   WITH_AUTOSCALER=1 ./install.sh   # also cluster-autoscaler (needs AWS_REGION + cluster name)
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${here}/versions.env"

kubectl_apply() { kubectl apply -f "$1"; }
wait_rollout()  { kubectl -n "$1" rollout status "$2" --timeout=180s; }

echo "==> 1/5 Calico CNI (${CALICO_VERSION}, VXLAN)"
kubectl_apply "https://raw.githubusercontent.com/projectcalico/calico/${CALICO_VERSION}/manifests/calico.yaml"
kubectl -n kube-system rollout status ds/calico-node --timeout=300s
# VXLAN, no BGP — matches the SG rules in modules/k8s-cluster (UDP 4789 only).
kubectl -n kube-system set env daemonset/calico-node CALICO_IPV4POOL_VXLAN=Always CALICO_IPV4POOL_IPIP=Never

echo "==> 2/5 metrics-server (${METRICS_SERVER_VERSION})"
kubectl_apply "https://github.com/kubernetes-sigs/metrics-server/releases/download/${METRICS_SERVER_VERSION}/components.yaml"
# kubeadm kubelets serve a self-signed cert; metrics-server must be told to accept it.
kubectl -n kube-system patch deploy metrics-server --type=json \
  -p '[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
wait_rollout kube-system deploy/metrics-server

echo "==> 3/5 ingress-nginx (${INGRESS_NGINX_VERSION}, bare-metal, DaemonSet, Local)"
kubectl_apply "https://raw.githubusercontent.com/kubernetes/ingress-nginx/${INGRESS_NGINX_VERSION}/deploy/static/provider/baremetal/deploy.yaml"
# The bare-metal manifest ships a Deployment + NodePort Service with random ports. Convert to a
# DaemonSet (every worker is a real ALB target), pin the NodePorts the ALB expects, preserve the
# client IP, and give in-flight SSE connections time to finish on shutdown.
kubectl -n ingress-nginx patch deploy ingress-nginx-controller \
  --type=json -p '[{"op":"replace","path":"/kind","value":"DaemonSet"}]' 2>/dev/null || {
    kubectl -n ingress-nginx get deploy ingress-nginx-controller -o yaml \
      | sed 's/^kind: Deployment$/kind: DaemonSet/' \
      | grep -v -E '^\s+(replicas|strategy|progressDeadlineSeconds):' \
      | kubectl apply -f -
    kubectl -n ingress-nginx delete deploy ingress-nginx-controller --ignore-not-found
  }
kubectl -n ingress-nginx patch svc ingress-nginx-controller --type=merge -p "$(cat <<JSON
{"spec":{"externalTrafficPolicy":"Local","ports":[
  {"name":"http","port":80,"targetPort":"http","nodePort":${INGRESS_HTTP_NODEPORT}},
  {"name":"https","port":443,"targetPort":"https","nodePort":${INGRESS_HTTPS_NODEPORT}},
  {"name":"healthz","port":10254,"targetPort":10254,"nodePort":${INGRESS_HEALTHZ_NODEPORT}}
]}}
JSON
)"
kubectl -n ingress-nginx patch configmap ingress-nginx-controller --type=merge \
  -p '{"data":{"worker-shutdown-timeout":"300s","use-forwarded-headers":"true"}}'
kubectl -n ingress-nginx rollout status ds/ingress-nginx-controller --timeout=180s

echo "==> 4/5 aws-node-termination-handler (${NODE_TERMINATION_HANDLER_VERSION})"
kubectl_apply "https://github.com/aws/aws-node-termination-handler/releases/download/v${NODE_TERMINATION_HANDLER_VERSION}/all-resources.yaml"

if [ "${WITH_AUTOSCALER:-0}" = "1" ]; then
  echo "==> 5/5 cluster-autoscaler (${CLUSTER_AUTOSCALER_VERSION})"
  : "${CLUSTER_NAME:?set CLUSTER_NAME to the name_prefix, e.g. demo-chat-staging}"
  : "${AWS_REGION:?set AWS_REGION}"
  curl -fsSL "https://raw.githubusercontent.com/kubernetes/autoscaler/cluster-autoscaler-${CLUSTER_AUTOSCALER_VERSION#v}/cluster-autoscaler/cloudprovider/aws/examples/cluster-autoscaler-autodiscover.yaml" \
    | sed "s|<YOUR CLUSTER NAME>|${CLUSTER_NAME}|g; s|--nodes=.*|--node-group-auto-discovery=asg:tag=k8s.io/cluster-autoscaler/enabled,k8s.io/cluster-autoscaler/${CLUSTER_NAME}|" \
    | kubectl apply -f -
  kubectl -n kube-system set env deploy/cluster-autoscaler AWS_REGION="${AWS_REGION}"
else
  echo "==> 5/5 cluster-autoscaler SKIPPED (set WITH_AUTOSCALER=1 to install)"
fi

echo "add-ons installed."
