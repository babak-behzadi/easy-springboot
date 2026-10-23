# Easy Spring Boot — Web module

The `web` module provides `BaseController<E, ID, F>`, an abstract Spring MVC controller that reuses the JPA module's service and result-mapping functionality and the Security module's method-authorization helper.

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

The inherited `find` and `query` endpoints use `@PreAuthorize("@authorityRestrictionService.hasAccess(#root.this)")`. Enable method security in your consuming application and annotate each concrete controller with `@AuthorityRestriction`:

```java
import easy.springboot.security.restriction.AuthorityRestriction;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
class MethodSecurityConfig {
}
```

```java
@RestController
@RequestMapping("person")
@AuthorityRestriction({"ROLE_USER"})
public class PersonController
        extends BaseController<Person, Long, PersonFilter> {
    // Inject and return the application's PersonService.
}
```

Use the exact authority names granted by your authentication setup. Multiple values in `@AuthorityRestriction` use OR semantics: any one matching authority is sufficient. An empty restriction allows any authenticated, non-anonymous user; prefer explicit authorities for protected endpoints.

This library does not configure your authentication provider, users, login, CSRF policy, or HTTP security filter chain. You must configure those according to your application and test that authorization behaves as intended.

## Provided endpoints

When a concrete controller is registered with a class-level request mapping, the base controller provides these endpoints relative to that mapping:

| Method | Path | Request | Behavior |
|---|---|---|---|
| `GET` | `/find?id=...` | ID as query parameter | Finds an entity and returns the fields selected by `@QueryResultField`; responds with 404 if the ID is absent. |
| `POST` | `/query` | Filter model as JSON body | Runs the dynamic filter and returns a `QueryResult` whose rows are mapped to response maps. |

For example, if the concrete controller uses `@RequestMapping("person")`, the routes are `GET /person/find?id=1` and `POST /person/query`.

## Example: extend the base controller

```java
import easy.springboot.security.restriction.AuthorityRestriction;
import easy.springboot.web.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("person")
@AuthorityRestriction({"ROLE_USER"})
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
