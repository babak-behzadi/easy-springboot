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
    @SuppressWarnings("unckecked")
    public <E extends BaseEntity> Map<String, Object> mutate(E entity, Class<? extends BaseEntity> skipClazz) {
        Map<String, Object> map = new HashMap<>();
        ReflectionUtils.allAnnotatedFields(entity.getClass(), QueryResultField.class).forEach(field -> {
            QueryResultField annotation = field.getAnnotation(QueryResultField.class);
            Object value = null;
            if (Collection.class.isAssignableFrom(field.getType()) &&
                    field.getGenericType() instanceof ParameterizedType elementType) {
                if (Arrays.stream(elementType.getActualTypeArguments())
                        .noneMatch(type -> type == entity.getClass() || type == skipClazz)) {
                    value = ((Collection<E>) ReflectionUtils.invokeGetter(entity, field.getName()))
                            .stream()
                            .map(item -> mutate(item, entity.getClass()))
                            .collect(Collectors.toList());
                }
            } else {
                if (BaseEntity.class.isAssignableFrom(field.getType()) &&
                        field.getType() != entity.getClass()) {
                    E nestedEntity = (E) ReflectionUtils.invokeGetter(entity, field.getName());
                    value = mutate(nestedEntity, entity.getClass());
                } else {
                    value = ReflectionUtils.fieldValue(entity, field);
                }
            }
            if (value != null) {
                map.put(annotation.name().isEmpty() ? field.getName() : annotation.name(), value);
            }
        });
        return map;
    }
}
