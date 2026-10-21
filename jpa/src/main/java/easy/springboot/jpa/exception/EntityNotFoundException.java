package easy.springboot.jpa.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Object id) {
        super("Entity with id [" + id + "] not found");
    }
}
