package easy.springboot.web.mutation;

import org.springframework.core.serializer.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QueryResultField {

    String name() default "";

    Class<?> serializer() default Serializer.class;
}
