# Easy Spring Boot

Reusable building blocks for Spring Boot applications using Spring Data JPA, Spring MVC, and Spring Security.

**Author:** Babak Behzadi

Easy Spring Boot is organized as three independent Maven modules:

- **JPA** — generic repositories and services, annotation-driven dynamic filtering, query-result mapping, and reflection utilities.
- **Security** — authority restrictions for inherited endpoints, request-body injection-pattern checks, and optional multipart file MIME-type validation.
- **Web** — a reusable Spring MVC base controller that combines the JPA service API with the Security module's endpoint restrictions.

> **Status:** This project is under development. Public APIs and behavior may change. The modules are helper libraries, not complete application frameworks; review the limitations and security notes before production use.

## Modules

### JPA

The `jpa` module includes:

- `BaseRepository<E, ID>`, extending Spring Data JPA's `JpaRepository` and `JpaSpecificationExecutor`.
- `BaseService<E, ID, R>` for common find, save, delete, paginated query, and entity-mapping operations.
- `FilterModel` and `@FilterField` for constructing JPA `Specification` predicates from annotated filter properties.
- `QueryResult<R>` for query rows and pagination metadata.
- `EntityMutator` and `@QueryResultField` for mapping selected entity fields into response maps.

See [jpa/README.md](jpa/README.md) for examples and supported operands.

### Security

The `security` module includes:

- `@BaseEndpointsAuthorityRestriction` and `@BaseEndpoint` for declaring required authorities by inherited endpoint name.
- `@PreAuthorizeBaseEndpoint` as the method marker used by the authority-restriction aspect.
- `BaseSecurityConfiguration` for Spring Security filter-chain, CSRF, CORS, and excluded-request configuration.
- `InjectionDetector` for heuristic regular-expression checks for SQL-like, script-like, and HTML-like patterns in request-body bytes.
- `FileChecker` and `FileMimeType` for Apache Tika content-based MIME detection of uploaded file parts.

**Security limitation:** pattern matching is only a heuristic. It is not a substitute for parameterized SQL, contextual output encoding, input validation, a robust HTML sanitizer, or application-specific security controls. See [security/README.md](security/README.md) for the exact scope and configuration behavior.

### Web

The `web` module provides `BaseController<E, ID, F>` with:

- `GET /find?id=...` to find an entity and return fields selected by `@QueryResultField`.
- `POST /query` to apply a filter model and return mapped query results.

A concrete controller supplies the service and request mapping. See [web/README.md](web/README.md) for an example.

## Requirements

- JDK 17 or later.
- Maven.
- Spring Boot version declared in each module POM (currently `4.1.2`).
- A database and JDBC driver configured by the consuming application for JPA use.
- Application-specific authentication and authorization configuration for protected endpoints.

## Build

The modules have separate Maven POMs; there is no root aggregator POM. The Web module depends on the JPA and Security artifacts, so install those modules before building Web.

From the repository root:

```bash
mvn -f jpa/pom.xml clean install
mvn -f security/pom.xml clean install
mvn -f web/pom.xml clean verify
```

Run the individual test suites with:

```bash
mvn -f jpa/pom.xml clean verify
mvn -f security/pom.xml clean verify
mvn -f web/pom.xml clean verify
```

## Security considerations and known limitations

- `InjectionDetector` applies regex heuristics when the wrapped request's `getInputStream()` is read. It does not comprehensively inspect query parameters, servlet parameters, multipart fields, or every possible request representation. Regex checks can produce false positives and false negatives.
- The configured `customPattern` property is present in the injection configuration record, but the current `InjectionDetector` implementation does not use it.
- Do not rely on injection-pattern matching to prevent SQL injection. Use parameterized queries and safe ORM APIs. Encode output for its destination context and sanitize HTML with a purpose-built sanitizer when accepting rich HTML.
- Multipart file checks run through servlet `Part` parsing when `getParts()` is requested. Apache Tika MIME detection does not prove that a file is harmless or structurally valid.
- Set request and file size limits, generate server-side filenames, store uploads safely, and add format-specific validation where needed.
- `BaseService.query()` currently clamps page size to at most 10 and does not apply the `Sort` value in `FilterModel`.
- `queryAll()` loads all matching entities into memory; avoid it for large result sets.
- Nested filter paths use inner joins, which can exclude rows without the related entity.
- `EntityMutator` is a lightweight map mapper, not a replacement for explicit API DTOs. Review lazy loading, null relationships, object cycles, and sensitive-field exposure.
- The base Web controller provides find and query endpoints only. Consuming applications remain responsible for request validation, DTO design, error handling, and endpoint-specific tests.
- Review Spring Security's HTTP rules, authentication mechanism, method security, CSRF/CORS policy, and authority names for your application before production use.

## Contributing

Issues and pull requests are welcome. Include tests for behavior changes and verify each affected module with the commands above.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

Copyright 2026 Babak Behzadi.
