package easy.springboot.jpa.exception;

public class InvalidBetweenFilterOperandValues extends EasyJpaException {

    public InvalidBetweenFilterOperandValues() {
        super("Values for 'BETWEEN' must implement Comparable");
    }
}
