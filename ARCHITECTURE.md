# Architecture Guide

## Purpose

This document defines the repository conventions that should be preserved when adding or changing features.
It exists to keep the current folder structure, architectural boundaries, and test strategy stable.

If `plan.md` exists in the future, treat it as task-ordering guidance.
This file defines the implementation shape that should remain consistent.

As of 2026-03-15, `plan.md` was not present in this repository root.

## Project Shape

- The real application lives in the `task` module.
- Root-level `local-infra` contains local database and container setup.
- Root-level `doc` contains architecture images and supporting documentation.
- Do not flatten the module layout unless the user explicitly asks for a structural change.

## Architecture Baseline

This project currently follows a hexagonal architecture with Spring-specific code kept at the edge.

### Main package roles

- `com.newy.task.common`: shared validation and error types.
- `com.newy.task.spring`: Spring MVC, auth, config, database detection, and global exception handling.
- `com.newy.task.<domain>`: business domains such as `task` and `notification`.

### Required subpackage pattern for each domain

Keep this layout when extending an existing domain or creating a new one:

- `adapter.in`
- `adapter.in.web`
- `adapter.in.web.model`
- `adapter.out`
- `adapter.out.internal_system`
- `adapter.out.persistence`
- `adapter.out.persistence.jpa`
- `adapter.out.persistence.jpa.model`
- `adapter.out.persistence.querydsl`
- `adapter.out.persistence.querydsl.model`
- `domain`
- `port.in`
- `port.in.model`
- `port.out`
- `service`

Do not collapse these responsibilities into fewer packages just because the feature is small.

## Responsibility Rules

### Web adapter

- Controllers should stay thin.
- Convert web requests to InPort models.
- Call InPort interfaces, not concrete services.
- Convert domain responses to web responses.
- Keep Spring MVC annotations here.

Reference example:
- `task/src/main/kotlin/com/newy/task/task/adapter/in/web/TaskController.kt`

### InPort model

- Input validation belongs in `port.in.model`.
- String-to-enum and string-to-date conversion belongs here.
- Normalize request input here when needed.
- Domain model conversion belongs here.

Reference examples:
- `task/src/main/kotlin/com/newy/task/task/port/in/model/CreateTaskCommand.kt`
- `task/src/main/kotlin/com/newy/task/task/port/in/model/UpdateTaskCommand.kt`
- `task/src/main/kotlin/com/newy/task/task/port/in/model/SearchTaskQuery.kt`

### Service

- Services implement InPort interfaces.
- Services orchestrate business rules and OutPort calls.
- Transaction boundaries belong here.
- Keep framework-independent business flow here rather than in controllers or adapters.

Reference example:
- `task/src/main/kotlin/com/newy/task/task/service/TaskService.kt`

### Persistence adapter

- Persistence adapters implement OutPort interfaces.
- JPA entities must stay inside persistence layers.
- Domain objects must be returned from adapters, not entities.
- Querydsl-specific query code should stay in `adapter.out.persistence.querydsl`.

Reference examples:
- `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/TaskAdapter.kt`
- `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/SearchTaskAdapter.kt`

### Spring infrastructure

- Authentication, argument resolvers, config, schedulers, and exception handlers belong under `com.newy.task.spring` or the relevant incoming adapter package.
- Do not move business decisions into these infrastructure classes.

## Feature Extension Rules

When changing behavior, prefer this sequence:

1. Add or update the smallest relevant test first.
2. Change only the layer required for that behavior.
3. Keep the same mapping direction: web model -> InPort model -> domain/service -> OutPort -> adapter.
4. Refactor only after tests pass.

When adding a new domain capability:

1. Reuse an existing domain package if the concept is clearly part of that domain.
2. Create a new domain package only when the concept has its own language, ports, and adapters.
3. If a new domain package is created, mirror the existing package pattern instead of inventing a new layout.

## Existing Behavioral Couplings To Preserve

These are important current behaviors. Do not break them accidentally.

- Task create/update flows trigger search index maintenance.
- Task create/update flows can create assignment notifications.
- Task search uses a separate `task_full_text_search` table.
- Notification sending uses an outbox-style flow with deduplication by `event_key`.
- Authentication for protected APIs is based on the `X-User-Id` request header.
- Global request/validation errors are normalized through the global exception handler.

If you change any of these, update both code and tests together.

## Test Strategy Baseline

Preserve the current test split instead of moving everything into one style.

### Unit tests

Use unit tests for:

- incoming web adapters
- services/use cases
- InPort models
- domain rules
- schedulers and small infrastructure logic

What unit tests should verify:

- parameter mapping
- response mapping
- validation
- business rules
- OutPort call arguments
- call ordering when sequencing matters
- required Spring annotations on public entry points

Reference examples:

- `task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt`
- `task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt`
- `task/src/test/kotlin/com/newy/task/unit/task/port/in/model/CreateTaskCommandTest.kt`

### Integration tests

Use integration tests for:

- persistence adapters
- Querydsl queries
- Liquibase/JPA mapping behavior
- auth interceptor behavior
- global exception handling

Reference examples:

- `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/TaskAdapterTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/SearchTaskAdapterTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/notification/out/persistence/NotificationAdapterTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/spring/auth/FakeLoginTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/spring/error/GlobalExceptionHandlerTest.kt`

### Test placement rule

- Mirror production package structure under `src/test/kotlin`.
- Keep `unit` and `integration` separated.
- If production code lives under `task/.../service`, the first choice is a matching service test package, not a mixed catch-all test file.

## Running Tests

Use these commands from the repository root:

```bash
cd task && ./gradlew test
```

For focused work, narrower test runs are acceptable during development, but before finishing a change run the full non-long-running suite used by this repository:

```bash
cd task && ./gradlew test
```

As of 2026-03-15, the full suite passes locally with this command.

## Database and Search Notes

- Development defaults to PostgreSQL.
- Tests default to MySQL.
- Liquibase manages schema creation.
- Full-text search behavior is database-specific, so DB-sensitive changes should keep both MySQL and PostgreSQL behavior in mind.
- Testcontainers is the expected integration-test path.

Reference examples:

- `task/src/main/resources/application.properties`
- `task/src/test/resources/application.properties`
- `task/src/test/kotlin/com/newy/task/integration/config/TestcontainersConfiguration.kt`

## Change Guardrails

Avoid these changes unless the user explicitly asks for them:

- replacing the package layout with a layered package-by-technology structure
- moving validation from InPort models into controllers
- returning JPA entities beyond persistence adapters
- bypassing ports to call adapters directly from controllers
- replacing persistence integration tests with only mocks
- removing annotation-verification tests from controllers/services
- merging all database logic into services

## Practical Notes For Future Agents

- Prefer minimal behavioral changes.
- Preserve existing naming style and Korean test names where the surrounding code already uses them.
- Extend existing test helpers instead of creating parallel helper patterns without a clear reason.
- If you notice documentation drift, fix it only when it directly affects the task or when the user asks.

## Current Improvement Candidates

These are observations, not immediate mandates:

- `CurrentUserArgumentResolver` is registered in two config classes.
- `DatabaseTypeProvider` opens a JDBC connection during initialization and should be checked for proper resource handling before related refactors.
- The global exception handler currently maps broad exceptions to HTTP 400, which may be too wide for production behavior.

Do not change these preemptively during unrelated feature work unless the user asks or the change is required to support the task safely.
