package easy.springboot.jpa.mutation;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.query.QueryResultField;
import easy.springboot.jpa.reflect.ReflectionUtils;
import jakarta.transaction.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityMutator {

    @Transactional
    public Map<String, Object> mutate(Object entity) {
        Map<String, Object> map = new HashMap<>();
        ReflectionUtils.allAnnotatedFields(entity.getClass(), QueryResultField.class).forEach(field -> {
            QueryResultField annotation = field.getAnnotation(QueryResultField.class);
            Object value;
            if (Collection.class.isAssignableFrom(field.getType()) &&
                    field.getGenericType() instanceof ParameterizedType elementType &&
                    Arrays.stream(elementType.getActualTypeArguments()).noneMatch(type -> type != entity.getClass())) {
                value = ((Collection) ReflectionUtils.invokeGetter(entity, field.getName()))
                        .stream()
                        .map(this::mutate)
                        .collect(Collectors.toList());
            } else {
                if (BaseEntity.class.isAssignableFrom(field.getType()) &&
                        field.getType() != entity.getClass()) {
                    Object nestedEntity = ReflectionUtils.invokeGetter(entity, field.getName());
                    value = mutate(nestedEntity);
                } else {
                    value = ReflectionUtils.fieldValue(entity, field);
                }
            }
            map.put(annotation.name().isEmpty() ? field.getName() : annotation.name(), value);
        });
        return map;
    }
}
