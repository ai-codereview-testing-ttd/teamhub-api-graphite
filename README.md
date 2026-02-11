# TeamHub API

A RESTful backend service for the TeamHub project management platform, built with Java 21 and Vert.x. Provides async, non-blocking APIs for managing projects, tasks, team members, and organization settings.

## Tech Stack

- **Language**: Java 21
- **Framework**: Vert.x 4.5 (async, non-blocking)
- **Database**: MongoDB 7.0
- **Auth**: JWT (Nimbus JOSE)
- **Build**: Maven
- **Testing**: JUnit 5 + Mockito

## Architecture

```
Handler → Manager → Repository → MongoDB
```

- **Handlers**: HTTP layer — parse requests, send responses
- **Managers**: Business logic — validation, orchestration
- **Repositories**: Data access — MongoDB queries via Vert.x MongoClient

## Project Structure

```
src/main/java/com/teamhub/
  MainVerticle.java          # Entry point, wires everything together
  config/                    # AppConfig (application constants)
  common/                    # AppException, ErrorCode, MongoRepository base
  models/                    # Domain models (Project, Task, Member, Organization, BillingPlan)
  repositories/              # Data access layer
  managers/                  # Business logic layer
  handlers/                  # HTTP handlers
  middleware/                # Auth, error handling, security headers
  routes/                    # API router
  utils/                     # Pagination, JWT, crypto, validation helpers
src/test/java/com/teamhub/  # Test suites
```

## Getting Started

```bash
# Start MongoDB
docker-compose up -d

# Build and test
mvn clean compile
mvn test

# Run the server
mvn exec:java       # Starts on http://localhost:8080
```

## API Endpoints

All routes are mounted at `/api/v1/`:

| Method | Path | Description |
|--------|------|-------------|
| GET/POST | `/projects` | List/create projects |
| GET/PUT/DELETE | `/projects/:id` | Project CRUD |
| POST | `/projects/:id/archive` | Archive project |
| GET/POST | `/tasks` | List/create tasks |
| GET/PUT/DELETE | `/tasks/:id` | Task CRUD |
| PATCH | `/tasks/:id/status` | Update task status |
| GET | `/members` | List members |
| POST | `/members/invite` | Invite member |
| PUT | `/members/:id/role` | Update member role |
| GET/PUT | `/organizations/:id` | Organization CRUD |
| GET | `/analytics/dashboard` | Dashboard stats |
| GET | `/billing/plan` | Current billing plan |
| GET | `/health` | Health check |

## Configuration

The application uses hardcoded development defaults in `AppConfig`. For production, these should be externalized to environment variables or a config file.

## Contributing

1. Create a feature branch from `main`
2. Make your changes and ensure tests pass (`mvn test`)
3. Ensure the build compiles cleanly (`mvn clean compile`)
4. Open a pull request
