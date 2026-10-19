<div align="center">

| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <img src="https://github.com/user-attachments/assets/a9da25a6-9677-4cf1-a4d6-35e6f9477cc8" alt="dextime-icon" width="200" /> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | <br> <h1 align="center">DexTime API</h1> <br> |
| :--- | :--- |

**A comprehensive appointment scheduling API with automated email notifications.**

</div>

---

**Project Start Date: October 2026**

DexTime API is the backend of the DexTime appointment scheduling platform, developed in **Java 21** with **Spring Boot 4.1.0**. It is a robust appointment management system that enables customers to schedule services, managers to manage stores and procedures, and providers to handle appointments.

It features secure JWT authentication, MongoDB persistence, RabbitMQ message queuing, and automated email notifications via AWS SES.

### Technologies Used
- Java 21 (Spring Framework)
- Spring Boot 4.1.0
- Spring Security & JWT Authentication
- Spring Data MongoDB
- RabbitMQ (Spring AMQP)
- AWS SES (Simple Email Service)
- AWS SNS (Simple Notification Service)
- Thymeleaf (Email Templates)
- Lombok
- Jackson
- Spring Boot Validation
- Docker

---

## 🧭 Quick Navigation
- 🚀 [Getting Started](#-getting-started)
- 🔐 [Authentication & Security](#-authentication--security)
- 📧 [Email Notifications](#-email-notifications)
- 📊 [API Endpoints](#-api-endpoints)
- 🔧 [Configuration](#-configuration)
- 📩 [Contact](#-contact-me)

---

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- MongoDB
- RabbitMQ
- AWS Account (for SES/SNS)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/ramosprodev-krs/dextime-api.git
cd dextime-api
```

2. Configure environment variables in `.env`:
```env
MONGODB_URI=mongodb://localhost:27017/dextime
JWT_SECRET=your-secret-key
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
SERVER_PORT=7860
```

3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

### Docker Deployment

Build and run with Docker:
```bash
docker build -t dextime-api .
docker run -p 7860:7860 --env-file .env dextime-api
```

---

## 🔐 Authentication & Security

The API uses JWT (JSON Web Tokens) for authentication. Users can register as:
- **Customer**: Can request and manage their own appointments
- **Manager**: Can manage stores, procedures, and view store appointments
- **Provider**: Can assign and complete appointments at their assigned stores

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

Response:
```json
{
  "token": "jwt-token-here"
}
```

Include the token in subsequent requests:
```http
Authorization: Bearer {token}
```

---

## 📧 Email Notifications

The system sends automated emails for:
- **Appointment Requested**: Confirmation sent to the customer when they request an appointment
- **Appointment Confirmed**: Sent to the customer when a provider accepts the appointment
- **Appointment Assigned**: Sent to the provider when they are assigned to an appointment
- **Appointment Completed**: Sent to the customer when the service is completed
- **Appointment Cancelled**: Sent to both customer and provider when an appointment is cancelled

Email templates are located in `src/main/resources/templates/emails/` and are written in Portuguese.

---

## 📊 API Endpoints

### Stores
- `GET /api/v1/stores` - List all stores
- `GET /api/v1/stores/{storeId}` - Get store by ID
- `POST /api/v1/stores` - Create new store (Manager only)

### Procedures
- `POST /api/v1/procedures` - Create new procedure (Manager only)
- `PATCH /api/v1/procedures/{procedureId}/dates` - Add available dates (Manager only)

### Appointments
- `POST /api/v1/appointments/request` - Request appointment (Customer only)
- `POST /api/v1/appointments/assign/{appointmentId}` - Assign provider (Provider only)
- `DELETE /api/v1/appointments/{appointmentId}` - Cancel appointment (Customer only)
- `PATCH /api/v1/appointments/{appointmentId}/complete` - Complete appointment (Provider only)
- `GET /api/v1/appointments/my` - List my appointments (Customer only)
- `GET /api/v1/appointments/{storeId}/appointments` - List store appointments (Manager/Provider only)

### Users
- `POST /api/v1/customers` - Register customer
- `POST /api/v1/managers` - Register manager

For detailed API documentation, see `API_DOCUMENTATION.txt`.

---

## 🔧 Configuration

### Application Properties
- `spring.application.name=dextime-api`
- `server.port=${SERVER_PORT}` (default: 7860)
- `spring.mongodb.uri=${MONGODB_URI}`

### Security Configuration
- CORS enabled for all origins
- JWT-based stateless authentication
- Role-based access control (`CUSTOMER`, `MANAGER`, `PROVIDER`)

### Message Queue
- RabbitMQ for asynchronous email processing
- Queue-based email sending via AWS SES

---

## 📩 Contact me!

If you liked this project or would like to share your opinion about it, give me feedback! Here are a few ways to contact me:
- Instagram: [@kal.rs2](https://www.instagram.com/kal.rs2/)
- LinkedIn: [linkedin.com/in/ramosprodev](https://www.linkedin.com/in/ramosprodev)
- Email: [ramosprodev@gmail.com](mailto:ramosprodev@gmail.com)
