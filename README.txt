<div align="center">

| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <img src="https://cdn-icons-png.flaticon.com/512/3176/3176366.png" width=200> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | <br> <h1 align="center">DexTime UI</h1> <br> |
| :--- |

**A web application for discovering services, scheduling appointments, and managing service providers.**

</div>

---
**Project Start Date: October 2026**

DexTime UI is the frontend for the DexTime appointment scheduling platform. Customers can browse establishments and services, request appointments, and manage their bookings. Managers can manage establishments, services, and providers, while providers can claim and complete appointments at their assigned establishments.

The application is built with **React 19**, **TypeScript**, and **Vite**. It communicates with the DexTime REST API and provides role-specific customer, manager, and provider experiences. The interface is in Brazilian Portuguese.

### Technologies used:
- React 19
- TypeScript
- Vite
- React Compiler
- ESLint
- Firebase Hosting configuration
- DexTime REST API

---

## 🧭 Quick Navigation
- 🚀 [Getting Started](#-getting-started)
- 👥 [Application Features](#-application-features)
- 🔐 [Authentication](#-authentication)
- 🔌 [API Integration](#-api-integration)
- 🌐 [Deployment](#-deployment)

---

## 🚀 Getting Started

### Prerequisites
- Node.js
- npm
- A running DexTime API server

### Installation

From the repository root:

1. Enter the frontend project directory:
```bash
cd dextime-ui
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

### Available Commands
```bash
npm run dev       # Start the Vite development server
npm run build     # Type-check and build the production app
npm run preview   # Preview the production build locally
npm run lint      # Run ESLint
```

---

## 👥 Application Features

### Customers
- Register and sign in to a customer account
- Browse establishments and their services
- Request an appointment from an available service time
- View and cancel personal appointments

### Managers
- Register and sign in to the manager panel
- Create and manage establishments
- Create services and add available dates
- Register providers for an establishment
- View appointments associated with an establishment

### Service Providers
- Sign in to the provider panel
- View establishments they are associated with
- Browse available appointments and claim one
- View their assigned appointments and mark confirmed appointments complete

---

## 🔐 Authentication

The application signs users in through the API and uses JWTs for authenticated requests. It checks the role provided by the token before showing role-specific areas:
- **Customer**: Browse services and manage personal appointments
- **Manager**: Manage establishments, services, providers, and appointments
- **Provider**: Claim and complete appointments at assigned establishments

Sessions are stored in the browser's local storage. Expired or unauthorized sessions are cleared and require the user to sign in again.

---

## 🔌 API Integration

The API base URL is configured with the `VITE_API_URL` environment variable. If it is not set, the frontend uses:

```text
http://localhost:8080/api/v1
```

Set `VITE_API_URL` to the base URL of the DexTime API before starting the development server or creating a production build.

The frontend uses these API routes:

### Authentication and Users
- `POST /auth/login` - Sign in
- `POST /customers` - Register a customer
- `POST /managers` - Register a manager
- `POST /providers?storeId={storeId}` - Register a provider for an establishment

### Establishments and Services
- `GET /stores` - List establishments
- `POST /stores` - Create an establishment
- `GET /procedures/store/{storeId}` - List an establishment's services
- `POST /procedures` - Create a service
- `PATCH /procedures/{procedureId}/dates` - Add service availability
- `GET /providers/me/stores` - List the signed-in provider's establishments
- `GET /providers/store/{storeId}` - List an establishment's providers

### Appointments
- `POST /appointments/request` - Request an appointment
- `GET /appointments/my` - List the signed-in customer's appointments
- `POST /appointments/cancel/{appointmentId}` - Cancel an appointment
- `GET /appointments/{storeId}/appointments` - List appointments for an establishment
- `POST /appointments/assign/{appointmentId}` - Claim an appointment
- `POST /appointments/complete/{appointmentId}` - Complete an appointment

---

## 🌐 Deployment

Create a production build with:
```bash
npm run build
```

The build output is written to `dist/`. Firebase Hosting configuration is included in `firebase.json`; it serves the production build and rewrites routes to the app entry point. Configure `VITE_API_URL` for the production API before building and deploying.

---

## 📩 Contact

If you liked this project or would like to share feedback, get in touch:
- Instagram: [@kal.rs2](https://www.instagram.com/kal.rs2/)
- LinkedIn: [www.linkedin.com/in/ramosprodev](https://www.linkedin.com/in/ramosprodev)
- Email: [ramosprodev@gmail.com](mailto:ramosprodev@gmail.com)
