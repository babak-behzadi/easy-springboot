package easy.springboot.jpa.reflect;

public class FieldNotFoundException extends EasyReflectionException {

    public FieldNotFoundException(String fieldName, Class<?> clazz) {
        super(String.format("Field [%s] not found in class [%s]", fieldName, clazz.getName()));
    }
}
