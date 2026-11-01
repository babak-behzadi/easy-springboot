# Easy Spring Boot — Web module

The `web` module provides `BaseController<E, ID, F>`, an abstract Spring MVC controller that reuses the JPA module's service and result-mapping functionality and the Security module's method-authorization helper. See the [root README](../README.md) for the repository overview and module build order.

## Requirements

- JDK 17 or later.
- Maven.
- The `easy.springboot:jpa:1.0.0` and `easy.springboot:security:1.0.0` artifacts from this repository.
- A Spring Boot application configured with Spring MVC, Spring Security authentication, method security, and a database appropriate to the application.

## Build

The Web module depends on both JPA and Security as Maven artifacts. From the repository root, install both first and then build Web:

```bash
mvn -f jpa/pom.xml clean install
mvn -f security/pom.xml clean install
mvn -f web/pom.xml clean verify
```

The repository has independent module POMs rather than a root aggregator POM. The Web build expects the matching JPA and Security artifacts to be available in your local Maven repository.

## Security setup

The inherited `find` and `query` methods are marked with the Security module's `@PreAuthorizeBaseEndpoint` annotation. The Security module's `BaseSecurityConfiguration` enables Spring method security.

Declare authorities for each inherited endpoint on the concrete controller with `@BaseEndpointsAuthorityRestriction` and nested `@BaseEndpoint` annotations:

```java
import easy.springboot.security.restriction.BaseEndpoint;
import easy.springboot.security.restriction.BaseEndpointsAuthorityRestriction;
import easy.springboot.web.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("person")
@BaseEndpointsAuthorityRestriction({
    @BaseEndpoint(value = "find", authorities = {"USER"}),
    @BaseEndpoint(value = "query", authorities = {"USER"})
})
public class PersonController
        extends BaseController<Person, Long, PersonFilter> {
    // Implement the service accessor required by BaseController.
}
```

Authority names must exactly match values supplied by the application's `GrantedAuthority` instances. The current advice requires the authenticated user's granted-authority set to contain all authorities declared for the matched endpoint. If no matching endpoint declaration exists, access is denied.

This module does not configure the application's authentication provider, users, login, or identity provider. Configure and test authentication and the HTTP security policy in the consuming application.

## Provided endpoints

When a concrete controller is registered with a class-level request mapping, the base controller provides these endpoints relative to that mapping:

| Method | Path | Request | Behavior |
|---|---|---|---|
| `GET` | `/find?id=...` | ID as query parameter | Finds an entity and returns the fields selected by `@QueryResultField`; responds with 404 if the ID is absent. |
| `POST` | `/query` | Filter model as JSON body | Runs the dynamic filter and returns a `QueryResult` whose rows are mapped to response maps. |

For example, if the concrete controller uses `@RequestMapping("person")`, the routes are `GET /person/find?id=1` and `POST /person/query`.

## Example: extend the base controller

```java
import easy.springboot.security.restriction.PreAuthorizeBaseEndpoint;
import easy.springboot.web.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("person")
@PreAuthorizeBaseEndpoint({"ROLE_USER"})
public class PersonController
        extends BaseController<Person, Long, PersonFilter> {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @Override
    protected PersonService getService() {
        return service;
    }
}
```

In this example, `PersonService` must extend `BaseService<Person, Long, PersonRepository>`, and `PersonFilter` must extend `FilterModel`. The concrete service, repository, entity, and controller must all be registered in the consuming Spring application.

A controller can also declare application-specific endpoints, such as a save endpoint that accepts a request DTO. The base controller does not define a generic save endpoint.

## Query request example

If your filter class has a `firstName` property annotated with `@FilterField(value = "firstName", operand = FilterOperand.LIKE_CI)`, a request could look like:

```http
POST /person/query
Content-Type: application/json

{
  "firstName": "bab",
  "page": 0,
  "pageSize": 10
}
```

The response contains `page`, `pageSize`, `total`, and `rows`. Exact JSON date formats depend on your application's Jackson configuration.

## Recommendations for consuming applications

- Add Bean Validation to request models and validate page/filter inputs.
- Use explicit request and response DTOs for stable public APIs.
- Add consistent exception handling with `@ControllerAdvice`.
- Avoid exposing sensitive entity fields; carefully review `@QueryResultField` and the JPA module's protected-column configuration.
- Test authorization with MockMvc, including anonymous users, authenticated users with and without required authorities, not-found cases, invalid requests, and empty query results.

## Current limitations

The base controller intentionally provides only basic find and query endpoints. It does not provide save/update/delete HTTP endpoints, application-specific request validation, or a standardized error-response schema. Authorization depends on correct Spring method-security and application security configuration.

## License

Licensed under the [Apache License, Version 2.0](../LICENSE).

Copyright 2026 Babak Behzadi.
