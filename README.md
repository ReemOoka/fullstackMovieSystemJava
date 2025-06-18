# 🎬 Movie Management System – Secure RESTful API

An enterprise-grade **Movie Management System** built using **Spring Boot**, designed to demonstrate robust backend architecture, secure authentication, clean design patterns, and RESTful API best practices.

This project was developed as a **full stack solution** for managing movie records, built with **SOLID design principles**, multiple **authentication strategies**, and **modular architecture** to reflect production-level backend engineering.

---

## 📖 Project Overview

The system allows authorized users to manage a catalog of movies through a REST API. It includes complete CRUD operations, search, filtering, pagination, and user authentication and authorization.

> This project is designed to simulate **real-world enterprise software**, including versioned APIs, secure endpoints, and architectural patterns typically used in scalable backend systems.

---

## 🎯 Core Features

### ✅ REST API Design with Spring Boot
The application exposes RESTful endpoints to create, read, update, and delete movie records. It follows **versioned API design** (`/api/v1/movies`) and uses **Spring MVC** controllers and DTOs to structure requests and responses.

### ✅ SOLID Principles & Design Patterns
The backend is structured following **SOLID principles** for maintainability and scalability:
- **Single Responsibility** in services and controllers
- **Open-Closed** with interfaces and extensions
- **Dependency Inversion** using `@Autowired` and constructor injection

It also integrates key **creational, structural, and behavioral design patterns**, such as:
- **Factory** for object creation
- **Facade** for simplified access to services
- **Strategy**, **State**, and **Observer** patterns for behavioral logic (e.g., different filtering or authentication flows)

### 🔐 Spring Security & Authentication

The project supports **multi-strategy authentication**, including:

- 🔑 **Basic Authentication** for initial testing
- 🔐 **JWT (JSON Web Tokens)** for stateless, scalable security
- 🌐 **OAuth2** for third-party integrations (e.g., Google login)
- 🧬 **LDAP** for enterprise directory service support

Role-based access control is implemented using annotations like `@PreAuthorize`.

### 🗄️ Data Persistence & ORM

- Integrated with **MySQL** for relational data storage
- Uses **Spring Data JPA** and **Hibernate ORM** for clean entity mapping
- Also supports raw **JDBC** operations in specific modules for flexibility

### 📑 API Documentation with Swagger

The API is fully documented using **SpringDoc** and **Swagger UI**, enabling interactive testing and visual schema exploration at `/swagger-ui.html`.

### 🧪 Enterprise-Grade Testing

- **Unit Testing** with **JUnit 5** and **Mockito** for service-level logic
- **Integration Testing** using **Spring Boot Test** with embedded DBs
- Test classes follow best practices like mocking repositories and isolating test concerns

### 💻 Frontend Integration (Optional)

The backend is compatible with any frontend but includes demo integration with **JavaFX (FXML)** for a simple desktop UI. This layer interacts with the REST API and demonstrates full stack connectivity.

---

## 🧰 Technologies in Action

| Area              | Tech/Tools Used                                                | Purpose |
|-------------------|----------------------------------------------------------------|---------|
| Language & Core   | Java 11, Maven                                                 | Backend development & build management |
| Web/API Layer     | Spring Boot, Spring MVC, Spring Web, SpringDoc                | RESTful endpoints and documentation |
| Security          | Spring Security, JWT, OAuth2, LDAP, BAC                       | Multi-strategy user authentication & authorization |
| Architecture      | SOLID, MVC, Factory, Strategy, Observer, State                | Enterprise architecture & maintainable design |
| Data Persistence  | MySQL, Hibernate, JPA, JDBC                                   | Flexible relational data access |
| Testing           | JUnit 5, Mockito, Spring Boot Test                            | Unit and integration testing |
| UI (optional)     | JavaFX, FXML                                                  | Desktop GUI integration demo |
| Dev Tools         | Postman, Swagger UI                                           | API testing and documentation |

---

## 🚀 Getting Started

### 📦 Prerequisites

- Java 11+
- Maven
- MySQL
- (Optional) Postman, JavaFX

### 🔧 Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/ReemOoka/fullstackMovieSystem.git
   cd fullstackMovieSystem

### Configure your database

Update src/main/resources/application.properties with your DB config:

```bash
spring.datasource.url=jdbc:mysql://localhost:3306/movies_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

Run the application

```bash
mvn spring-boot:run

```

Access Swagger UI

```bash
http://localhost:8080/swagger-ui.html

```

🧪 Run Tests
```bash
mvn test
```
Tests are located in /src/test/java

Includes service layer tests with mocks and integration tests with in-memory DBs

📂 Project Structure

movie-management-api/
├── controller/
├── service/
├── model/
├── repository/
├── security/
├── dto/
├── config/
├── resources/
│   └── application.properties
└── test/

## 👤 Author
---
Reem Ooka

Full Stack Java Developer | AI/ML Researcher | MSCS Graduate


## 📜 License
---
This project is licensed under the Boost Software License 
