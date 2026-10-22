package easy.springboot.jpa.exception;

public abstract class EasyJpaException extends RuntimeException {

    public EasyJpaException(String message) {
        super(message);
    }
}
