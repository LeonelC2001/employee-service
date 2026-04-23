# employee-service — Infrastructure & DevSecOps Guide

## Arquitectura General

```
GitHub Actions CI/CD
        │
        ├─ build → test → sonar → docker build → push ECR
        │
        ├─ [develop branch] ──auto──▶ EKS Namespace: employee-dev (H2, 1 replica)
        │
        └─ [main branch] ──approval──▶ EKS Namespace: employee-prod (RDS MySQL, 2–8 replicas + HPA)
```

---

## Estructura del Repositorio

```
employee-service/              
docker/
  Dockerfile                    ← multi-stage build (builder + runtime)
  docker-compose.yml            ← para desarrollo local
k8s/
  dev/
    manifest.yaml               ← Namespace, ConfigMap, Secret, Deployment, Service, Ingress
  prod/
    manifest.yaml               ← igual + HPA, PDB, topologySpread, HTTPS, WAF
scripts/
  main.tf                       ← Terraform: VPC, EKS, RDS MySQL, ECR, Secrets Manager
  bootstrap.sh                  ← setup one-time del cluster
.github/
  workflows/
    ci-cd.yml                   ← Pipeline completo CI/CD
```

---

## Flujo Git Flow

```
feature/* ──▶ develop ──▶ release/* ──▶ main
                │                          │
          deploy DEV (auto)         deploy PROD (manual approval)
```

---

## 1. Desarrollo Local (Docker Compose)

```bash
# Copiar el Dockerfile y docker-compose.yml a la raíz del proyecto
cp docker/Dockerfile .
cp docker/docker-compose.yml .

# Levantar localmente (H2 in-memory, perfil dev)
docker compose up --build

# Acceder a:
#   API:     http://localhost:8080/employees
#   Swagger: http://localhost:8080/swagger-ui.html
#   H2:      http://localhost:8080/h2-console
#   Health:  http://localhost:8080/actuator/health
```

---

## 2. Infraestructura en AWS (Terraform)

### Pre-requisitos
- AWS CLI configurado (`aws configure`)
- Terraform >= 1.5
- kubectl
- helm

### Crear el bucket S3 para el estado de Terraform

```bash
aws s3 mb s3://invex-terraform-state --region us-east-1
aws s3api put-bucket-versioning \
  --bucket invex-terraform-state \
  --versioning-configuration Status=Enabled
```

### Aplicar la infraestructura

```bash
cd scripts/

# Crear archivo de variables
cat > terraform.tfvars <<EOF
aws_region          = "us-east-1"
cluster_name        = "invex-eks"
db_password         = "TuPasswordSeguro123!"
app_admin_password  = "AdminPassword456!"
EOF

terraform init
terraform plan  -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

### Bootstrap del cluster (una sola vez)

```bash
chmod +x scripts/bootstrap.sh
./scripts/bootstrap.sh invex-eks us-east-1
```

---

## 3. Pipeline CI/CD (GitHub Actions)

### Secrets requeridos en GitHub → Settings → Secrets

| Secret | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | IAM user con permisos ECR + EKS |
| `AWS_SECRET_ACCESS_KEY` | Secret key del IAM user |
| `EKS_CLUSTER_NAME` | Nombre del cluster (ej: `invex-eks`) |
| `SONAR_TOKEN` | Token de SonarCloud | (Pendiente de implementar)

### Configurar el Environment de producción (aprobación manual)

1. GitHub → Settings → Environments → New environment → `production`
2. Activar **Required reviewers** y agregar tus aprobadores
3. El pipeline se pausará antes del deploy a PROD hasta que alguien apruebe

### Flujo del pipeline

```
push a develop → test → sonar → docker build → push ECR → deploy DEV (auto)
push a main    → test → sonar → docker build → push ECR → [APROBACIÓN] → deploy PROD
```

---

## 4. Kubernetes

### Ver estado de los ambientes

```bash
# DEV
kubectl get all -n employee-dev
kubectl logs -l app=employee-service -n employee-dev -f

# PROD
kubectl get all -n employee-prod
kubectl get hpa -n employee-prod
kubectl top pods -n employee-prod
```

### Rollback manual

```bash
kubectl rollout undo deployment/employee-service -n employee-prod
kubectl rollout history deployment/employee-service -n employee-prod
```

---

## 5. Variables de Entorno por Ambiente

### DEV
| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` |
| `APP_ADMIN_USER` | `admin` |
| `APP_ADMIN_PASSWORD` | `admin123` |

### PROD (inyectadas desde AWS Secrets Manager)
| Variable | Fuente |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `employee-service/prod/db-url` |
| `DB_USER` | `employee-service/prod/db-user` |
| `DB_PASSWORD` | `employee-service/prod/db-password` |
| `APP_ADMIN_USER` | `employee-service/prod/app-user` |
| `APP_ADMIN_PASSWORD` | `employee-service/prod/app-password` |

---

## 6. Personalización Requerida

Antes de usar en producción, actualiza estos valores:

- `k8s/dev/manifest.yaml` → `host: employee-dev.your-domain.com`
- `k8s/prod/manifest.yaml` → `host: employee.your-domain.com`
- `k8s/prod/manifest.yaml` → `certificate-arn: arn:aws:acm:...`
- `k8s/prod/manifest.yaml` → `wafv2-acl-arn: arn:aws:wafv2:...` (opcional)
- `scripts/main.tf` → `bucket = "invex-terraform-state"` (nombre único)

