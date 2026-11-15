# Easy Spring Boot — Security module

The `security` module provides Spring Security configuration helpers and two request-inspection features:

1. Heuristic detection of SQL-like, script-like, and HTML-like patterns in request-body bytes.
2. Optional content-based MIME-type validation for uploaded multipart file parts using Apache Tika.

It also provides an aspect-based authority restriction mechanism for the inherited endpoints exposed by the Web module.

## Requirements

- JDK 17 or later.
- Maven.
- Spring Boot `4.1.2`, as declared in this module's POM.
- A consuming application configured with its own authentication mechanism and identity source.

See the [root README](../README.md) for the repository overview and module build order.

## Build and test

```bash
mvn -f security/pom.xml clean verify
```

The module is an independent Maven project; there is no root aggregator POM.

## Components

| Component | Responsibility |
|---|---|
| `BaseSecurityConfiguration` | Enables web and method security, builds the filter chain, and configures CSRF, CORS, and excluded URLs. |
| `BaseSecurityConfigurationSpec` | Binds settings under `spring.easy.security`, including multipart and injection options. |
| `BaseSecurityFilter` | Adds the request wrapper to the servlet filter chain. |
| `SecureRequestWrapper` | Checks request-body bytes when `getInputStream()` is read and validates uploaded file parts when `getParts()` is called. |
| `InjectionDetector` | Applies regular-expression heuristics for SQL-like, script-like, and HTML-like content. |
| `FileChecker` / `FileMimeType` | Detect uploaded file MIME types with Apache Tika and compare them with the configured allowlist. |
| `BaseEndpointsAuthorityRestrictionAdvice` | Checks the current authentication against endpoint-specific authorities declared on a controller. |

## Request inspection

### Injection-pattern checks

The wrapper invokes `InjectionDetector` from `getInputStream()` when `spring.easy.security.injection.enabled` is true. The detector currently checks for patterns such as SQL keywords and comment markers, script elements and event-handler attributes, and HTML tags.

This is a heuristic filter, not a parser or a comprehensive injection defense:

- It only sees bytes read through the wrapped `getInputStream()`; it does not cover every way request data can enter an application.
- It does not comprehensively inspect query parameters, form parameters, multipart fields, or data supplied by other application channels.
- Regex rules can produce false positives and false negatives. They can reject benign content and miss malicious variants.
- The `customPattern` value exists in the configuration record but is not currently consumed by `InjectionDetector`.

Do not use this feature as your primary SQL injection defense. Use parameterized queries and safe ORM APIs. Apply contextual output encoding, validate data according to its expected type and format, and use a purpose-built HTML sanitizer if rich HTML is a supported input.

### Multipart file validation

When multipart validation is enabled, `SecureRequestWrapper.getParts()` delegates parsing to the servlet container and checks parts with a submitted filename. Ordinary form fields such as `id=1` are skipped. `FileChecker` detects the content MIME type once with Apache Tika and compares it with the configured extension-derived allowlist.

Example configuration:

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
      injection:
        enabled: true
        custom-pattern: ""
      csrf:
        enabled: true
      cors:
        enabled: false
      filter:
        excluded-urls:
          - /actuator/health
```

Use extension values supported by the module's `FileMimeType` mapping, without a leading dot. The allowlist is a MIME-type allowlist derived from those extensions; it does not trust the filename or the part's declared `Content-Type`.

**Upload safety requirements:** configure maximum request and file sizes in the consuming application, generate storage names on the server, store uploads outside executable paths, and add format-specific parsing or scanning when required. MIME detection does not prove that a file is safe. Test uploads against a real embedded servlet container as well as with MockMvc, because multipart mocks may not exercise servlet `Part` parsing.

## Endpoint authority restrictions

The Web module's base controller marks inherited methods with `@PreAuthorizeBaseEndpoint`. Declare the required authorities for those endpoint names on the concrete controller:

```java
@RestController
@RequestMapping("person")
@BaseEndpointsAuthorityRestriction({
    @BaseEndpoint(value = "find", authorities = {"USER"}),
    @BaseEndpoint(value = "query", authorities = {"USER"})
})
public class PersonController
        extends BaseController<Person, Long, PersonFilterModel> {
    // Implement the application's service accessor.
}
```

The authority strings must match values supplied by the application's `GrantedAuthority` instances. The current advice checks that the granted-authority set contains all authorities declared for the matched endpoint. An endpoint without a matching `BaseEndpoint` declaration is denied.

The consuming application must configure authentication and review the full HTTP security policy. This module does not create users, authenticate requests, or provide an identity provider.

## Important limitations

- The injection detector is heuristic and does not replace parameterized database access, validation, encoding, or sanitization.
- The request wrapper checks body bytes only when `getInputStream()` is read; it is not a universal inspection layer for every parameter source.
- Multipart checks depend on servlet `Part` parsing and are only one layer of upload validation.
- The module's defaults must be reviewed against the consuming application's routes, authentication, CORS, and CSRF requirements.
- Add integration tests for your actual servlet container, multipart requests, request-body formats, and authorization rules before production use.

## License

Licensed under the [Apache License, Version 2.0](../LICENSE).

Copyright 2026 Babak Behzadi.
