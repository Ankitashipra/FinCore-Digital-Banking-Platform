# FinCore — Digital Banking Platform

A full-stack digital banking simulation built with **Java, Spring Boot Microservices, Angular, and PostgreSQL**. FinCore demonstrates how core banking-style workflows can be organized into modular services with secure API communication and a modern web interface.

> **Project status:** Portfolio project / actively evolving  
> **Environment:** Local development with Docker Compose  
> **Note:** FinCore is an educational simulation. It does not process real money or provide regulated banking services.

## ✨ Highlights

- **Identity & Access** — User registration, authentication, JWT-based authorization, and email verification workflows.
- **Account Management** — Account requests, account lifecycle workflows, and customer account views.
- **Payments & Transfers** — Transfer and payment workflows with transaction records.
- **Admin Workflows** — Administrative customer and account management capabilities.
- **Notifications** — Dedicated notification service for application events.
- **Financial Insights** — Transaction history, spending analytics, and account activity views.
- **Modern Web UI** — Angular interface for customer and administrative workflows.

## 🏗️ Architecture

FinCore follows a microservices-oriented architecture:

| Component | Responsibility |
|---|---|
| API Gateway | Central API entry point and request routing |
| Identity Service | Registration, authentication, user identity |
| Account Service | Accounts, transactions, transfers, analytics |
| Payment Service | Payment processing workflows |
| Notification Service | Application notifications |
| Angular Frontend | Customer and admin user interface |
| PostgreSQL | Persistent relational data |
| Redis | Supporting cache / infrastructure layer |

## 🛠️ Tech Stack

**Backend:** Java 21 · Spring Boot · Spring Security · Spring Cloud Gateway · Maven · REST APIs

**Frontend:** Angular · TypeScript · HTML · SCSS

**Data & Infrastructure:** PostgreSQL · Redis · Docker · Docker Compose

## 📁 Project Structure

```text
FinCore/
├── backend/
│   ├── api-gateway/
│   ├── identity-service/
│   ├── account-service/
│   ├── payment-service/
│   └── notification-service/
├── frontend/
├── infrastructure/
│   ├── docker/
│   └── kubernetes/
├── docs/
└── IMPLEMENTATION_NOTES.md
```

## 🚀 Run Locally

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js and npm
- Docker Desktop

### Start infrastructure and backend services

Configure the environment variables required by the Docker Compose configuration, then run:

```powershell
cd infrastructure/docker
docker compose up --build -d
```

### Start the Angular frontend

```powershell
cd frontend
npm install
npm start
```

Open the frontend at:

```text
http://localhost:4200
```

The API Gateway is configured to run on port `8086`.

## 🎯 Engineering Focus

FinCore is a hands-on exploration of:

- Microservice boundaries and service-to-service communication
- Authentication and role-based authorization
- REST API design and frontend-backend integration
- Relational data modeling and transaction workflows
- Containerized local development
- Building an end-to-end application across multiple services

## ⚠️ Disclaimer

This project is intended for learning, demonstration, and portfolio purposes only. It is not a real banking system. Do not use real financial information, production credentials, or actual payment data.

---

**Built as a full-stack software engineering portfolio project.**