package easy.springboot.jpa.query;

import easy.springboot.jpa.reflect.ReflectionUtils;
import jakarta.persistence.criteria.Predicate;
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

    private List<Field> searchFields() {
        return ReflectionUtils.allFields(this.getClass(), new ArrayList<>())
                .stream()
                .filter(field -> field.isAnnotationPresent(FilterField.class))
                .toList();
    }

    private Object fieldValue(Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(this);
            field.setAccessible(false);
            return value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String likeValue(String s, FilterOperand operand) {
        return switch (operand) {
            case LIKE, LIKE_CI, NOT_LIKE, NOT_LIKE_CI -> "%" + s + "%";
            case STARTS_WITH, STARTS_WITH_CI -> s + "%";
            case ENDS_WITH, ENDS_WITH_CI -> "%" + s;
            default -> throw new RuntimeException("Invalid operand type " + operand);
        };
    }

    @SuppressWarnings("unchecked")
    public <E> Specification<E> toSpecification() {
        return Specification.where(
                (from, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    searchFields().forEach(field -> {
                        FilterField filterField = field.getAnnotation(FilterField.class);
                        Object value = fieldValue(field);
                        FilterOperand operand = filterField.operand();
                        Predicate predicate = switch (operand) {
                            case EQ -> criteriaBuilder.equal(from.get(field.getName()), value);
                            case NOT_EQ -> criteriaBuilder.notEqual(from.get(field.getName()), value);
                            case EQ_CI,
                                 NOT_EQ_CI -> {
                                if (value instanceof String string) {
                                    yield operand == FilterOperand.EQ_CI ?
                                            criteriaBuilder.equal(criteriaBuilder.lower(from.get(field.getName())), string.toLowerCase()) :
                                            criteriaBuilder.notEqual(criteriaBuilder.lower(from.get(field.getName())), string.toLowerCase());
                                }
                                throw new RuntimeException("Operands 'EQ_CI' and 'NOT_EQ_CI' are only applicable to String values");
                            }
                            case GT,
                                 GE,
                                 LT,
                                 LE -> {
                                if (value instanceof Comparable comparable) {
                                    yield switch (operand) {
                                        case GT -> criteriaBuilder.greaterThan(from.get(field.getName()), comparable);
                                        case GE ->
                                                criteriaBuilder.greaterThanOrEqualTo(from.get(field.getName()), comparable);
                                        case LT -> criteriaBuilder.lessThan(from.get(field.getName()), comparable);
                                        case LE ->
                                                criteriaBuilder.lessThanOrEqualTo(from.get(field.getName()), comparable);
                                        default ->
                                                throw new RuntimeException("Invalid  operand type " + filterField.operand());
                                    };
                                }
                                throw new RuntimeException("Operands 'GT', 'GE', 'LT' and 'LE' are only applicable to Comparable values");
                            }
                            case IN -> {
                                if (value instanceof Collection<?> collection) {
                                    yield from.get(field.getName()).in(collection);
                                } else if (value.getClass().isArray()) {
                                    yield from.get(field.getName()).in(value);
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
                                Comparable<Object> lowerBound = (Comparable<Object>) lower;
                                @SuppressWarnings("unchecked")
                                Comparable<Object> upperBound = (Comparable<Object>) upper;
                                yield criteriaBuilder.and(
                                        criteriaBuilder.greaterThanOrEqualTo(
                                                from.get(field.getName()),
                                                lowerBound
                                        ),
                                        criteriaBuilder.lessThan(
                                                from.get(field.getName()),
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
                                    String likeValue = likeValue(string, operand);
                                    yield switch (operand) {
                                        case LIKE, STARTS_WITH, ENDS_WITH ->
                                                criteriaBuilder.like(from.get(field.getName()), likeValue);
                                        case LIKE_CI, STARTS_WITH_CI, ENDS_WITH_CI ->
                                                criteriaBuilder.like(criteriaBuilder.lower(from.get(field.getName())), likeValue.toLowerCase());
                                        case NOT_LIKE -> criteriaBuilder.notLike(from.get(field.getName()), likeValue);
                                        case NOT_LIKE_CI ->
                                                criteriaBuilder.notLike(criteriaBuilder.lower(from.get(field.getName())), likeValue.toLowerCase());
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
