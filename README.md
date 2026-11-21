# JSON Dataset API

Spring Boot service to store JSON records in named datasets (PostgreSQL) and query them with dynamic **group-by** and **sort-by**.

- **Live URL:** `<your-render-url>`
- **Swagger UI:** `<your-render-url>/swagger-ui.html`

## Tech
Java 17, Spring Boot 3.3, Spring Data JPA, PostgreSQL (JSON column), springdoc-openapi, JUnit 5, Mockito, H2 (tests only).

## Design
- `controller` - REST layer only; `service` - validation and orchestration; `repository` - Spring Data JPA.
- `operator` - `GroupByOperator` and `SortByOperator` are small single-purpose strategies, so a new operator can be added without touching the service flow. `JsonValueComparator` compares numbers numerically, strings case-insensitively; records missing the field are always last.
- `exception` - one `@RestControllerAdvice` returns a consistent error body for every failure.
- Records are stored as JSON in one table (`dataset_records`) with a unique key on `(dataset_name, record_key)`, where `record_key` is the record's `id`. Grouping and sorting run in the service layer, which keeps the logic database-independent and unit-testable.

## Run locally
```bash
docker compose up -d        # PostgreSQL on :5432
mvn spring-boot:run         # app on :8080
```
Env vars (defaults in brackets): `SPRING_DATASOURCE_URL` (`jdbc:postgresql://localhost:5432/datasetdb`), `SPRING_DATASOURCE_USERNAME` (`postgres`), `SPRING_DATASOURCE_PASSWORD` (`postgres`), `PORT` (`8080`).

## Run tests
```bash
mvn test
```

## API

### Insert record
`POST /api/dataset/{datasetName}/record` -> `201 Created`
```bash
curl -X POST localhost:8080/api/dataset/employee_dataset/record \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"John Doe","age":30,"department":"Engineering"}'
```
```json
{ "message": "Record added successfully", "dataset": "employee_dataset", "recordId": 1 }
```
The record needs an `id` (number or string), unique within the dataset. Dataset names: letters, digits, `_`, `-`.

### Query
`GET /api/dataset/{datasetName}/query`

| Param | Description |
|---|---|
| `groupBy` | field to group by |
| `sortBy` | field to sort by |
| `order` | `asc` (default) or `desc` |

```bash
curl "localhost:8080/api/dataset/employee_dataset/query?groupBy=department"
curl "localhost:8080/api/dataset/employee_dataset/query?sortBy=age&order=asc"
```
- `groupBy` -> `{ "groupedRecords": { "Engineering": [...], "Marketing": [...] } }`
- `sortBy` -> `{ "sortedRecords": [...] }`
- Both together: records are sorted first, then grouped, so every group is sorted.

### Errors
Every error has the shape `{ timestamp, status, error, message, path }`.

| Status | When |
|---|---|
| 400 | malformed JSON, missing `id`, bad dataset name, invalid `order`, unknown `groupBy`/`sortBy` field, no query params |
| 404 | dataset has no records |
| 409 | duplicate `id` in the dataset |
| 500 | unexpected error |

## Deploy (Render + Neon, free tiers)
1. Create a Neon Postgres DB. Convert its connection string `postgresql://USER:PASS@HOST/DB?sslmode=require` into `SPRING_DATASOURCE_URL=jdbc:postgresql://HOST/DB?sslmode=require`, plus `SPRING_DATASOURCE_USERNAME=USER` and `SPRING_DATASOURCE_PASSWORD=PASS`.
2. Push this repo to GitHub, create a Render **Web Service** from it (runtime: Docker) and add the three env vars.
3. Open `<render-url>/swagger-ui.html`.
