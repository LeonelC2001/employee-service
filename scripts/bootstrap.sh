#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# bootstrap.sh — One-time EKS cluster setup after Terraform apply
#
# Run this ONCE after `terraform apply` to prepare the cluster.
# Prerequisites: aws cli, kubectl, helm, terraform installed locally.
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

CLUSTER_NAME=${1:-invex-eks}
AWS_REGION=${2:-us-east-1}

echo "▶ Configuring kubectl..."
aws eks update-kubeconfig --name "$CLUSTER_NAME" --region "$AWS_REGION"

echo "▶ Installing AWS Load Balancer Controller (for Ingress ALB)..."
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm upgrade --install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName="$CLUSTER_NAME" \
  --set serviceAccount.create=true \
  --wait

echo "▶ Installing metrics-server (required for HPA)..."
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

echo "▶ Creating namespaces..."
kubectl apply -f k8s/dev/manifest.yaml  --dry-run=client  # validates first
kubectl apply -f k8s/prod/manifest.yaml --dry-run=client

kubectl create namespace employee-dev  --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace employee-prod --dry-run=client -o yaml | kubectl apply -f -

echo "▶ Adding GitHub Actions IAM role annotation (update ARN below)..."
# After creating the GitHub Actions IAM user/role, add permissions:
# aws iam attach-role-policy --role-name github-actions-role \
#   --policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess
# aws iam attach-role-policy --role-name github-actions-role \
#   --policy-arn arn:aws:iam::aws:policy/AmazonEKSClusterPolicy

echo ""
echo "✅ Bootstrap complete!"
echo ""
echo "Next steps:"
echo "  1. Add these secrets to your GitHub repository Settings → Secrets:"
echo "       AWS_ACCESS_KEY_ID"
echo "       AWS_SECRET_ACCESS_KEY"
echo "       EKS_CLUSTER_NAME = ${CLUSTER_NAME}"
echo "       SONAR_TOKEN"
echo ""
echo "  2. Update ingress host in k8s/dev/manifest.yaml and k8s/prod/manifest.yaml"
echo "  3. Update ACM certificate ARN in k8s/prod/manifest.yaml"
echo "  4. Enable GitHub Environment 'production' with required reviewers"
echo ""
echo "  ECR Registry: $(terraform -chdir=scripts output -raw ecr_registry)"
echo "  RDS Endpoint: $(terraform -chdir=scripts output -raw rds_endpoint)"
