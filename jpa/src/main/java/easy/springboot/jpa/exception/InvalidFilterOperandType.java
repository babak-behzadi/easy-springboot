package easy.springboot.jpa.exception;

import easy.springboot.jpa.query.FilterOperand;

import java.util.List;
import java.util.stream.Collectors;

public class InvalidFilterOperandType extends EasyJpaException {

    public InvalidFilterOperandType(FilterOperand operand) {
        super("Invalid operand type " + operand);
    }

    public InvalidFilterOperandType(List<FilterOperand> operands, Class<?> desiredClass) {
        super("Operand(s) [%s] is/are only applicable to [%s] values".formatted(
                operands.stream().map(FilterOperand::name).collect(Collectors.joining(",")),
                desiredClass.getSimpleName()));
    }

    public InvalidFilterOperandType(List<FilterOperand> operands, List<Class<?>> desiredClasses) {
        super("Operand(s) [%s] is/are only applicable to [%s] values".formatted(
                operands.stream().map(FilterOperand::name).collect(Collectors.joining(",")),
                desiredClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(","))));
    }
}
