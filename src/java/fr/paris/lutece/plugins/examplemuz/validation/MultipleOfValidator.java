package fr.paris.lutece.plugins.examplemuz.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MultipleOfValidator implements ConstraintValidator<MultipleOf, Integer>
{
    private int _nMultiple;

    @Override
    public void initialize( MultipleOf annotation )
    {
        _nMultiple = annotation.value();
    } 

    @Override
    public boolean isValid( Integer value, ConstraintValidatorContext context )
    {
        if (value == null ) return true;
        return value % _nMultiple == 0;
    }
}
