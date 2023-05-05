package com.example.end.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Email
public @interface UniqueEmail {
    String message() default "user with this email already exists";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
