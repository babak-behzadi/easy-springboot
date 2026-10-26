# Easy Spring Boot — Security module

The `security` module provides a small Spring Security helper for restricting access to controller classes by granted authority.

## Requirements

- JDK 17 or later.
- Maven.
- Spring Boot `4.1.2`, as declared in this module's POM.
- A consuming Spring application configured with Spring Security authentication and authorization.

## Build and test

From the repository root:

```bash
mvn -f security/pom.xml clean verify
```

The module is an independent Maven project; there is no root aggregator POM.

## Components

| Component | Purpose |
|---|---|
| `@AuthorityRestriction` | Declares the authorities that may access an annotated class. |
| `AuthorityRestrictionService` | Checks the current Spring Security authentication and the target class's restriction. |

When multiple authorities are listed, access is granted if the authenticated user has **at least one** of them (OR semantics). Authority strings must match the values returned by Spring Security's `GrantedAuthority.getAuthority()`, for example `ROLE_USER` or `ROLE_ADMIN`.

## Usage with the Web module

Enable method security in the consuming application:

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
```

Then annotate a concrete controller with the required authority or authorities:

```java
import easy.springboot.security.restriction.PreAuthorizeBaseEndpoint;
import easy.springboot.security.restriction.PreAuthorizeBaseEndpoints;
import easy.springboot.web.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("person")
@PreAuthorizeBaseEndpoint({"ROLE_USER"})
public class PersonController
        extends BaseController<Person, Long, PersonFilter> {
    // Provide the application's PersonService as required by BaseController.
}
```

The Web module's inherited `find` and `query` methods use `@PreAuthorize` to call `AuthorityRestrictionService`. The consuming application must configure its authentication mechanism and HTTP security filter chain as well; this module does not create users, authenticate requests, or configure login, sessions, CSRF, or endpoint rules for the application.

## Access behavior and safety

- A missing `@AuthorityRestriction` annotation denies access.
- An empty restriction, `@AuthorityRestriction`, allows any authenticated, non-anonymous user. Prefer explicit authorities for protected endpoints.
- A missing authentication, an unauthenticated token, or Spring Security's anonymous authentication is denied.
- The service uses the first matching restriction on the target class. Apply the annotation to the concrete controller class and test the behavior in your application's proxy/configuration setup.

This is a small authorization helper, not a complete security framework. Review the application's method-security configuration, HTTP security rules, authentication setup, and endpoint tests before production use.

## License

Licensed under the [Apache License, Version 2.0](../LICENSE).

Copyright 2026 Babak Behzadi.
