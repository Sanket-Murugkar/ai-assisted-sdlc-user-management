# user-management-system
RESTful user management service built with Quarkus, Java 21, and Amazon DynamoDB.

## Prerequisites
- **JDK 21** (Gradle uses a Java 21 toolchain)
- **Docker** with Docker Compose, for DynamoDB Local and the containerised app

No AWS account or credentials are needed for local work: everything runs against
[DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html)
with dummy credentials.

## Build
```shell
./gradlew build        # Windows: gradlew.bat build
```
This compiles the app, runs the tests and packages it into `build/quarkus-app/`.
Run only the unit tests with `./gradlew test`; they need no Docker, DynamoDB or AWS credentials.

## Run

### Dev mode (live reload)
```shell
docker compose up -d dynamodb-local   # DynamoDB Local on localhost:8000
./gradlew quarkusDev                  # app on http://localhost:8080, Dev UI at /q/dev-ui
```
The `%dev` profile points the DynamoDB client at `http://localhost:8000` with dummy credentials.

### Docker Compose (app + DynamoDB Local)
The app image copies the packaged app from `build/quarkus-app/`, so build it first:
```shell
./gradlew build
docker compose up --build
```
Stop everything with:
```shell
docker compose down
```
DynamoDB Local runs **in memory** by default, so data is lost on `docker compose down`.
To keep data between restarts, follow the comments in `docker-compose.yml`: switch the
`dynamodb-local` command to `-dbPath` and uncomment the `dynamodb-data` volume. Remove the
volume with `docker compose down -v`.

Compose defaults can be overridden in a `.env` file; copy `.env.example` to start.
Never put real AWS credentials in it.

## Health checks
| Endpoint | Meaning |
|---|---|
| `GET /q/health/live` | The app is running |
| `GET /q/health/ready` | DynamoDB answers `DescribeTable` for the users table. Returns 503 `DOWN` when DynamoDB is unreachable. Until the table is created automatically (MVP-04), a missing table reports `UP` with `table: missing`. |
| `GET /q/health` | Both of the above |

## Configuration
Every key can be overridden by an environment variable (Quarkus maps `a.b-c` to `A_B_C`).

| Key | Env var | Default | Notes |
|---|---|---|---|
| `user-management.dynamodb.table-name` | `USER_MANAGEMENT_DYNAMODB_TABLE_NAME` | `users` | Table used by the app and the readiness check |
| `user-management.dynamodb.create-table` | `USER_MANAGEMENT_DYNAMODB_CREATE_TABLE` | `false` | Set to `true` in Compose; takes effect from MVP-04 |
| `quarkus.dynamodb.endpoint-override` | `QUARKUS_DYNAMODB_ENDPOINT_OVERRIDE` | unset (`http://localhost:8000` in dev and test) | Compose: `http://dynamodb-local:8000` |
| `quarkus.dynamodb.aws.region` | `QUARKUS_DYNAMODB_AWS_REGION` | `us-east-1` | |
| `quarkus.dynamodb.aws.credentials.type` | `QUARKUS_DYNAMODB_AWS_CREDENTIALS_TYPE` | `default` (`static` with `local`/`local` in dev and test) | Prod uses the default AWS credentials chain, e.g. `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` |
| `quarkus.dynamodb.sync-client.type` | `QUARKUS_DYNAMODB_SYNC_CLIENT_TYPE` | `url` | URL connection HTTP client |
| `quarkus.dynamodb.devservices.enabled` | `QUARKUS_DYNAMODB_DEVSERVICES_ENABLED` | `false` | Quarkus never starts its own DynamoDB container |
| `quarkus.log.console.json.enabled` | `QUARKUS_LOG_CONSOLE_JSON_ENABLED` | `true` (`false` in dev and test) | JSON console logs |
