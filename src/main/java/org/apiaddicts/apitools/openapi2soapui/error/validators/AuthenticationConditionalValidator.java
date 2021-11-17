package org.apiaddicts.apitools.openapi2soapui.error.validators;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Authentication property constraint validator
 */
@Slf4j
public class AuthenticationConditionalValidator implements ConstraintValidator<AuthenticationConditional, Object> {

    /**
     * Selected property name for validation
     */
	private String selected;
    /**
     * Required properties
     */
    private String[] required;
    /**
     * Error message if validation failed
     */
    private String message;
    /**
     * Group of possible values ​​of the selected property to perform the validation
     */
    private String[] values;

    /**
     * Initialize constraint validator properties 
     */
    @Override
    public void initialize(AuthenticationConditional requiredIfChecked) {
        selected = requiredIfChecked.selected();
        required = requiredIfChecked.required();
        message = requiredIfChecked.message();
        values = requiredIfChecked.values();
    }
	
    /**
     * Validates if a certain property of an object has a value contained in a certain group of values, if so, a certain group of properties is mandatory
     * If validation result is false 
     * @param objectToValidate object on which the validations are performed
     * @param context contextual data and operation when applying a given constraint validator
     * @return validation result
     */
	@Override
	public boolean isValid(Object objectToValidate, ConstraintValidatorContext context) {
		
        boolean valid = true;
        try {
            Object actualValue = BeanUtils.getProperty(objectToValidate, selected);
            if (Arrays.asList(values).contains(actualValue)) {
                for (String propName : required) {
                	boolean propertyValid = true;
                    Object requiredValue = BeanUtils.getProperty(objectToValidate, propName);
                    propertyValid = requiredValue != null && !isEmpty(requiredValue);
                    if (!propertyValid) {
                    	valid = false;
                    	HibernateConstraintValidatorContext hibernateConstraintValidatorContext = context.unwrap( HibernateConstraintValidatorContext.class );
                    	hibernateConstraintValidatorContext.addMessageParameter("selected", selected);
                    	hibernateConstraintValidatorContext.addMessageParameter("value", actualValue);
                    	hibernateConstraintValidatorContext.addMessageParameter("attribute", propName);
                    	hibernateConstraintValidatorContext.disableDefaultConstraintViolation();
                    	hibernateConstraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode(propName).addConstraintViolation();
                    }
                }
            }
        } catch (IllegalAccessException|NoSuchMethodException|InvocationTargetException e) {
            log.error("Error", e);
            return false;
        }
        return valid;
	}

}
