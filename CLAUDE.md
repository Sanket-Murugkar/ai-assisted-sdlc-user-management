# ai-assisted-sdlc-user-management

Quarkus 3 REST service (Java 21, Gradle Groovy DSL) backed by DynamoDB.

- Build and unit tests: `./gradlew test` (Windows: `gradlew.bat test`).
- Unit tests must not need Docker, DynamoDB or AWS credentials. Tests that do are
  integration tests: name them `*IT` or tag them `@Tag("integration")`.

## ai-agent
```yaml
tracker: github
github_project: https://github.com/users/Sanket-Murugkar/projects/7
base: main
```
