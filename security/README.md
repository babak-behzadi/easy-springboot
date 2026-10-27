# Easy Spring Boot — Security module

The `security` module provides Spring Security helpers for restricting access by granted authority and configuring common HTTP security behavior. It also supports optional MIME-type validation of multipart file uploads using Apache Tika.

## Requirements

- JDK 17 or later.
- Maven.
- Spring Boot `4.1.2`, as declared in this module's POM.
- A consuming Spring application configured with the authentication mechanism appropriate to the application.

For the repository-wide overview and build order, see the [root README](../README.md).

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
| `BaseSecurityConfiguration` | Configures the security filter chain, CSRF, CORS, and request exclusions from properties. |
| `BaseSecurityFilter` / `SecureRequestWrapper` | Wrap the request and provide multipart-part validation through the servlet request API. |
| `FileChecker` / `FileMimeType` | Detect the uploaded content's MIME type with Apache Tika and compare it with the configured allowlist. |

When multiple authorities are listed, access is granted if the authenticated user has **at least one** of them (OR semantics). Authority strings must match the values returned by Spring Security's `GrantedAuthority.getAuthority()`, for example `ROLE_USER` or `ROLE_ADMIN`.

## Configuration

Configuration properties use the prefix `spring.easy.security`. For example, a YAML configuration can enable multipart validation and allow selected file extensions:

```yaml
spring:
  easy:
    security:
      multipart:
        enabled: true
        file-extensions:
          - png
          - jpg
          - jpeg
      csrf:
        enabled: true
      cors:
        enabled: false
      filter:
        excluded-urls:
          - /actuator/health
```

Use extension values supported by the module's `FileMimeType` mapping, without a leading dot. The allowlist is used to derive accepted MIME types; the check is based on Apache Tika's content detection rather than trusting the filename extension or a client-supplied `Content-Type`.

When multipart validation is disabled, the wrapper does not perform the MIME-type allowlist check. Configure upload-size limits in the consuming application as well; MIME detection is not a substitute for size limits, safe storage, or format-specific validation.

## Usage with the Web module

Enable method security in the consuming application if you use the Web module's inherited protected endpoints:

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

The consuming application must configure its authentication mechanism and HTTP security rules as appropriate. This module does not create users or authenticate requests, and it does not choose the application's login, session, or identity-provider setup.

## Access behavior and safety

- A missing `@AuthorityRestriction` annotation denies access.
- An empty restriction, `@AuthorityRestriction`, allows any authenticated, non-anonymous user. Prefer explicit authorities for protected endpoints.
- A missing authentication, an unauthenticated token, or Spring Security's anonymous authentication is denied.
- The service uses the first matching restriction on the target class. Apply the annotation to the concrete controller class and test the behavior in your application's proxy/configuration setup.
- File MIME detection is a defense-in-depth check. It cannot guarantee that a file is benign or structurally valid. Set maximum request/file sizes, store files outside executable/public paths where appropriate, generate server-side storage names, and add format-specific validation for sensitive use cases.
- Test multipart requests against a running servlet container as well as with MockMvc; multipart mocks may not exercise the same servlet `Part` parsing path as a real request.

This is a small security helper, not a complete security framework. Review the application's method-security configuration, HTTP security rules, authentication setup, upload limits, exception handling, and endpoint tests before production use.

## License

Licensed under the [Apache License, Version 2.0](../LICENSE).

Copyright 2026 Babak Behzadi.
