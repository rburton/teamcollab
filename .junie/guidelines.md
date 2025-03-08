# TeamCollab - Project Overview

## Introduction

TeamCollab is a collaborative messaging platform that enables real-time communication between users within
organizations. The application supports multi-tenancy with users belonging to companies, role-based access control, and
AI-powered features.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── ai/teamcollab/server/
│   │       ├── api/              # REST API controllers
│   │       ├── config/           # Application configuration
│   │       ├── controller/       # Web controllers
│   │       ├── domain/           # Domain model entities
│   │       ├── dto/              # Data Transfer Objects
│   │       ├── exception/        # Custom exceptions
│   │       ├── repository/       # Data access layer
│   │       ├── service/          # Business logic
│   │       ├── templates/        # Template-related classes
│   │       └── ws/               # WebSocket-related classes
│   └── resources/
│       ├── db/                   # Database migrations
│       ├── static/               # Static resources
│       ├── templates/            # Thymeleaf templates
│       ├── application.properties # Application configuration
│       └── messages.properties   # Internationalization messages
└── test/
    ├── java/                     # Test classes
    └── resources/                # Test resources
```

## Tech Stack

### Backend

- **Java 23**: Latest Java version for modern language features
- **Spring Boot 3.4**: Framework for building Java applications
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data access layer
- **Spring WebSocket**: Real-time communication
- **Thymeleaf**: Server-side templating engine
- **Lombok**: Reduces boilerplate code
- **PostgreSQL**: Relational database
- **Flyway**: Database migrations
- **Spring AI**: Integration with AI services (OpenAI, Ollama)

### Frontend

- **Stimulus.js**: JavaScript framework for enhancing HTML
- **TailwindCSS**: Utility-first CSS framework
- **Turbo**: Hotwire's approach for modern web applications
- **WebSockets**: Real-time communication

### DevOps & Testing

- **Maven**: Build automation
- **Docker Compose**: Local development environment
- **Testcontainers**: Integration testing with containers
- **JUnit Jupiter**: Testing framework

## Key Components

### Domain Model

- **User**: Represents a user with authentication details and roles
- **Company**: Represents an organization that users belong to
- **Conversation**: A chat or discussion thread
- **Message**: Individual messages within conversations
- **Persona**: Different characters or identities that can be used in conversations

### Architecture

- **Multi-tenant**: Users belong to companies
- **Role-based access**: Users have specific roles and permissions
- **Real-time communication**: WebSockets for instant messaging
- **AI integration**: Spring AI for intelligent features

## Development Guidelines

### Code Organization

1. Place REST controllers in `ai.teamcollab.server.api` package
2. Place web controllers in `ai.teamcollab.server.controller` package
3. Domain classes should be in the `ai.teamcollab.server.domain` package
4. Custom exceptions should be in the `ai.teamcollab.server.exception` package
5. Follow clean architecture principles with clear separation of concerns

### Coding Standards

1. Follow Spring Java Format
2. Use Lombok annotations to reduce boilerplate code
3. Keep functions concise and focused (less than 25 lines)
4. Use descriptive function and variable names
5. Implement comprehensive error handling
6. Add informative logging in critical paths
7. Use `final var` or `var` when possible
8. Use Lombok `@NonNull` annotation to avoid null checks
9. Use Lombok `@Builder` for complex object creation

### Best Practices

1. Implement proper exception handling
2. Use static imports for constants and functions
3. Separate business logic from UI/framework code
4. Keep components focused and single-purpose
5. Follow security best practices
6. Document API changes
7. Before doing any work, create a git feature branch prefixed with 'junie-' and a short description
8. Each change commit and provide a meaningful but concise message.
9. After a feature is completed, squash the feature branch into one commit.

### Anti-patterns to Avoid

1. Unnecessary use of nullable types
2. Excessive use of inheritance over composition
3. Not leveraging Java standard library functions
4. Using mutability when immutability is possible
5. Using Java-style loops instead of functional operations
6. Using `!=` or `== null` (use `Objects.isNull` or `Objects.nonNull` instead)
7. Using wildcard imports
8. Method chaining

## Getting Started

### Prerequisites

- Java 23
- Maven
- Docker and Docker Compose
- Node.js and npm

### Setup

1. Clone the repository
2. Run `docker-compose up -d` to start the database
3. Run `npm install` to install frontend dependencies
4. Run `npm run build` to build frontend assets
5. Run `mvn spring-boot:run` to start the application

### Development Workflow

1. Make changes to Java code
2. For frontend changes, edit files in `src/main/resources/static/js/`
3. Run `npm run build` after frontend changes
4. Restart the application to see changes

## Testing

- Run unit tests with `mvn test`
- Run integration tests with `mvn verify`