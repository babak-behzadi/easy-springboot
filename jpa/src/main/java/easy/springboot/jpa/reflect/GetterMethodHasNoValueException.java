package easy.springboot.jpa.reflect;

public class GetterMethodHasNoValueException extends EasyReflectionException {
    public GetterMethodHasNoValueException(String fieldName) {
        super(String.format("Getter method for [%s] has not return value", fieldName));
    }
}
