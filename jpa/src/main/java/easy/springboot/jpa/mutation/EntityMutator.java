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

package easy.springboot.jpa.mutation;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.query.QueryResultField;
import easy.springboot.jpa.reflect.ReflectionUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EntityMutator {

    /**
     * to keep sensitive data safe, by default fields like 'password' are skipped in data mutation process
     */
    @Value("${spring.easy.protectedColumns:password,secret,token,apikey}")
    private String[] protectedColumns;

    /**
     * convert entity object to {@link Map} objects to make response object fields elective
     *
     * @param entity    entity class object
     * @param skipClazz fields of this class will be skipped in mutation
     * @param <E>       entity class which extends {@link BaseEntity}
     * @return {@link Map}
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public <E extends BaseEntity> Map<String, Object> mutate(E entity, Class<? extends BaseEntity> skipClazz) {
        Map<String, Object> map = new HashMap<>();
        ReflectionUtils.allAnnotatedFields(entity.getClass(), QueryResultField.class).forEach(field -> {
            if (protectedColumns != null && Arrays.stream(protectedColumns)
                    .anyMatch(protectedColumn -> field.getName().equalsIgnoreCase(protectedColumn))) {
                return;
            }
            QueryResultField annotation = field.getAnnotation(QueryResultField.class);
            Object value = null;
            if (Collection.class.isAssignableFrom(field.getType()) &&
                    field.getGenericType() instanceof ParameterizedType elementType) {
                if (Arrays.stream(
                        elementType.getActualTypeArguments()).noneMatch(
                        type -> type == entity.getClass() || type == skipClazz)) {
                    value = ((Collection<E>) ReflectionUtils.invokeGetter(entity, field.getName()))
                            .stream().map(item -> mutate(item, entity.getClass())).collect(Collectors.toList());
                }
            } else {
                if (BaseEntity.class.isAssignableFrom(field.getType()) && field.getType() != entity.getClass()) {
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
