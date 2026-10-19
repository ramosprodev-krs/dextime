<div align="center">

| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <img src="https://github.com/user-attachments/assets/a9da25a6-9677-4cf1-a4d6-35e6f9477cc8" alt="dextime-icon" width="200" /> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | <br> <h1 align="center">DexTime</h1> <br> |
| :--- | :--- |

**A complete appointment scheduling platform with role-based access and automated email notifications.**

</div>

---

**Project Start Date: October 2026**

DexTime is a full-stack appointment scheduling platform. Customers can discover establishments and book services, managers can organize their establishments, services and providers, and providers can claim and complete appointments at the establishments they are assigned to.

The project is split into two applications, each one living in its own branch of this repository:

| Branch | Project | Description |
| :--- | :--- | :--- |
| [`backend`](https://github.com/ramosprodev-krs/dextime/tree/backend) | **DexTime API** | REST API built with Java 21 and Spring Boot |
| [`frontend`](https://github.com/ramosprodev-krs/dextime/tree/frontend) | **DexTime UI** | Web application built with React and TypeScript |
| `master` | **Documentation** | General project documentation (this branch) |

> This branch contains documentation only. The source code of each application is in its own branch.

### Technologies Used

**Backend**
- Java 21
- Spring Boot 4.1.0
- Spring Security & JWT Authentication
- Spring Data MongoDB
- RabbitMQ (Spring AMQP)
- AWS SES & AWS SNS
- Thymeleaf (Email Templates)
- Lombok
- Docker

**Frontend**
- React 19
- TypeScript
- Vite
- React Compiler
- ESLint
- Firebase Hosting

---

## 🧭 Quick Navigation
- 🏗️ [Architecture](#-architecture)
- 👥 [User Roles and Features](#-user-roles-and-features)
- 🔄 [Appointment Lifecycle](#-appointment-lifecycle)
- 🚀 [Getting Started](#-getting-started)
- 🔐 [Authentication & Security](#-authentication--security)
- 📧 [Email Notifications](#-email-notifications)
- 📊 [API Overview](#-api-overview)
- 🌐 [Deployment](#-deployment)
- 📂 [Repository Structure](#-repository-structure)
- 📩 [Contact](#-contact)

---

## 🏗️ Architecture

```text
┌──────────────────┐     HTTPS / JWT     ┌──────────────────────┐
│   DexTime UI     │ ──────────────────▶ │     DexTime API      │
│ React + Vite     │ ◀────────────────── │ Spring Boot + JWT    │
│ (Firebase Host)  │       JSON          └──────────┬───────────┘
└──────────────────┘                                │
                                    ┌───────────────┼────────────────┐
                                    ▼               ▼                ▼
                              ┌──────────┐   ┌────────────┐   ┌─────────────┐
                              │ MongoDB  │   │  RabbitMQ  │   │   AWS SNS   │
                              │ (data)   │   │  (queue)   │   │ AWS SES     │
                              └──────────┘   └─────┬──────┘   │ (e-mails)   │
                                                   │          └─────────────┘
                                                   ▼
                                           Email consumer
```

- **DexTime UI** is a single-page application that talks to the API over REST and stores the user session in the browser.
- **DexTime API** holds all business rules, authentication and persistence. It follows a layered structure (`controller`, `service`, `repository`, `entity`, `dto`, `exception`).
- **MongoDB** stores users, stores, procedures and appointments.
- **RabbitMQ** decouples e-mail sending from the request cycle: the API publishes a message and a consumer processes it asynchronously.
- **AWS SES/SNS** deliver the notifications, using Thymeleaf HTML templates written in Portuguese.

---

## 👥 User Roles and Features

### Customers
- Register and sign in to a customer account
- Browse establishments and their services
- Request an appointment from an available service time
- View and cancel personal appointments
- Receive e-mail notifications about their appointments

### Managers
- Register and sign in to the manager panel
- Create and manage establishments
- Create services (procedures) and add available dates
- Register providers for an establishment
- View appointments associated with an establishment

### Service Providers
- Sign in to the provider panel
- View the establishments they are associated with
- Browse available appointments and claim one
- View their assigned appointments and mark confirmed appointments as complete

---

## 🔄 Appointment Lifecycle

```text
Customer requests ──▶ Provider claims ──▶ Provider completes
        │                    │                     │
        ▼                    ▼                     ▼
  "Requested" e-mail   "Confirmed" e-mail     "Completed" e-mail
  to the customer      to the customer        to the customer
                       "Assigned" e-mail
                       to the provider

        Customer cancels at any time ──▶ "Cancelled" e-mail
                                          to customer and provider
```

1. A **customer** requests an appointment for an available date of a service.
2. A **provider** from that establishment claims the appointment, which confirms it.
3. After the service is delivered, the **provider** marks it as completed.
4. A **customer** can cancel the appointment before it is completed.

---

## 🚀 Getting Started

To run the full platform locally, start the API first and then the UI.

### Prerequisites
- Java 21 or higher
- Maven 3.8+ (or the included Maven Wrapper)
- Node.js and npm
- MongoDB
- RabbitMQ
- AWS Account (for SES/SNS)

### 1. Get the code

Each application lives in its own branch. Clone the repository twice, or use separate working copies, one per branch:

```bash
git clone --branch backend https://github.com/ramosprodev-krs/dextime.git dextime-api
git clone --branch frontend https://github.com/ramosprodev-krs/dextime.git dextime-ui
```

### 2. Run the API

```bash
cd dextime-api
```

Create a `.env` file with the environment variables:

```env
MONGODB_URI=mongodb://localhost:27017/dextime
JWT_SECRET=your-secret-key
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
SERVER_PORT=7860
```

Build and start the application:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### 3. Run the UI

```bash
cd dextime-ui/dextime-ui
npm install
npm run dev
```

The UI reads the API address from the `VITE_API_URL` environment variable. If it is not set, it falls back to `http://localhost:8080/api/v1`. Make sure it points to the same address and port where your API is running, for example:

```env
VITE_API_URL=http://localhost:7860/api/v1
```

### Available Frontend Commands
```bash
npm run dev       # Start the Vite development server
npm run build     # Type-check and build the production app
npm run preview   # Preview the production build locally
npm run lint      # Run ESLint
```

---

## 🔐 Authentication & Security

The platform uses JWT (JSON Web Tokens) for authentication.

- The user signs in through `POST /api/v1/auth/login` and receives a token.
- The UI sends the token in every authenticated request:

```http
Authorization: Bearer {token}
```

- The API is **stateless** and applies **role-based access control** with the roles `CUSTOMER`, `MANAGER` and `PROVIDER`.
- The UI checks the role contained in the token before showing role-specific areas.
- Sessions are stored in the browser's local storage. Expired or unauthorized sessions are cleared and the user must sign in again.
- CORS is enabled in the API so the UI can be served from a different origin.

---

## 📧 Email Notifications

The system sends automated e-mails for:

| Event | Recipient |
| :--- | :--- |
| Appointment Requested | Customer |
| Appointment Confirmed | Customer |
| Appointment Assigned | Provider |
| Appointment Completed | Customer |
| Appointment Cancelled | Customer and provider |

Messages are published to RabbitMQ and processed asynchronously by an e-mail consumer, which renders a Thymeleaf template and sends it through AWS SES. Templates are located in `src/main/resources/templates/emails/` in the `backend` branch.

---

## 📊 API Overview

All routes are prefixed with `/api/v1`. This is a summary; see the README of the [`backend`](https://github.com/ramosprodev-krs/dextime/tree/backend) branch for the complete reference.

### Authentication and Users
- `POST /auth/login` - Sign in
- `POST /customers` - Register a customer
- `POST /managers` - Register a manager
- `POST /providers?storeId={storeId}` - Register a provider for an establishment
- `GET /providers/me/stores` - List the signed-in provider's establishments
- `GET /providers/store/{storeId}` - List an establishment's providers

### Establishments and Services
- `GET /stores` - List establishments
- `GET /stores/{storeId}` - Get an establishment by ID
- `POST /stores` - Create an establishment (Manager)
- `GET /procedures/store/{storeId}` - List an establishment's services
- `POST /procedures` - Create a service (Manager)
- `PATCH /procedures/{procedureId}/dates` - Add service availability (Manager)

### Appointments
- `POST /appointments/request` - Request an appointment (Customer)
- `GET /appointments/my` - List the signed-in customer's appointments (Customer)
- `GET /appointments/{storeId}/appointments` - List appointments for an establishment (Manager/Provider)
- `POST /appointments/assign/{appointmentId}` - Claim an appointment (Provider)
- Cancel an appointment (Customer)
- Complete an appointment (Provider)

---

## 🌐 Deployment

### Backend (Docker)

The `backend` branch includes a `Dockerfile` for cloud deployment:

```bash
docker build -t dextime-api .
docker run -p 7860:7860 --env-file .env dextime-api
```

### Frontend (Firebase Hosting)

Create a production build with:

```bash
npm run build
```

The build output is written to `dist/`. Firebase Hosting configuration is included in `firebase.json`; it serves the production build and rewrites routes to the app entry point. Configure `VITE_API_URL` with the production API address **before** building.

---

## 📂 Repository Structure

```text
dextime
├── master      → Documentation (this branch)
├── backend     → DexTime API (Spring Boot)
│   ├── src/main/java/com/dextime/api/
│   │   ├── configuration/   AWS, RabbitMQ and Security setup
│   │   ├── consumer/        RabbitMQ e-mail consumer
│   │   ├── controller/      REST controllers
│   │   ├── dto/             Request and response objects
│   │   ├── entity/          Domain entities and enums
│   │   ├── exception/       Custom exceptions and global handler
│   │   ├── filter/          JWT security filter
│   │   ├── repository/      MongoDB repositories
│   │   └── service/         Business logic
│   ├── src/main/resources/  Properties and e-mail templates
│   └── Dockerfile
└── frontend    → DexTime UI (React + TypeScript)
    └── dextime-ui/          Vite application
```

---

## 📩 Contact

If you liked this project or would like to share your opinion about it, give me feedback! Here are a few ways to contact me:
- Instagram: [@kal.rs2](https://www.instagram.com/kal.rs2/)
- LinkedIn: [linkedin.com/in/ramosprodev](https://www.linkedin.com/in/ramosprodev)
- Email: [ramosprodev@gmail.com](mailto:ramosprodev@gmail.com)
