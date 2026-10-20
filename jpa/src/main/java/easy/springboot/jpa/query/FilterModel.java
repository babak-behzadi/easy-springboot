package easy.springboot.jpa.query;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.reflect.ReflectionUtils;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class FilterModel implements Serializable {

    private int page;
    private int pageSize;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * finds all fields annotated with {@link FilterField}- inherited fields included-
     *
     * @return {@link List<Field>}
     */
    private List<Field> findAllFilterFields() {
        return ReflectionUtils.allFields(this.getClass(), new ArrayList<>())
                .stream()
                .filter(field -> field.isAnnotationPresent(FilterField.class))
                .toList();
    }

    /**
     * creates like expression for the query, based on operand
     *
     * @param s
     * @param operand
     * @return {@link String}
     */
    private String createLikeExpression(String s, FilterOperand operand) {
        return switch (operand) {
            case LIKE, LIKE_CI, NOT_LIKE, NOT_LIKE_CI -> "%" + s + "%";
            case STARTS_WITH, STARTS_WITH_CI -> s + "%";
            case ENDS_WITH, ENDS_WITH_CI -> "%" + s;
            default -> throw new RuntimeException("Invalid operand type " + operand);
        };
    }

    private Path<?> extractFieldPath(From<?, ?> from, String fieldName) {
        String[] parts = fieldName.split("\\.");
        From<?, ?> current = from;
        for (int i = 0; i < parts.length - 1; i++) {
            current = current.join(parts[i], JoinType.INNER);
        }
        return current.get(parts[parts.length - 1]);
    }

    /**
     * extract respective {@link Specification} from {@link FilterModel} object
     *
     * @param <E> type of {@link BaseEntity}
     * @return {@link Specification<E>}
     */
    @SuppressWarnings("unchecked")
    public <E extends BaseEntity> Specification<E> toSpecification() {
        return Specification.where(
                (from, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    findAllFilterFields().forEach(field -> {
                        FilterField filterField = field.getAnnotation(FilterField.class);
                        Object value = ReflectionUtils.fieldValue(this, field);
                        if (value == null) {
                            return;
                        }
                        Path<?> fieldPath = from.get(field.getName());
                        if (filterField.value().contains(".")) {
                            fieldPath = extractFieldPath(from, filterField.value());
                        }
                        FilterOperand operand = filterField.operand();
                        Predicate predicate = switch (operand) {
                            case EQ -> criteriaBuilder.equal(fieldPath, value);
                            case NOT_EQ -> criteriaBuilder.notEqual(fieldPath, value);
                            case EQ_CI,
                                 NOT_EQ_CI -> {
                                if (value instanceof String string) {
                                    yield operand == FilterOperand.EQ_CI ?
                                            criteriaBuilder.equal(criteriaBuilder.lower(fieldPath.as(String.class)), string.toLowerCase()) :
                                            criteriaBuilder.notEqual(criteriaBuilder.lower(fieldPath.as(String.class)), string.toLowerCase());
                                }
                                throw new RuntimeException("Operands 'EQ_CI' and 'NOT_EQ_CI' are only applicable to String values");
                            }
                            case GT,
                                 GE,
                                 LT,
                                 LE -> {
                                if (value instanceof Comparable comparable) {
                                    yield switch (operand) {
                                        case GT -> criteriaBuilder.greaterThan((Expression) fieldPath, comparable);
                                        case GE ->
                                                criteriaBuilder.greaterThanOrEqualTo((Expression) fieldPath, comparable);
                                        case LT -> criteriaBuilder.lessThan((Expression) fieldPath, comparable);
                                        case LE ->
                                                criteriaBuilder.lessThanOrEqualTo((Expression) fieldPath, comparable);
                                        default ->
                                                throw new RuntimeException("Invalid  operand type " + filterField.operand());
                                    };
                                }
                                throw new RuntimeException("Operands 'GT', 'GE', 'LT' and 'LE' are only applicable to Comparable values");
                            }
                            case IN -> {
                                if (value instanceof Collection<?> collection) {
                                    yield fieldPath.in(collection);
                                } else if (value.getClass().isArray()) {
                                    yield fieldPath.in(value);
                                }
                                throw new RuntimeException("Operand 'BETWEEN' is only applicable to Collection and Array values");
                            }
                            case BETWEEN -> {
                                Object lower;
                                Object upper;
                                if (value instanceof Collection<?> collection) {
                                    if (collection.size() != 2) {
                                        throw new IllegalArgumentException(
                                                "Operand 'BETWEEN' requires exactly 2 values"
                                        );
                                    }
                                    var iterator = collection.iterator();
                                    lower = iterator.next();
                                    upper = iterator.next();
                                } else if (value != null && value.getClass().isArray()) {
                                    int length = java.lang.reflect.Array.getLength(value);
                                    if (length != 2) {
                                        throw new IllegalArgumentException(
                                                "Operand 'BETWEEN' requires exactly 2 values"
                                        );
                                    }
                                    lower = java.lang.reflect.Array.get(value, 0);
                                    upper = java.lang.reflect.Array.get(value, 1);
                                } else {
                                    throw new IllegalArgumentException(
                                            "Operand 'BETWEEN' is only applicable to Collection and Array values"
                                    );
                                }

                                if (!(lower instanceof Comparable<?>)
                                        || !(upper instanceof Comparable<?>)) {
                                    throw new IllegalArgumentException(
                                            "Values for 'BETWEEN' must implement Comparable"
                                    );
                                }
                                @SuppressWarnings("unchecked")
                                Comparable lowerBound = (Comparable<Object>) lower;
                                @SuppressWarnings("unchecked")
                                Comparable upperBound = (Comparable<Object>) upper;
                                yield criteriaBuilder.and(
                                        criteriaBuilder.lessThanOrEqualTo(
                                                (Expression) fieldPath,
                                                lowerBound
                                        ),
                                        criteriaBuilder.lessThan(
                                                (Expression) fieldPath,
                                                upperBound
                                        )
                                );
                            }
                            case LIKE,
                                 LIKE_CI,
                                 NOT_LIKE,
                                 NOT_LIKE_CI,
                                 STARTS_WITH,
                                 ENDS_WITH,
                                 STARTS_WITH_CI,
                                 ENDS_WITH_CI -> {
                                if (value instanceof String string) {
                                    String likeValue = createLikeExpression(string, operand);
                                    yield switch (operand) {
                                        case LIKE, STARTS_WITH, ENDS_WITH -> criteriaBuilder.like(
                                                fieldPath.as(String.class),
                                                likeValue);
                                        case LIKE_CI, STARTS_WITH_CI, ENDS_WITH_CI -> criteriaBuilder.like(
                                                criteriaBuilder.lower(fieldPath.as(String.class)),
                                                likeValue.toLowerCase());
                                        case NOT_LIKE -> criteriaBuilder.notLike(fieldPath.as(String.class), likeValue);
                                        case NOT_LIKE_CI -> criteriaBuilder.notLike(
                                                criteriaBuilder.lower(fieldPath.as(String.class)),
                                                likeValue.toLowerCase());
                                        default -> throw new RuntimeException("Invalid  operand type " + operand);
                                    };
                                }
                                throw new RuntimeException("Operands 'LIKE', 'LIKE_CI', 'NOT_LIKE', 'NOT_LIKE_CI', STARTS_WITH, ENDS_WITH, STARTS_WITH_CI and ENDS_WITH_CI are only applicable to String values");
                            }
                        };
                        predicates.add(predicate);
                    });
                    return criteriaBuilder.and(predicates);
                });
    }
}
