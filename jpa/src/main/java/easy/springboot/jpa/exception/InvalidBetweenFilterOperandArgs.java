package easy.springboot.jpa.exception;

public class InvalidBetweenFilterOperandArgs extends EasyJpaException {

    public InvalidBetweenFilterOperandArgs() {
        super("Operand 'BETWEEN' requires exactly 2 values");
    }
}
