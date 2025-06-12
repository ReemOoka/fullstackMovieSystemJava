# Movie Management System

## Project Description
This is a full-stack application for managing and reviewing movies. It features a Spring Boot backend providing a RESTful API and a JavaFX client for user interaction. The system supports user authentication, role-based access control, and CRUD operations for movies and reviews.

## Table of Contents
1.  [Team Members](#team-members)
2.  [Prerequisites](#prerequisites)
3.  [Project Setup](#project-setup)
4.  [Running the Application](#running-the-application)
5.  [Running Tests](#running-tests)
6.  [Application Architecture](#application-architecture)
7.  [API Specification](#api-specification)
8.  [Security Model](#security-model)
9.  [Test Strategy](#test-strategy)
10. [Design Decisions & Patterns](#design-decisions--patterns)
11. [Design Rationale](#design-rationale)
12. [Team Reflections](#team-reflections)

## Team Members
*   Nihar Lodaya
*   Reem Ooka
*   Roman Huerta

## Prerequisites
*   Java JDK 21 only (do not use newer versions)
*   Apache Maven 3.6.x or newer
*   MySQL Server 8.x

## Project Setup

1.  **Clone the repository:**
    ```bash
    git clone http://github.com/niharl7/SDDProject/
    cd SDDProject
    ```
2.  **Database Setup:**
    *   Ensure your MySQL server is running.
    *   The application is configured to connect to a MySQL database named `movie_db_javafx`.
    *   **The database will be created automatically if it does not exist** when the backend server starts, due to the `createDatabaseIfNotExist=true` setting in the connection URL.
    *   The application will also attempt to create tables on startup if they don't exist, based on `schema.sql` (if `spring.sql.init.mode=always`) and `spring.jpa.hibernate.ddl-auto` settings.

## Running the Application

### Backend (Server)
The backend is a Spring Boot application.
* Open in CMD (Command Prompt) only.

Then:
1.  Navigate to the server directory:
    ```bash
    cd server
    ```
2.  Run the application using Maven, providing your MySQL credentials:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.arguments="--DB_USERNAME=your_mysql_username --DB_PASSWORD=your_mysql_password"
    ```
    Replace `your_mysql_username` and `your_mysql_password` with your actual MySQL credentials.
    The server will start, typically on port `8080`.

### Frontend (Client)
The frontend is a JavaFX application.
* Open in CMD (Command Prompt) only.v
  
1.  Navigate to the client directory:
    ```bash
    cd client
    ```
2.  Run the application using Maven (ensure your `client/pom.xml` is configured appropriately):
    ```bash
    mvn javafx:run
    ```
3. **Login Details:**  
   Default admin username: `admin`  
   Default admin password: `admin123`



## Running Tests
* Open in CMD (Command Prompt) only.
  
1.  Navigate to the server directory:
    ```bash
    cd server
    ```
2.  Run the backend tests using Maven. If your tests are configured to run against a live MySQL database (as opposed to an in-memory one like H2), provide your MySQL credentials:
    ```bash
    mvn clean test -DDB_USERNAME=your_mysql_username -DDB_PASSWORD=your_mysql_password
    ```
    Replace `your_mysql_username` and `your_mysql_password` with your actual MySQL credentials.


## Application Architecture

The application follows a classic client-server architecture:

*   **Frontend (Client):**
    *   Built with JavaFX.
    *   Handles user interface and interaction.
    *   Communicates with the backend via RESTful API calls.
    *   Implements the Model-View-Controller (MVC) pattern for UI organization.
*   **Backend (Server):**
    *   Built with Spring Boot.
    *   Provides a RESTful API for data management and business logic.
    *   Uses Spring MVC for handling web requests.
    *   Employs a layered architecture:
        *   **Controller Layer (`com.example.controller`):** Handles incoming HTTP requests, delegates to services, and returns responses.
        *   **Service Layer (`com.example.service`):** Contains business logic, orchestrates calls to repositories.
        *   **Repository Layer (`com.example.repository`):** Manages data persistence using Spring Data JPA and interacts with the database.
        *   **Model Layer (`com.example.model`):** Defines JPA entities (e.g., `User`, `Movie`, `Review`).
*   **Database:**
    *   MySQL is used for persistent data storage.

## API Specification

The backend exposes the following primary REST endpoints. All paths are prefixed with `/api`.

*   **Authentication (`/api/auth`):**
    *   `POST /api/auth/register`: User registration.
    *   `POST /api/auth/login`: User login and session creation.
    *   `POST /api/auth/logout`: User logout.
*   **Movies (`/api/movies`):**
    *   `GET /api/movies`: Retrieve all movies.
    *   `GET /api/movies/{id}`: Retrieve a specific movie by ID.
    *   `POST /api/movies`: Create a new movie (Admin only).
    *   `PUT /api/movies/{id}`: Update an existing movie (Admin only).
    *   `DELETE /api/movies/{id}`: Delete a movie (Admin only).
*   **Reviews (`/api/reviews` and `/api/movies/{movieId}/reviews`):**
    *   `GET /api/movies/{movieId}/reviews`: Get all reviews for a specific movie.
    *   `POST /api/movies/{movieId}/reviews`: Create a new review for a movie.
    *   `PUT /api/reviews/{reviewId}`: Update a review (Owner or Admin).
    *   `DELETE /api/reviews/{reviewId}`: Delete a review (Owner or Admin).
*   **Admin (`/api/admin`):**
    *   `GET /api/admin/users`: Get all users (Admin only).
    *   `DELETE /api/admin/users/{userId}`: Delete a user (Admin only).
    *   `PUT /api/admin/users/{userId}/roles`: Update user roles (Admin only).


## Security Model

*   **Framework:** Spring Security.
*   **Authentication:** Session-based authentication. Upon successful login, a session is established, and subsequent requests are authenticated using the session cookie.
*   **Authorization:** Role-Based Access Control (RBAC).
    *   **Roles:** `ROLE_USER`, `ROLE_ADMIN`.
    *   Endpoints are secured using `@PreAuthorize` annotations and security configurations in `SecurityConfig.java`.
*   **Password Management:** Passwords are hashed using `PasswordEncoder` (e.g., BCrypt) before being stored in the database.
*   **CSRF Protection:** Enabled by default in Spring Security for stateful applications.
*   **Session Management:** Configured with timeouts and HttpOnly cookies.

## Test Strategy

*   **Backend:**
    *   **Unit Tests:** Focus on individual components (services, utility classes).
        *   Mockito is used for mocking dependencies (e.g., `UserDetailsServiceImplTests`).
    *   **Integration Tests:** Test interactions between components, including controller-service-repository layers and API endpoints.
        *   `@SpringBootTest` is used to load the application context.
        *   `MockMvc` is used for testing REST controllers (`MovieControllerIntegrationTests`).
*   **Frontend:**
    *   *(Manual testing is assumed unless UI test frameworks like TestFX are implemented. If so, describe here.)*

## Design Decisions & Patterns

Several design patterns and principles have been applied:

1.  **Model-View-Controller (MVC):**
    *   **Backend (Spring MVC):** Controllers handle HTTP requests, Models represent data (JPA entities), and Views are typically JSON responses (for REST APIs).
    *   **Frontend (JavaFX):** FXML files define the View, Controller classes (`com.example.controller.*` in client) handle UI logic and user input, and Model classes (`com.example.model.*` in client) represent data fetched from the server.
2.  **Repository Pattern (Spring Data JPA):**
    *   Interfaces extending `JpaRepository` (e.g., `MovieRepository`, `UserRepository`) abstract data access logic. Spring Data JPA provides implementations at runtime, simplifying CRUD operations and custom queries.
3.  **Service Layer Pattern:**
    *   Services (e.g., `ReviewService`, `UserDetailsServiceImpl`) encapsulate business logic, acting as an intermediary between controllers and repositories. This promotes separation of concerns and reusability.
4.  **Dependency Injection (DI):**
    *   Leveraged extensively by Spring Boot. Dependencies (e.g., services, repositories, `PasswordEncoder`) are injected into components using `@Autowired`, promoting loose coupling and testability.
5.  **Singleton Pattern:**
    *   Spring beans are singletons by default, ensuring a single instance of services, repositories, etc., is managed by the Spring container.
    *   The JavaFX `Main` class in the client uses a static `instance` for global access, resembling a singleton.
6.  **Data Transfer Object (DTO):**
    *   DTOs (e.g., `LoginRequest`, `ReviewDto`, `UserDto`) are used to transfer data between layers, particularly between controllers and clients, to decouple API contracts from internal domain models and to shape data specifically for views or requests.

## Design Rationale

The architecture aims to balance:

*   **Cohesion:** Functionalities are grouped into logical modules (e.g., `controller`, `service`, `model`, `repository` packages in the backend; distinct FXML controllers in the frontend). Each module/class has a well-defined responsibility.
*   **Modularity:**
    *   The separation of frontend (client) and backend (server) into distinct applications allows for independent development, deployment, and scaling.
    *   Within the backend, the layered architecture (Controller-Service-Repository) ensures clear separation of concerns.
*   **Extensibility:**
    *   Using interfaces for services and repositories (though often implemented directly by Spring Data JPA) allows for easier changes or additions of implementations.
    *   Spring's DI makes it straightforward to add new components or modify existing ones.
    *   The REST API provides a clear contract, allowing for different types of clients (e.g., web, mobile) to be developed in the future.
    *   DTOs help in evolving the API without directly exposing internal domain model changes.

## Team Reflections

### Role-Based Access Decisions
*   **`ROLE_ADMIN`:** Granted extensive permissions, including user management (listing, deleting, changing roles) and full CRUD operations on movies. This is justified as administrative tasks require overarching control. The `DataInitializer` creates a default admin user for initial setup.
*   **`ROLE_USER`:** Granted permissions for standard user activities, such as viewing movies, creating/editing/deleting their own reviews. This ensures users can interact with the system's core features without compromising administrative functions or other users' data.
*   **Public Access:** Certain endpoints like movie listing and registration are generally public or require minimal authentication. Login is, by nature, an unauthenticated entry point.

### Exception Handling Strategy
*   **Backend:**
    *   Spring Boot's default exception handling mechanisms are utilized.
    *   Custom exceptions like `ResourceNotFoundException` are defined for specific business scenarios.
    *   `@ControllerAdvice` and `@ExceptionHandler` can be (or are) used to create global or controller-specific handlers that translate exceptions into appropriate HTTP responses (e.g., 404 for `ResourceNotFoundException`, 400 for bad requests, 401/403 for security issues).
    *   Logging of exceptions is crucial for debugging (using SLF4J).
*   **Frontend:**
    *   Try-catch blocks are used around API calls (`HttpUtil`).
    *   User-friendly error messages are displayed to the user via alerts or UI updates rather than raw stack traces.

### Use and Justification of Design Patterns

1.  **Model-View-Controller (MVC):**
    *   **Use:** Structuring the Spring Boot backend (controllers, services as part of the controller/logic, models, views as responses) and the JavaFX client (FXML for views, Java controllers for logic, client-side models).
    *   **Justification:** Provides a clear separation of concerns, making the application easier to understand, develop, and maintain. It allows UI logic, business logic, and data representation to evolve independently.
2.  **Repository Pattern (via Spring Data JPA):**
    *   **Use:** `UserRepository`, `MovieRepository`, `ReviewRepository` interfaces extend Spring Data JPA interfaces.
    *   **Justification:** Abstracts the data persistence mechanism, decoupling business logic (services) from the specifics of data access (e.g., SQL queries). Spring Data JPA further reduces boilerplate code for common CRUD operations.
3.  **Service Layer Pattern:**
    *   **Use:** Classes like `UserDetailsServiceImpl` and `ReviewService` encapsulate business logic.
    *   **Justification:** Promotes separation of concerns by isolating business rules from web/controller logic and data access logic. This makes services reusable by different controllers or entry points and improves testability of business logic.

### Trade-offs: Complexity vs. Maintainability

*   **Spring Boot Framework:**
    *   **Trade-off:** Reduces boilerplate and provides many auto-configurations (improves maintainability and development speed) but introduces a level of "magic" and a learning curve (initial complexity). Understanding how auto-configurations work is key.
    *   **Decision:** Adopted for its rapid development capabilities and robust ecosystem, deeming the long-term maintainability benefits worth the initial learning investment.
*   **ORM (Hibernate via Spring Data JPA):**
    *   **Trade-off:** Simplifies database interactions and object-relational mapping (improves maintainability for CRUD) but adds a layer of abstraction that can sometimes obscure performance issues or make complex queries harder to optimize (adds complexity).
    *   **Decision:** Chosen for developer productivity and to avoid writing raw SQL for most operations. Performance considerations for complex queries would be addressed as needed.
*   **Role-Based Access Control (RBAC):**
    *   **Trade-off:** Implementing and managing roles and permissions adds complexity to the security configuration and user management logic. However, it significantly improves the maintainability of a secure system by providing a clear and granular way to control access.
    *   **Decision:** Essential for any application with different user types and sensitive operations. The clarity it brings to security rules outweighs the implementation overhead.
*   **Separate Frontend and Backend:**
    *   **Trade-off:** Increases initial setup complexity (two projects, API design, CORS handling if applicable) but greatly enhances modularity and maintainability. Allows teams to work independently and technologies to be chosen/updated separately.
    *   **Decision:** A standard approach for modern web applications, offering flexibility and scalability.
