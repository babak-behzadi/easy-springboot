# Easy Spring Boot

Reusable building blocks for Spring Boot applications using Spring Data JPA and Spring MVC.

The repository currently contains two independent Maven projects:

- **`jpa`** — generic repository and service base classes, annotation-driven dynamic filters, query-result mapping, and reflection utilities.
- **`web`** — a generic Spring MVC controller that exposes find-by-ID and query endpoints using the JPA module.

> **Project status:** This project is under development. The modules have example integration tests, but the API and behavior may change. Review the limitations below before using it in production.

## Features

### JPA module

- `BaseRepository<E, ID>` extends Spring Data JPA's `JpaRepository` and `JpaSpecificationExecutor`.
- `BaseService<E, ID, R>` provides common find, save, delete, query, and result-mapping operations.
- `FilterModel` builds a JPA `Specification` from fields annotated with `@FilterField`.
- `QueryResult<R>` represents query rows together with page, page-size, and total-count metadata.
- `EntityMutator` maps entity fields annotated with `@QueryResultField` to a response-friendly `Map<String, Object>`.
- Reflection utilities and specific exceptions support the library internals.

See [the JPA module guide](jpa/README.md) for examples and supported filter operands.

### Web module

- `BaseController<E, ID, F>` provides:
  - `GET /find?id=...` — look up an entity by ID and return its mapped fields.
  - `POST /query` — apply a filter model and return mapped query results.

Applications extend the base controller and supply their own entity, filter model, and service. See [the Web module guide](web/README.md).

## Requirements

- JDK 17 or later.
- Maven.
- A database and JDBC driver configured by the consuming application for runtime use.

The POMs currently use Spring Boot `4.1.1`. Check the module POMs if you need to align versions with your application.

## Build

The modules are separate Maven projects; there is no root aggregator POM at present. The Web module depends on the JPA artifact, so install JPA locally before building Web.

From the repository root:

```bash
# Build and install the JPA module into your local Maven repository
mvn -f jpa/pom.xml clean install

# Build and test the Web module against the installed JPA artifact
mvn -f web/pom.xml clean verify
```

Run only the JPA module tests with:

```bash
mvn -f jpa/pom.xml clean verify
```

The modules include H2 as a test dependency. Their tests use test-specific configuration; configure a production database separately in the application that consumes the library.

## Repository layout

```text
.
├── jpa/
│   ├── src/main/java/easy/springboot/jpa/
│   ├── src/test/
│   └── README.md
└── web/
    ├── src/main/java/easy/springboot/web/
    ├── src/test/
    └── README.md
```

## Important limitations

- `BaseService.query()` currently caps page size at 10 and does not apply the `Sort` value held by `FilterModel`.
- `queryAll()` loads all matching rows into memory. Avoid it for large result sets.
- Nested filter paths use inner joins, so rows without the related entity may not match.
- Dynamic filters should only expose fields and operands that your application intends clients to query. Validate and constrain request data.
- `EntityMutator` is a lightweight field mapper, not a substitute for explicit API DTOs. Review lazy-loaded relationships, null relationships, recursion/cycles, and sensitive-field handling for your domain.
- The Web base controller provides only basic find and query endpoints. Applications should add validation, authorization, error handling, and API-specific DTOs as needed.
- A `pom.xml` for a root multi-module Maven build, CI workflow, license, and release/publishing instructions have not yet been added.

## Contributing

Issues and pull requests are welcome. Please include tests for behavior changes and verify both modules with the commands above when changing shared APIs.

## License

No license has been declared in this repository yet. Until a license is added, do not assume the code is available for unrestricted reuse.
