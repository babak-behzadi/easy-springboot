# Easy Spring Boot — JPA module

The `jpa` module provides reusable Spring Data JPA building blocks for repositories, services, dynamic filtering, and mapping selected entity fields into query results.

## Requirements

- JDK 17 or later.
- Maven.
- Spring Boot `4.1.2` as declared in this module's POM.
- A configured database and JDBC driver in the consuming application.

## Build and test

From the repository root:

```bash
mvn -f jpa/pom.xml clean verify
```

To install the artifact into your local Maven repository (needed before building the current Web module):

```bash
mvn -f jpa/pom.xml clean install
```

H2 is included as a test dependency. Configure a separate database for your application.

## Core components

| Type | Purpose |
|---|---|
| `BaseEntity` | Mapped superclass implementing `Serializable`. Extend it from entities used with the base repository and service. |
| `BaseRepository<E, ID>` | Extends `JpaRepository<E, ID>` and `JpaSpecificationExecutor<E>`. It is marked `@NoRepositoryBean`. |
| `BaseService<E, ID, R>` | Provides common find, save, delete, dynamic query, and mapping operations. |
| `FilterModel` | Builds a JPA `Specification` from non-null fields annotated with `@FilterField`. |
| `QueryResult<R>` | Holds `rows`, `page`, `pageSize`, and `total` metadata. |
| `EntityMutator` | Maps fields annotated with `@QueryResultField` into a `Map<String, Object>`. |

## Example: entity, repository, and service

### 1. Define an entity

```java
import easy.springboot.jpa.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Person extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
```

### 2. Create a repository

```java
import easy.springboot.jpa.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends BaseRepository<Person, Long> {
}
```

### 3. Create a service

```java
import easy.springboot.jpa.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class PersonService extends BaseService<Person, Long, PersonRepository> {
    public PersonService(PersonRepository repository) {
        super(repository);
    }
}
```

Register these classes with Spring and configure entity/repository scanning in your application. The entity's package must be included in JPA entity scanning.

## Dynamic filtering

Create a class that extends `FilterModel`. Each `@FilterField` declares the entity property/path and the operation to use. A field whose value is `null` is ignored.

```java
import easy.springboot.jpa.query.FilterField;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.FilterOperand;

public class PersonFilter extends FilterModel {
    @FilterField(value = "firstName", operand = FilterOperand.LIKE_CI)
    private String firstName;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
```

Usage from a service:

```java
QueryResult<Person> result = personService.query(filter);
```

The `FilterModel` also has `page`, `pageSize`, and `sort` properties. **Current implementation note:** `BaseService.query()` clamps the page index to zero or greater and page size to 1–10, but does not currently apply the `sort` property to the repository query.

### Supported operands

| Operand | Behavior |
|---|---|
| `EQ`, `NOT_EQ` | Equal / not equal |
| `EQ_CI`, `NOT_EQ_CI` | Case-insensitive string equality / inequality |
| `GT`, `GE`, `LT`, `LE` | Comparable values: greater than, greater than or equal, less than, less than or equal |
| `IN` | Value is contained in a collection or array |
| `BETWEEN` | Two-element collection or array; both boundaries are inclusive |
| `LIKE`, `NOT_LIKE` | Contains-style SQL LIKE / NOT LIKE pattern |
| `LIKE_CI`, `NOT_LIKE_CI` | Case-insensitive contains-style matching |
| `STARTS_WITH`, `STARTS_WITH_CI` | Prefix matching, optionally case-insensitive |
| `ENDS_WITH`, `ENDS_WITH_CI` | Suffix matching, optionally case-insensitive |

For relationship properties, use a dotted path such as `"department.name"`. The current implementation joins relationship segments with `INNER` joins, which can exclude rows where a relationship is absent.

**Input safety:** only expose filter fields and operands that are appropriate for your API. Validate request values, consider escaping SQL LIKE wildcard characters (`%` and `_`) when literal matching is desired, and test filters against your target database.

## Mapping query results

Annotate fields with `@QueryResultField` to include them in the map created by `EntityMutator`. The annotation's optional `name` changes the output key.

```java
@QueryResultField
private Long id;

@QueryResultField(name = "first_name")
private String firstName;
```

Then use the service mapping methods:

```java
Map<String, Object> mappedPerson = personService.mutate(person);
QueryResult<Map<String, Object>> mappedResult = personService.mutate(result);
```

By default, fields whose Java field names match `password`, `secret`, `token`, or `apikey` (case-insensitively) are omitted from mutation. Configure the list with the `spring.easy.protectedColumns` property. This is a safeguard, not a complete authorization policy; test it with your entity model and prefer explicit response DTOs for sensitive APIs.

## Query result shape

`QueryResult<R>` exposes:

- `rows`: returned items.
- `page`: zero-based page index.
- `pageSize`: page size used for the query.
- `total`: total matching count supplied by the service.

The current `queryAll()` method loads all matching entities into memory and should only be used when the result set is known to be small.

## Testing

The module's tests include example entity, repository, filter model, and service classes under `src/test/java`. Run them with:

```bash
mvn -f jpa/pom.xml clean verify
```

Prefer self-contained tests: create the data needed by each test rather than relying on test execution order or fixed generated IDs. An in-memory database is usually a good choice for repeatable integration tests.

## Current scope

This module is a developing utility library, not a complete persistence or API security framework. Review transaction boundaries, lazy-loaded relationships, null relationships, cyclic entity graphs, pagination/count-query performance, exception handling, and database-specific behavior before production use.
