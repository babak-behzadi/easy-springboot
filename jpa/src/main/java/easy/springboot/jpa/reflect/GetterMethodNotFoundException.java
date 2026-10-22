package easy.springboot.jpa.reflect;

public class GetterMethodNotFoundException extends EasyReflectionException {

    public GetterMethodNotFoundException(String fieldName, Class<?> clazz) {
        super(String.format("Getter method for field [%s] not found in class [%s]", fieldName, clazz.getName()));
    }
}
