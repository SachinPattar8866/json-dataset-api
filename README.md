
# JSON Dataset API

A Spring Boot REST service that stores JSON records in named datasets (PostgreSQL) and queries them with dynamic **group-by** and **sort-by**.

- **Live URL:** https://json-dataset-api-fyvr.onrender.com
- **Swagger UI:** https://json-dataset-api-fyvr.onrender.com/swagger-ui.html
- **OpenAPI JSON:** https://json-dataset-api-fyvr.onrender.com/v3/api-docs

> Hosted on Render's free tier, so the first request after inactivity can take about a minute while the service wakes up.

## Tech stack
Java 17, Spring Boot 3.3, Spring Data JPA (Hibernate), PostgreSQL (JSON column), springdoc-openapi (Swagger), JUnit 5, Mockito, H2 (tests only), Docker.

## Design
- **Layers:** `controller` (REST only) -> `service` (validation and orchestration) -> `repository` (Spring Data JPA).
- **Operators:** `GroupByOperator` and `SortByOperator` are small single-purpose classes (strategy-style), so new operators can be added without changing the service flow. `JsonValueComparator` sorts numbers numerically (9 < 10) and strings case-insensitively. Records missing the sort field always go last.
- **Errors:** one `@RestControllerAdvice` (`GlobalExceptionHandler`) returns the same error body for every failure.
- **Storage:** all records live in one table, `dataset_records`, with a JSON column for the record and a unique key on `(dataset_name, record_key)`. `record_key` is the record's `id`, so the same `id` can exist in different datasets. Grouping and sorting run in the service layer, which keeps the logic database-independent and unit-testable.

## Run locally
Requires Java 17+ and Docker.

```bash
docker compose up -d                 # PostgreSQL on port 5432
./mvnw spring-boot:run               # Windows: .\mvnw.cmd spring-boot:run
```
The app starts on http://localhost:8080 and Swagger UI is at http://localhost:8080/swagger-ui.html.

Optional environment variables (defaults in brackets):

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/datasetdb` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` |
| `PORT` | `8080` |

## Run tests
```bash
./mvnw test                          # Windows: .\mvnw.cmd test
```
28 tests: unit tests for the operators and service (Mockito) plus integration tests for every endpoint and error case (MockMvc with in-memory H2, no database needed).

## API

### 1. Insert record
`POST /api/dataset/{datasetName}/record` returns `201 Created`

```bash
curl -X POST "https://json-dataset-api-fyvr.onrender.com/api/dataset/employees/record" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"John Doe","age":30,"department":"Engineering"}'
```
```json
{
  "message": "Record added successfully",
  "dataset": "employees",
  "recordId": 1
}
```
Rules:
- The body must be one JSON object with an `id` (number or string), unique within the dataset.
- Dataset names may contain letters, digits, `_` and `-` (max 100 characters).

### 2. Query (group-by / sort-by)
`GET /api/dataset/{datasetName}/query`

| Parameter | Required | Description |
|---|---|---|
| `groupBy` | one of `groupBy` / `sortBy` | field to group by |
| `sortBy` | one of `groupBy` / `sortBy` | field to sort by |
| `order` | no | `asc` (default) or `desc` |

**Group-by**
```bash
curl "https://json-dataset-api-fyvr.onrender.com/api/dataset/employees/query?groupBy=department"
```
```json
{
  "groupedRecords": {
    "Engineering": [
      { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" },
      { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" }
    ],
    "Marketing": [
      { "id": 3, "name": "Alice Brown", "age": 28, "department": "Marketing" }
    ]
  }
}
```

**Sort-by**
```bash
curl "https://json-dataset-api-fyvr.onrender.com/api/dataset/employees/query?sortBy=age&order=asc"
```
```json
{
  "sortedRecords": [
    { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" },
    { "id": 3, "name": "Alice Brown", "age": 28, "department": "Marketing" },
    { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
  ]
}
```

**Both together:** records are sorted first and then grouped, so every group comes back sorted.
```
GET /api/dataset/employees/query?groupBy=department&sortBy=age&order=asc
```

Note: key order inside each returned record may differ from the order it was inserted, because JSON objects are unordered.

### Error responses
Every error has the same shape:
```json
{
  "timestamp": "2026-11-21T09:13:04.020Z",
  "status": 409,
  "error": "Conflict",
  "message": "Record with id '1' already exists in dataset 'employees'",
  "path": "/api/dataset/employees/record"
}
```

| Status | When |
|---|---|
| 400 | malformed JSON or extra content after the JSON object, missing `id`, invalid dataset name, invalid `order`, unknown `groupBy`/`sortBy` field, neither `groupBy` nor `sortBy` given |
| 404 | the dataset has no records |
| 409 | duplicate `id` within the dataset |
| 500 | unexpected server error |

## Testing with Postman
Import the API definition into Postman (**Import**, then paste `https://json-dataset-api-fyvr.onrender.com/v3/api-docs`) to get a ready-made collection. For POST requests, set the header `Content-Type: application/json` and send one JSON object per request.

## Deployment
- App: Render Web Service (Docker runtime, built from the `Dockerfile` in this repo).
- Database: Neon PostgreSQL.
- Configuration is passed as environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`). No credentials are stored in the repository.
- The JVM timezone is set to UTC at startup, because Postgres rejects legacy timezone ids such as `Asia/Calcutta`.

## Project structure
```
src/main/java/com/datasetapi
  controller/   DatasetController
  service/      DatasetService
  operator/     GroupByOperator, SortByOperator, JsonValueComparator, SortOrder
  repository/   DatasetRecordRepository
  model/        DatasetRecord
  dto/          InsertRecordResponse, QueryResponse, ErrorResponse
  exception/    GlobalExceptionHandler and custom exceptions
src/test/java/com/datasetapi   unit and integration tests
```
