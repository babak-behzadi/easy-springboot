package easy.springboot.jpa.reflect;

import java.lang.reflect.Field;
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
}
