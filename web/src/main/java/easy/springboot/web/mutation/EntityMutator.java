package easy.springboot.web.mutation;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.reflect.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EntityMutator<E extends BaseEntity> implements Function<E, Map<String, Object>> {

    @Override
    public Map<String, Object> apply(E e) {
        Map<String, Object> map = new HashMap<>();
        ReflectionUtils.allAnnotatedFields(e.getClass(), QueryResultField.class).forEach(field -> {
            QueryResultField annotation = field.getAnnotation(QueryResultField.class);
            Object value = ReflectionUtils.fieldValue(e, field);
            map.put(annotation.name().isEmpty() ? field.getName() : annotation.name(), value);
        });
        return map;
    }
}
