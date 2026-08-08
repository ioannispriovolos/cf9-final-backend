# Network Infrastructure Management Platform – Backend

## Overview

The **Network Infrastructure Management Platform** is a secure enterprise-style backend application developed as the final project for the **Coding Factory @ Athens University of Economics and Business (AUEB)**.

The platform enables centralized management of network devices from multiple vendors (such as Cisco, MikroTik, Aruba, Palo Alto, etc.), providing secure authentication, role-based authorization, device management, dashboard statistics, and concurrent SSH command execution.

The project follows modern Spring Boot development practices, including layered architecture, Data Transfer Objects (DTOs), service-oriented design, validation, exception handling, JWT authentication, Flyway database migrations, and Docker-based development.

The backend follows a layered architecture influenced by Domain-Driven Design principles. The domain model represents the core concepts of network infrastructure management, including devices, users, roles and capabilities, while controllers, DTOs, application services, repositories, security and infrastructure concerns are separated into dedicated layers.

---

# Features

- JWT Authentication
- Role-Based Access Control (RBAC)
- Capability-based authorization using Spring Security
- Secure password hashing using BCrypt
- AES-256 encryption for stored device SSH credentials
- Device CRUD operations
- Soft-delete support
- Dashboard with network statistics
- Concurrent SSH command execution
- Swagger / OpenAPI documentation
- PostgreSQL database
- Flyway database migrations
- Docker Compose support
- Bean Validation
- Global exception handling
- Pagination support
- Audit timestamps
- Enterprise REST API design

---

# Technology Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker & Docker Compose
- Apache MINA SSHD
- JWT (JJWT)
- Lombok
- Hibernate
- Swagger / OpenAPI

---

# Project Structure

```
src
 ├── configuration
 ├── controller
 ├── dto
 ├── entity
 ├── exception
 ├── mapper
 ├── repository
 ├── security
 ├── service
 ├── ssh
 ├── validation
 └── BackendApplication
```

---

# Running the Project

## Requirements

- Java 21
- Docker Desktop
- Git

---

## Clone the repository

```bash
git clone https://github.com/ioannispriovolos/cf9-final-backend.git
```

```bash
cd cf9-final-backend
```

---

## Start PostgreSQL

```bash
docker compose up --build
```

Docker will create the PostgreSQL container.

---

## Run the backend

Using Gradle:

```bash
./gradlew bootRun
```

or

Run the `BackendApplication` class directly from IntelliJ IDEA.

---

# Database Initialization

Flyway automatically creates:

- schemas
- tables
- indexes
- constraints
- relationships
- seed data
- demo users
- roles
- capabilities

No manual SQL execution is required.

---

# Demo Users

The following accounts are automatically created by the Flyway seed migration.

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin_user` | `password123` |
| Network Engineer | `engineer_user` | `password123` |
| Viewer | `viewer_user` | `password123` |

---

# Password Repair (First Execution Only)

Depending on the local BCrypt implementation or environment, the seeded password hashes may not match the application's configured password encoder.

If authentication with the demo users fails on the first execution, create the following class **in the same package as `BackendApplication`**, run the application once, verify that the passwords have been updated, and then **delete the class**.

```java
@Component
@RequiredArgsConstructor
public class PasswordRepairRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        System.out.println("=== STARTING LIVE PASSWORD REPAIR ===");

        String nativeHash =
                passwordEncoder.encode("password123");

        System.out.println(
                "Your app's native hash for 'password123' is: "
                        + nativeHash
        );

        userRepository.findByUsername("admin_user")
                .ifPresent(user -> {
                    user.setPassword(nativeHash);
                    userRepository.save(user);
                    System.out.println(
                            "Successfully repaired admin_user!"
                    );
                });

        userRepository.findByUsername("engineer_user")
                .ifPresent(user -> {
                    user.setPassword(nativeHash);
                    userRepository.save(user);
                    System.out.println(
                            "Successfully repaired engineer_user!"
                    );
                });

        userRepository.findByUsername("viewer_user")
                .ifPresent(user -> {
                    user.setPassword(nativeHash);
                    userRepository.save(user);
                    System.out.println(
                            "Successfully repaired viewer_user!"
                    );
                });

        System.out.println(
                "=== PASSWORD REPAIR COMPLETE ==="
        );
    }
}
```

After the passwords have been repaired successfully:

1. Stop the application.
2. Delete the `PasswordRepairRunner` class.
3. Start the application again.

This class will **not** remain in production deployments.

---

# Authentication

Authenticate using:

```
POST /api/v1/auth/authenticate
```

Example request:

```json
{
    "username": "admin_user",
    "password": "password123"
}
```

The response returns a JWT access token.

Use the token in subsequent requests:

```
Authorization: Bearer <JWT_TOKEN>
```

---

# Swagger Documentation

After starting the application, the API documentation is available at:

```
http://localhost:8080/swagger-ui/index.html
```

Click **Authorize**, paste the generated JWT token, and invoke secured endpoints according to the permissions assigned to the authenticated user.

---

# Main Functionalities

## Authentication

- Login
- JWT generation
- JWT validation

## User Management

- Create users
- Update users
- Soft delete users
- Paginated user retrieval

## Device Management

- Register devices
- Update devices
- Soft delete devices
- Paginated device retrieval

## Dashboard

- Active device count
- Manufacturer statistics
- Device model statistics
- Monthly device additions
- Recently added devices

## SSH Automation

- Execute commands on one or multiple devices
- Concurrent execution
- Connection timeout handling
- Command timeout handling
- Output size limiting
- Detailed execution results
- Secure credential decryption

---

# Security

The backend implements multiple security layers:

- Spring Security
- JWT authentication
- Role-Based Access Control
- Capability-based authorization
- BCrypt password hashing
- AES-256 encryption for stored SSH passwords
- Bean Validation
- Global exception handling
- Soft-delete support
- Configurable SSH limits

---

# Notes for Examiners

- The database is automatically initialized using Flyway.
- Docker Compose is used to provide a reproducible PostgreSQL environment.
- The backend exposes a fully documented REST API through Swagger/OpenAPI.
- The included demo users provide access to the different authorization levels implemented by the platform.
- If the seeded passwords are not accepted due to BCrypt differences between environments, execute the provided `PasswordRepairRunner` once and remove it afterwards.

---

# Author

**Ioannis Priovolos**

Coding Factory @ Athens University of Economics and Business (AUEB)

---

# Acknowledgements

This project was designed and implemented by **Ioannis Priovolos** as part of the Coding Factory final project.

The implementation was developed with the assistance of **OpenAI's ChatGPT**, which was used as an AI programming assistant for architectural discussions, code reviews, documentation, and development support. All design decisions, implementation, integration, testing, and final project responsibility remain with the project author.
