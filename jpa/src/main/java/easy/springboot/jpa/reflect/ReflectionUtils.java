package easy.springboot.jpa.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtils {

    public static List<Field> allFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class) {
            return fields;
        }
        Collections.addAll(fields, clazz.getDeclaredFields());
        return allFields(clazz.getSuperclass(), fields);
    }

    public static List<Field> allAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return ReflectionUtils.allFields(clazz, new ArrayList<>())
                .stream()
                .filter(field -> field.isAnnotationPresent(annotation))
                .toList();
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return ReflectionUtils.allFields(clazz, new ArrayList<>())
                .stream()
                .filter(field1 -> field1.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Field [" + fieldName + "] not found in class [" + clazz.getName() + "]"));
    }

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

    public static Object fieldValue(Object target, String fieldName) {
        Field field = findField(target.getClass(), fieldName);
        return ReflectionUtils.fieldValue(target, field);
    }
}
