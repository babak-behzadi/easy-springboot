package easy.springboot.jpa.reflect;

import org.springframework.validation.ObjectError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public static List<Method> allMethods(Class<?> clazz, List<Method> methods) {
        if (clazz == Object.class) {
            return methods;
        }
        Collections.addAll(methods, clazz.getDeclaredMethods());
        return allMethods(clazz.getSuperclass(), methods);
    }

    public static Object invokeGetter(Object target, String fieldName) {
        Method method = allMethods(target.getClass(), new ArrayList<>())
                .stream()
                .filter(m -> m.getName().toLowerCase().equals("get" + fieldName))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Getter method for [" + fieldName + "] " +
                                "not found in class [" + target.getClass().getName() + "]"));
        if (method.getReturnType() != Void.class) {
            try {
                method.setAccessible(true);
                return method.invoke(target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                method.setAccessible(false);
            }
        }
        throw new RuntimeException("Getter method for [" + fieldName + "] has no return type.");
    }
}
