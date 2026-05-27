package fr.paris.lutece.plugins.examplemuz.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint( validatedBy = MultipleOfValidator.class )
@Target( { ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface MultipleOf 
{
    int value();
    String message() default "#i18n{examplemuz.validation.project.Cost.multipleOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
