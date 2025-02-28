## Development Guidelines
1. **Code Style**:
    - Follow Spring Java Format
    - Implement comprehensive error handling
    - Add informative logging in critical paths

2. **Testing**
    - Write unit tests for new features
    - Maintain test coverage of at least 75%
    - Include integration tests for critical paths

### Key Features

1. **Form Validation**
    - Spring Boot Validation implementation
    - Real-time form validation with comprehensive error handling
    - Age and name field validation with specific rules
    - Internationalized validation messages
2. **Technical Stack**
    - Java 21
    - Spring Boot 3.4
    - Thymeleaf template engine
    - Maven build system
    - JUnit Jupiter for testing
    - TailWind CSS for styling
   
### Testing Requirements
1. Always run all tests in project to make sure you didn't introduce regression before submitting the task.
2. Use @ParameterizedTest for tests with multiple examples.

### Anti-patterns to Avoid
1. Overuse of `!!` operator
2. Unnecessary use of nullable types
3. Excessive use of inheritance over composition
4. Not leveraging Java standard library functions
5. Using var when val would suffice
6. Using mutability when immutability is possible 
7. Using Java-style loops instead of functional operations 
8. Do not use != or == null, instead use Objects.isNull or Objects.nonNull
9. Using wild card imports

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
1. Write tests for new features
2. Keep components focused and small
3. Follow security best practices
4. Document API changes
5. Never change pom.xml without asking