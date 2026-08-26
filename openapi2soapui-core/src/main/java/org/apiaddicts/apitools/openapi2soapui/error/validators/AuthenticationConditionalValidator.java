package org.apiaddicts.apitools.openapi2soapui.error.validators;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Slf4j
public class AuthenticationConditionalValidator implements ConstraintValidator<AuthenticationConditional, Object> {

	private String selected;

    private String[] required;

    private String message;

    private String[] values;

    @Override
    public void initialize(AuthenticationConditional requiredIfChecked) {
        selected = requiredIfChecked.selected();
        required = requiredIfChecked.required();
        message = requiredIfChecked.message();
        values = requiredIfChecked.values();
    }

    /**
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

    /**
     * @param value property value to check
     * @return true if the value is null or an empty CharSequence
     */
    private static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof CharSequence charSequence) return charSequence.isEmpty();
        return false;
    }

}
