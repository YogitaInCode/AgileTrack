# AgileTrack V2 – Full-stack Agile Project & Quality Management

A college SEPM project MVP with project management, Scrum task planning, bug tracking, quality test cases, team roles, activity history and a dashboard.

## V2 improvements
- Sign-in with server-side session and BCrypt password hashing.
- Roles: ADMIN, PROJECT_MANAGER, DEVELOPER, TESTER.
- Project and sprint CRUD; admin-only delete for projects/sprints.
- Backlog and a 3-column Kanban board (To Do / In Progress / Done).
- Story points, task type, assignee and priority.
- Bug severity, priority, assignment, status, comments and timestamps.
- Test cases with expected/actual result, execution status and optional linked bug ID.
- Dashboard metrics: task completion, open/critical bugs, test pass rate and task status counts.
- Team directory and recent activity feed.
- H2 persistent database by default; optional MySQL configuration.
- Responsive desktop/mobile UI.

## Requirements
- JDK 17+
- Apache Maven 3.8+
- VS Code (Java Extension Pack recommended) or IntelliJ IDEA
- Chrome / Edge

Verify:
```bash
java -version
mvn -version
```

## Run
1. Extract the ZIP.
2. Open the `agiletrack-v2` folder in VS Code.
3. Open Terminal → New Terminal.
4. Run:
```bash
mvn spring-boot:run
```
5. Wait for `Started AgileTrackApplication`.
6. Open `http://localhost:8080`.

The app creates a local `data` folder and persists its H2 database there. Demo records and accounts are seeded on the first run.

## Demo login accounts
All four accounts use the temporary demo password `Agile123!`.

| Role | Email |
|---|---|
| Admin | admin@agiletrack.local |
| Project Manager | manager@agiletrack.local |
| Developer | developer@agiletrack.local |
| Tester | tester@agiletrack.local |

This is an academic demo password. Change/remove seeded credentials before any public deployment. The current MVP has no self-service registration or password-reset screen.

## Database console
Open `http://localhost:8080/h2-console`:
- JDBC URL: `jdbc:h2:file:./data/agiletrackdb`
- User: `sa`
- Password: leave blank

## Optional MySQL
1. Create database `agiletrack`.
2. In `src/main/resources/application.properties`, comment out H2 datasource/dialect properties.
3. Uncomment MySQL properties and set your MySQL password.
4. Restart the application.

## Main API routes
- `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`
- CRUD: `/api/projects`, `/api/sprints`, `/api/tasks`, `/api/bugs`, `/api/tests`
- Bug comments: `GET/POST /api/bugs/{id}/comments`
- `GET /api/team`, `GET /api/activity`, `GET /api/dashboard`

## Important project limitations
This is a learning-focused MVP, not production-ready software. It does not yet implement:
- Full enterprise permission rules for every operation (some authenticated roles share access).
- Self-service account creation, password reset, email verification or MFA.
- Real file upload/attachment storage or push/email notifications.
- A true burndown chart computed from daily sprint snapshots, velocity history or release management.
- Full audit diff/version history, advanced filters, pagination or export.
- Comprehensive automated tests, security review, deployment configuration or backups.

Do not deploy publicly with the seeded demo credentials. Use only sample/non-confidential data.

## Recommended next milestone
Add JUnit tests, role-specific UI, burndown snapshots, sprint capacity/velocity, release management, file attachment storage, and link failed test execution directly to defect creation.
