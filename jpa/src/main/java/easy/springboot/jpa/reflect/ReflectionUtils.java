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

package easy.springboot.jpa.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtils {

    /**
     * returns all fields of class containing inherited fields back to {@link Object}
     *
     * @param clazz  class name
     * @param fields initial list of {@link Field}
     * @return {@link List<Field>}
     */
    public static List<Field> allFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class) {
            return fields;
        }
        Collections.addAll(fields, clazz.getDeclaredFields());
        return allFields(clazz.getSuperclass(), fields);
    }

    /**
     * returns all fields of class, annotated by input annotation, containing inherited fields back to {@link Object}
     *
     * @param clazz      class name
     * @param annotation input annotation
     * @return {@link List<Field>}
     */
    public static List<Field> allAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return ReflectionUtils.allFields(clazz, new ArrayList<>())
                .stream()
                .filter(field -> field.isAnnotationPresent(annotation))
                .toList();
    }

    /**
     * finds a field by name
     *
     * @param clazz     class name
     * @param fieldName name of the field
     * @return {@link Field}
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        return ReflectionUtils.allFields(clazz, new ArrayList<>())
                .stream()
                .filter(field1 -> field1.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new FieldNotFoundException(fieldName, clazz));
    }

    /**
     * gets the value of the given field by field object
     *
     * @param target object
     * @param field  field
     * @return {@link Object}
     */
    public static Object fieldValue(Object target, Field field) {
        field.setAccessible(true);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * gets the value of the given field by field name
     *
     * @param target    object
     * @param fieldName field name
     * @return {@link Object}
     */
    public static Object fieldValue(Object target, String fieldName) {
        Field field = findField(target.getClass(), fieldName);
        return ReflectionUtils.fieldValue(target, field);
    }

    /**
     * returns all fields of class containing inherited fields back to {@link Object}
     *
     * @param clazz   class
     * @param methods initial list of {@link Method}
     * @return {@link List<Method>}
     */
    public static List<Method> allMethods(Class<?> clazz, List<Method> methods) {
        if (clazz == Object.class) {
            return methods;
        }
        Collections.addAll(methods, clazz.getDeclaredMethods());
        return allMethods(clazz.getSuperclass(), methods);
    }

    /**
     * invokes getter metho of the given field
     *
     * @param target    object
     * @param fieldName field name
     * @return {@link Object}
     */
    public static Object invokeGetter(Object target, String fieldName) {
        Method method = allMethods(target.getClass(), new ArrayList<>())
                .stream()
                .filter(m ->
                        m.getName().equals("get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)) ||
                                m.getName().equals("is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)))
                .findFirst()
                .orElseThrow(() -> new GetterMethodNotFoundException(fieldName, target.getClass()));
        if (method.getReturnType() != void.class) {
            try {
                method.setAccessible(true);
                return method.invoke(target);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                method.setAccessible(false);
            }
        }
        throw new GetterMethodHasNoValueException(fieldName);
    }
}
