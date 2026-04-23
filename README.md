
# 🚀 INVEX Employee Service — Infrastructure & DevSecOps
Solución técnica completa para el despliegue del servicio de empleados, utilizando arquitectura en EKS, integración continua con GitHub Actions, provisión con Terraform y contenedores Docker.

### 🛠️ Requisitos Previos
* **Docker & Docker Compose**
* **AWS CLI** configurado (`aws configure`)
* **Terraform** (>= 1.5)
* **Kubectl** y **Helm**
* Cuenta de repositorio en **GitHub**

---

### 🐳 1. Desarrollo Local (Docker Compose)
Para levantar el proyecto en tu máquina con base de datos H2 in-memory (perfil `dev`):

**Preparar y levantar contenedores:**
```bash
# Copiar las configuraciones a la raíz del proyecto
cp docker/Dockerfile .
cp docker/docker-compose.yml .

# Construir la imagen localmente y levantar el contenedor
docker compose up --build
```

---

### ☁️ 2. Infraestructura en AWS (Terraform)
Despliega todos los recursos necesarios (VPC, EKS, RDS MySQL, ECR, Secrets Manager) con Terraform.

**1. Crear el bucket S3 para el estado (Terraform State)**
```bash
aws s3 mb s3://invex-terraform-state --region us-east-1
aws s3api put-bucket-versioning \
  --bucket invex-terraform-state \
  --versioning-configuration Status=Enabled
```

**2. Aplicar la infraestructura**
```bash
cd scripts/

# Crear archivo de variables con tus datos
cat > terraform.tfvars <<EOF
aws_region          = "us-east-1"
cluster_name        = "invex-eks"
db_password         = "TuPasswordSeguro123!"
app_admin_password  = "AdminPassword456!"
EOF

# Inicializar y aplicar
terraform init
terraform apply -var-file=terraform.tfvars
```

**3. Bootstrap del Cluster (Ejecutar solo una vez)**
```bash
chmod +x scripts/bootstrap.sh
./scripts/bootstrap.sh invex-eks us-east-1
```

---

### ⚙️ 3. Pipeline CI/CD (GitHub Actions)
El proyecto utiliza un flujo Git Flow automatizado:
* `develop`: Deploy automático a DEV (Namespace: `employee-dev`, H2, 1 replica).
* `main`: Deploy a PROD (Namespace: `employee-prod`, RDS, 2-8 replicas + HPA) requiere aprobación.

**Configuración en GitHub:**
1. Ve a **Settings → Secrets and variables → Actions** y agrega:
    * `AWS_ACCESS_KEY_ID` (Permisos ECR + EKS)
    * `AWS_SECRET_ACCESS_KEY`
    * `EKS_CLUSTER_NAME` (Ej: `invex-eks`)
    * `SONAR_TOKEN`
2. Ve a **Settings → Environments**, crea uno nuevo llamado `production`, activa la opción **Required reviewers** y asigna a los aprobadores.

---

### ☸️ 4. Kubernetes (Monitoreo y Despliegue)
Verifica el estado del cluster directamente a través de kubectl.

**Monitoreo en Desarrollo (DEV)**
```bash
kubectl get all -n employee-dev
kubectl logs -l app=employee-service -n employee-dev -f
```

**Monitoreo en Producción (PROD)**
```bash
kubectl get all -n employee-prod
kubectl get hpa -n employee-prod
kubectl top pods -n employee-prod
```

**Rollback de Emergencia**
```bash
kubectl rollout undo deployment/employee-service -n employee-prod
kubectl rollout history deployment/employee-service -n employee-prod
```

---

### 📖 5. Documentación y Accesos Locales
Una vez levantado tu ambiente de desarrollo (Paso 1), puedes acceder a las siguientes herramientas:

**Credenciales por defecto (DEV):**
* **Usuario:** `admin`
* **Password:** `admin123`

**Enlaces:**
* **API Base:** http://localhost:8080/employees
* **Swagger UI:** http://localhost:8080/swagger-ui.html
* **Consola H2:** http://localhost:8080/h2-console
* **Health Check:** http://localhost:8080/actuator/health

---

### 🔐 6. Personalización para Producción
Antes de promover a un entorno real, asegúrate de actualizar estos valores en tus manifiestos y scripts:

* `k8s/dev/manifest.yaml` → Modificar el Ingress host (ej: `employee-dev.your-domain.com`)
* `k8s/prod/manifest.yaml` → Modificar el Ingress host (ej: `employee.your-domain.com`)
* `k8s/prod/manifest.yaml` → Actualizar el `certificate-arn` y `wafv2-acl-arn` con los datos de AWS.
* `scripts/main.tf` → Cambiar el `bucket = "invex-terraform-state"` por un nombre globalmente único.
* **Seguridad:** Las variables en producción (DB URL, usuarios, passwords) son inyectadas de forma segura y automática desde AWS Secrets Manager al namespace de K8s.
