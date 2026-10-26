# Easy Spring Boot

Reusable building blocks for Spring Boot applications using Spring Data JPA, Spring MVC, and Spring Security.

**Author:** Babak Behzadi

The repository currently contains three independent Maven projects:

- **`jpa`** — generic repository and service base classes, annotation-driven dynamic filters, query-result mapping, and reflection utilities.
- **`security`** — an authority-restriction annotation and a service used by Spring method-security expressions.
- **`web`** — a generic Spring MVC controller that exposes find-by-ID and query endpoints using the JPA and Security modules.

> **Project status:** This project is under development. The modules have example integration tests, but the API and behavior may change. Review the limitations below before using it in production.

## Features

### JPA module

- `BaseRepository<E, ID>` extends Spring Data JPA's `JpaRepository` and `JpaSpecificationExecutor`.
- `BaseService<E, ID, R>` provides common find, save, delete, query, and result-mapping operations.
- `FilterModel` builds a JPA `Specification` from fields annotated with `@FilterField`.
- `QueryResult<R>` represents query rows together with page, page-size, and total-count metadata.
- `EntityMutator` maps entity fields annotated with `@QueryResultField` to a response-friendly `Map<String, Object>`.

See [the JPA module guide](jpa/README.md) for examples and supported filter operands.

### Security module

- `@AuthorityRestriction` declares the authorities allowed to access a controller class.
- `AuthorityRestrictionService` checks the current authentication and granted authorities.
- The consuming application must enable method security and configure authentication and HTTP security rules.

See [the Security module guide](security/README.md) for setup, examples, and security limitations.

### Web module

- `BaseController<E, ID, F>` provides:
  - `GET /find?id=...` — look up an entity by ID and return its mapped fields.
  - `POST /query` — apply a filter model and return mapped query results.
- The base endpoints use Spring Security method authorization through the Security module.

Applications extend the base controller and supply their own entity, filter model, and service. See [the Web module guide](web/README.md).

## Requirements

- JDK 17 or later.
- Maven.
- A database and JDBC driver configured by the consuming application for runtime use.
- Authentication and authorization configuration in consuming applications that use the Security or Web modules.

The POMs currently use Spring Boot `4.1.2`. Check each module POM if you need to align versions with your application.

## Build

The modules are separate Maven projects; there is no root aggregator POM. Web depends on both JPA and Security, so install those artifacts locally before building Web.

From the repository root:

```bash
mvn -f jpa/pom.xml clean install
mvn -f security/pom.xml clean install
mvn -f web/pom.xml clean verify
```

Run individual module tests with:

```bash
mvn -f jpa/pom.xml clean verify
mvn -f security/pom.xml clean verify
```

The JPA and Web modules include H2 as a test dependency. Configure a production database separately in the application that consumes the library.

## Repository layout

```text
.
├── jpa/
│   ├── src/main/java/
│   └── README.md
├── security/
│   ├── src/main/java/
│   └── README.md
└── web/
    ├── src/main/java/
    └── README.md
```

## Important limitations

- `BaseService.query()` currently caps page size at 10 and does not apply the `Sort` value held by `FilterModel`.
- `queryAll()` loads all matching rows into memory. Avoid it for large result sets.
- Nested filter paths use inner joins, so rows without the related entity may not match.
- Dynamic filters should only expose fields and operands that your application intends clients to query. Validate and constrain request data.
- `EntityMutator` is a lightweight field mapper, not a substitute for explicit API DTOs. Review lazy-loaded relationships, null relationships, recursion/cycles, and sensitive-field handling for your domain.
- The Security module only supplies a method-security helper. It does not configure authentication or the HTTP security filter chain for your application.
- The Web base controller provides only basic find and query endpoints. Applications should add validation, application-specific error handling, DTOs, and endpoint tests as needed.
- A root multi-module Maven build, CI workflow, and release/publishing instructions have not yet been added.

## Contributing

Issues and pull requests are welcome. Please include tests for behavior changes and verify the affected modules with the commands above when changing shared APIs.

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).

Copyright 2026 Babak Behzadi.
