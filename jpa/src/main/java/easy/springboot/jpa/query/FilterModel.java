/*
 * Copyright 2026 Babak Behzadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy.springboot.jpa.query;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.exception.InvalidBetweenFilterOperandArgs;
import easy.springboot.jpa.exception.InvalidBetweenFilterOperandValues;
import easy.springboot.jpa.exception.InvalidFilterOperandType;
import easy.springboot.jpa.reflect.ReflectionUtils;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public abstract class FilterModel implements Serializable {

    private int page;
    private int pageSize;
    private Sort sort;

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

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
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
     * @param s       string value of like expression
     * @param operand *LIKE* operation
     * @return {@link String}
     */
    private String createLikeExpression(String s, FilterOperand operand) {
        return switch (operand) {
            case LIKE, NOT_LIKE -> "%" + s + "%";
            case LIKE_CI, NOT_LIKE_CI -> "%" + s.toLowerCase(Locale.ROOT) + "%";
            case STARTS_WITH -> s + "%";
            case STARTS_WITH_CI -> s.toLowerCase(Locale.ROOT) + "%";
            case ENDS_WITH -> "%" + s;
            case ENDS_WITH_CI -> "%" + s.toLowerCase(Locale.ROOT);
            default -> throw new InvalidFilterOperandType(operand);
        };
    }

    /**
     * extract join field path
     *
     * @param from      relation root
     * @param fieldName name of the entity field
     * @return {@link Path}
     */
    private Path<?> extractJoinFieldPath(From<?, ?> from, String fieldName) {
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
                        Path<?> fieldPath;
                        if (filterField.value().contains(".")) {
                            fieldPath = extractJoinFieldPath(from, filterField.value());
                        } else {
                            fieldPath = from.get(filterField.value());
                        }
                        FilterOperand operand = filterField.operand();
                        Predicate predicate = switch (operand) {
                            case EQ -> criteriaBuilder.equal(fieldPath, value);
                            case NOT_EQ -> criteriaBuilder.notEqual(fieldPath, value);
                            case EQ_CI,
                                 NOT_EQ_CI -> {
                                if (value instanceof String string) {
                                    yield operand == FilterOperand.EQ_CI ?
                                            criteriaBuilder.equal(criteriaBuilder.lower(fieldPath.as(String.class)),
                                                    string.toLowerCase(Locale.ROOT)) :
                                            criteriaBuilder.notEqual(criteriaBuilder.lower(fieldPath.as(String.class)),
                                                    string.toLowerCase(Locale.ROOT));
                                }
                                throw new InvalidFilterOperandType(List.of(FilterOperand.EQ_CI, FilterOperand.NOT_EQ_CI), String.class);
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
                                        default -> throw new InvalidFilterOperandType(filterField.operand());
                                    };
                                }
                                throw new InvalidFilterOperandType(
                                        List.of(FilterOperand.GT, FilterOperand.GE, FilterOperand.LE, FilterOperand.LT),
                                        Comparable.class);
                            }
                            case IN -> {
                                if (value instanceof Collection<?> collection) {
                                    yield fieldPath.in(collection);
                                } else if (value.getClass().isArray()) {
                                    yield fieldPath.in(value);
                                }
                                throw new InvalidFilterOperandType(List.of(FilterOperand.IN),
                                        List.of(Collection.class, Array.class));
                            }
                            case BETWEEN -> {
                                Object lower;
                                Object upper;
                                if (value instanceof Collection<?> collection) {
                                    if (collection.size() != 2) {
                                        throw new InvalidBetweenFilterOperandArgs();
                                    }
                                    var iterator = collection.iterator();
                                    lower = iterator.next();
                                    upper = iterator.next();
                                } else if (value.getClass().isArray()) {
                                    int length = Array.getLength(value);
                                    if (length != 2) {
                                        throw new InvalidBetweenFilterOperandArgs();
                                    }
                                    lower = Array.get(value, 0);
                                    upper = Array.get(value, 1);
                                } else {
                                    throw new InvalidFilterOperandType(List.of(FilterOperand.BETWEEN),
                                            List.of(Collection.class, Array.class));
                                }

                                if (!(lower instanceof Comparable<?>)
                                        || !(upper instanceof Comparable<?>)) {
                                    throw new InvalidBetweenFilterOperandValues();
                                }
                                Comparable<Object> lowerBound = (Comparable<Object>) lower;
                                Comparable<Object> upperBound = (Comparable<Object>) upper;
                                yield criteriaBuilder.and(
                                        criteriaBuilder.greaterThanOrEqualTo(
                                                (Expression<Comparable<Object>>) fieldPath,
                                                lowerBound
                                        ),
                                        criteriaBuilder.lessThanOrEqualTo(
                                                (Expression<Comparable<Object>>) fieldPath,
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
                                                likeValue.toLowerCase(Locale.ROOT));
                                        case NOT_LIKE -> criteriaBuilder.notLike(fieldPath.as(String.class), likeValue);
                                        case NOT_LIKE_CI -> criteriaBuilder.notLike(
                                                criteriaBuilder.lower(fieldPath.as(String.class)),
                                                likeValue.toLowerCase(Locale.ROOT));
                                        default -> throw new InvalidFilterOperandType(operand);
                                    };
                                }
                                throw new InvalidFilterOperandType(
                                        List.of(FilterOperand.LIKE,
                                                FilterOperand.LIKE_CI,
                                                FilterOperand.NOT_LIKE,
                                                FilterOperand.NOT_LIKE_CI,
                                                FilterOperand.STARTS_WITH,
                                                FilterOperand.ENDS_WITH,
                                                FilterOperand.STARTS_WITH_CI,
                                                FilterOperand.ENDS_WITH_CI),
                                        String.class
                                );
                            }
                        };
                        predicates.add(predicate);
                    });
                    return criteriaBuilder.and(predicates);
                });
    }
}
