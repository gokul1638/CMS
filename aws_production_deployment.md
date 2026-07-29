# AWS Production Environment Deployment Guide

This document provides step-by-step instructions to deploy the Cloud Management System (CMS) in a secure, high-availability, decoupled production environment on AWS.

---

## Architecture Overview

```text
                  +-----------------------------------+
                  |           Cloud Clients           |
                  +-----------------------------------+
                                    |
                                    v (HTTPS on port 443)
                  +-----------------------------------+
                  |     Amazon CloudFront (CDN)       |
                  +-----------------------------------+
                    /                               \
       (Default /*) /                                 \ (API Requests /api/*)
                   v                                   v
        +---------------------+             +---------------------+
        |  Amazon S3 Bucket   |             |  Application Load   |
        |  (Static UI Assets) |             |   Balancer (ALB)    |
        +---------------------+             +---------------------+
                                                       |
                                                       v (Port 8081)
                                            +---------------------+
                                            |   AWS ECS Fargate   |
                                            | (Spring Boot Tasks) |
                                            +---------------------+
                                                       |
                                                       v (Port 3066)
                                            +---------------------+
                                            |  Amazon RDS MySQL   |
                                            | (Managed Database)  |
                                            +---------------------+
```

---

## Prerequisites
1. **AWS Account**: Admin access.
2. **AWS CLI**: Installed and configured (`aws configure`).
3. **Docker**: Running locally to compile and push images.
4. **Domain Name**: Registered via Route 53 or other DNS registrars.

---

## Step 1: Network & Security Group Configuration

We must create security groups to restrict traffic strictly to necessary paths.

### 1. Database Security Group (`rds-sg`)
1. Open the **EC2 Console**, go to **Network & Security** -> **Security Groups**.
2. Click **Create security group**:
   - **Name**: `cms-rds-sg`
   - **Description**: Allow access to database from ECS backend.
   - **VPC**: Select your default VPC.
3. Under **Inbound Rules**, add:
   - **Type**: `MYSQL/Aurora` (Port 3306).
   - **Source**: Leave blank for now (we will update this once the ECS security group is created).

### 2. Backend ECS Security Group (`ecs-sg`)
1. Click **Create security group**:
   - **Name**: `cms-ecs-sg`
   - **Description**: Allow traffic from Application Load Balancer (ALB).
2. Under **Inbound Rules**, add:
   - **Type**: `Custom TCP` (Port 8081).
   - **Source**: Leave blank (we will update this once the ALB is created).

### 3. Load Balancer Security Group (`alb-sg`)
1. Click **Create security group**:
   - **Name**: `cms-alb-sg`
   - **Description**: Allow public web access.
2. Under **Inbound Rules**, add:
   - **Type**: `HTTP` (Port 80), **Source**: `Anywhere-IPv4` (`0.0.0.0/0`).
   - **Type**: `HTTPS` (Port 443), **Source**: `Anywhere-IPv4` (`0.0.0.0/0`).

### 4. Inter-link the Security Groups
1. Open `cms-ecs-sg` -> **Edit inbound rules**:
   - Set the source for the Port 8081 rule to be the security group ID of `cms-alb-sg`.
2. Open `cms-rds-sg` -> **Edit inbound rules**:
   - Set the source for the Port 3306 rule to be the security group ID of `cms-ecs-sg`.

---

## Step 2: Provision the MySQL Database (Amazon RDS)

1. Open the **Amazon RDS Console** -> **Databases** -> **Create database**.
2. **Database creation method**: `Standard create`.
3. **Engine options**: `MySQL` (Community Edition).
4. **Engine Version**: `MySQL 8.0.33` (or latest).
5. **Templates**: `Free Tier` or `Dev/Test`.
6. **Settings**:
   - **DB instance identifier**: `cms-production-db`
   - **Master username**: `cms_user`
   - **Master password**: *Choose a secure password* (e.g. `CmsPass12345!`)
7. **Connectivity**:
   - **Virtual private cloud (VPC)**: Default VPC.
   - **Public access**: `No` (Critical for security).
   - **Existing VPC security groups**: Select `cms-rds-sg`.
8. **Additional configuration**:
   - **Initial database name**: `cms_cloud`
9. Click **Create database**. Once created, copy the **Endpoint URL** (e.g., `cms-production-db.xxxx.us-east-1.rds.amazonaws.com`).

---

## Step 3: Build & Push Backend Container to ECR

1. Open the **Amazon ECR Console** -> **Repositories** -> **Create repository**:
   - **Repository name**: `cms-backend`
   - **Tag immutability**: Enabled.
   - Click **Create repository**.
2. Open a local terminal at the workspace root (`d:\CBMS\CMS\CMS`) and authenticate the AWS CLI:
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin your-aws-account-id.dkr.ecr.us-east-1.amazonaws.com
   ```
3. Compile, package, and tag the docker container:
   ```bash
   docker build -t cms-backend .
   docker tag cms-backend:latest your-aws-account-id.dkr.ecr.us-east-1.amazonaws.com/cms-backend:latest
   ```
4. Push the image to AWS:
   ```bash
   docker push your-aws-account-id.dkr.ecr.us-east-1.amazonaws.com/cms-backend:latest
   ```

---

## Step 4: Deploy the Backend on AWS ECS (Fargate)

### 1. Create the Application Load Balancer (ALB)
1. Open the **EC2 Console** -> **Load Balancing** -> **Load Balancers** -> **Create load balancer**.
2. Choose **Application Load Balancer**:
   - **Name**: `cms-alb`
   - **Scheme**: `Internet-facing`.
   - **IP address type**: `IPv4`.
   - **VPC & Mappings**: Select VPC and at least two public subnets in different availability zones.
   - **Security groups**: Select `cms-alb-sg`.
3. Under **Listeners and routing**:
   - Create a listener on Protocol `HTTP`, Port `80`.
   - Click **Create target group**:
     - **Target type**: `IP` (required for Fargate).
     - **Target group name**: `cms-tg`
     - **Protocol**: `HTTP`, Port: `8081`.
     - **Health check path**: `/health`.
     - Click **Next** -> **Create target group** (without registering targets manually).
4. Return to the Load Balancer setup, select `cms-tg` for the Port 80 listener. Click **Create load balancer**.

### 2. Configure ECS Cluster & Task Definition
1. Open the **Amazon ECS Console** -> **Clusters** -> **Create cluster**:
   - **Cluster name**: `cms-production-cluster`
   - **Infrastructure**: AWS Fargate (Serverless). Click **Create**.
2. Go to **Task definitions** -> **Create new task definition**:
   - **Family**: `cms-task`
   - **Launch type**: AWS Fargate.
   - **Task size**: CPU: `0.5 vCPU`, Memory: `1 GB`.
   - **Task execution role**: Select or create `ecsTaskExecutionRole`.
   - **Container details**:
     - **Name**: `cms-api`
     - **Image URI**: Enter your ECR Image URI (`your-aws-account-id.dkr.ecr.us-east-1.amazonaws.com/cms-backend:latest`).
     - **Port mappings**: Container Port: `8081`, Protocol: `TCP`.
     - **Environment variables**: Add the following keys:
       - `DB_URL` = `jdbc:mysql://your-rds-endpoint-here:3306/cms_cloud?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
       - `DB_USERNAME` = `cms_user`
       - `DB_PASSWORD` = `your-rds-password-here`
       - `DB_DRIVER` = `com.mysql.cj.jdbc.Driver`
       - `DB_DIALECT` = `org.hibernate.dialect.MySQLDialect`
       - `DB_DDL` = `update`
   - Click **Create**.

### 3. Create the ECS Service
1. Inside your `cms-production-cluster`, go to **Services** -> **Create**:
   - **Compute options**: Launch type -> `FARGATE`.
   - **Deployment configuration**: Task definition Family -> `cms-task`, Service name -> `cms-service`, Desired tasks -> `2`.
   - **Networking**:
     - **Security group**: Select `cms-ecs-sg`.
     - **Subnets**: Choose private subnets if you set up a NAT Gateway, or public subnets with Auto-assign public IP enabled for direct pulls.
   - **Load balancing**:
     - **Load balancer type**: Application Load Balancer.
     - **Load balancer**: `cms-alb`.
     - **Container**: `cms-api : 8081`.
     - **Target group**: Select `cms-tg`.
2. Click **Create**. ECS will spin up two container instances and bind them to the Load Balancer.

---

## Step 5: Host the Frontend UI on Amazon S3

1. Open the **Amazon S3 Console** -> **Create bucket**:
   - **Bucket name**: `cms-frontend-production-bucket`
   - **VPC Region**: Choose the same region.
   - Keep **Block all public access** enabled.
2. In the uploaded bucket, upload your root `frontend` folder files:
   - `index.html`
   - `styles.css`
   - `app.js`

---

## Step 6: Route Traffic through Amazon CloudFront (CDN)

1. Open the **Amazon CloudFront Console** -> **Create distribution**.
2. **Origin settings**:
   - **Origin domain**: Select your S3 bucket.
   - **Origin access**: Select **Origin access control settings (OAC)** -> Create control setting -> **Save** (Allows CloudFront to pull files from the private S3 bucket).
3. **Default cache behavior settings**:
   - **Viewer protocol policy**: Redirect HTTP to HTTPS.
   - **Allowed HTTP methods**: `GET, HEAD`.
4. Click **Create Origin** (Add Second Origin for API requests):
   - **Origin domain**: Select the domain DNS endpoint of your **Application Load Balancer (ALB)**.
   - **Protocol**: HTTP only (CloudFront manages SSL publicly; traffic from CloudFront to the ALB uses internal AWS HTTP routing).
5. Go to the **Behaviors** tab -> **Create behavior**:
   - **Path pattern**: `/api/*`
   - **Origin**: Select your ALB Origin.
   - **Allowed HTTP methods**: `GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE`.
   - **Cache key and origin requests**: Choose **Cache policy** -> `CachingDisabled`, **Origin request policy** -> `AllViewer`. (Required so that API queries are never cached and headers like Authorization tokens pass directly to the Spring backend).
6. Click **Create distribution**.
7. *S3 Bucket Permissions Notice*: Copy the S3 bucket policy generated by CloudFront, navigate back to the S3 Bucket -> **Permissions** -> **Bucket policy** -> Paste and save it.

---

## Step 7: DNS Routing & SSL Setup (Let's Encrypt / Amazon Certificate Manager)

To hook up your custom domain (e.g. `yourdomain.com`):
1. Open the **AWS Certificate Manager (ACM)** -> **Request a certificate** -> Request public certificate:
   - Domain name: `yourdomain.com` and `*.yourdomain.com`.
   - Validation method: DNS validation (add the CNAME records to your registrar).
2. Go back to your **CloudFront Distribution** -> **Edit**:
   - **Alternate domain names (CNAMEs)**: Add `yourdomain.com` and `www.yourdomain.com`.
   - **Custom SSL certificate**: Select the ACM certificate.
3. Open **Amazon Route 53** (or your third-party DNS provider) and add:
   - An **A Record** pointing to your CloudFront distribution domain name (using alias setting).

---

## Step 8: Database Seeding & Verification

1. On startup, the Spring Boot application (ECS Task) checks the MySQL `users` database table. Finding it empty, it executes the `DataLoader` and seeds:
   - Standard user: `customer` / password: `password`.
   - Admin user: `admin` / password: `password`.
   - Active tariffs and core system limits.
2. Navigate to `https://yourdomain.com` in your browser.
3. The UI will render securely over HTTPS. Log in, request custom resources, switch accounts to admin, and approve orders to confirm all microservices are successfully connected!
