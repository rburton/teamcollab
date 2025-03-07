## Development Guidelines

1. **Code Style**:
    - Follow Spring Java Format
    - Implement comprehensive error handling
    - Add informative logging in critical paths
    - Always use final var or var when possible.
    - Use Lombok annotations to reduce boilerplate code
    - Use Lombok builders when possible
    - Keep functions concise and focused; less than 25 lines.
    - Make function names clear and descriptive
    - Use Lombok @NonNull annotation to avoid null checks
    - Use Lombok @Builder annotation for complex object creation
    - When ever a @Controller requires ADMIN or USER role, use the company id from the authenticated user.
2. **Form Validation**
    - Spring Boot Validation implementation
    - Real-time form validation with comprehensive error handling
    - Age and name field validation with specific rules
    - Internationalized validation messages
3. **Technical Stack**
    - Java 21
    - Spring Boot 3.4
    - Thymeleaf template engine
    - Maven build system
    - JUnit Jupiter for testing
    - TailWind CSS for styling

---

### Special Instructions for Junie

- Place any web @RestController in the `ai.teamcollab.server.api` package to ensure consistency and proper organization.
- Place any web @Controller in the `ai.teamcollab.server.controller` package to ensure consistency and proper organization.

---
### Anti-patterns to Avoid

- Unnecessary use of nullable types
- Excessive use of inheritance over composition
- Not leveraging Java standard library functions
- Using mutability when immutability is possible
- Using Java-style loops instead of functional operations
- Do not use != or == null, instead use Objects.isNull or Objects.nonNull
- Using wild card imports
- method chaining

### Best Practices

1. Implement proper exception handling
2. Use static imports for constants and functions
3. Always put domain classes in a domain package
4. Always put custom exception classes in an exception package
5. Always sort annotations by length of name from smallest to largest
6. Use static imports for constants and functions

### Code Organization

1. Group related functionality into packages
2. Keep files focused and single-purpose
3. Use extension functions to organize utility functions
4. Separate business logic from UI/framework code
5. Follow clean architecture principles

### Internationalization

- All user-facing strings should be externalized
- Use message properties files for translations

## Best Practices

[//]: # (1. Write tests for new features)

2. Keep components focused and small
3. Follow security best practices
4. Document API changes
5. Never change pom.xml without asking