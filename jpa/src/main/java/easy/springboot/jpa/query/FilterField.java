package easy.springboot.jpa.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterField {

    /**
     * filter field name
     *
     * @return {@link String}
     */
    String value();

    /**
     * query operand that will be done on the field
     *
     * @return {@link FilterField}
     */
    FilterOperand operand();
}
