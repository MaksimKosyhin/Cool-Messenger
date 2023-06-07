package com.example.end.domain.validation;

import com.example.end.domain.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.util.Set;

public class RemaindersValidator implements ConstraintValidator<Remainders, Set<User.Remainder>> {
    @Override
    public boolean isValid(Set<User.Remainder> value, ConstraintValidatorContext context) {
        var now = LocalDateTime.now();

        for(User.Remainder remainder: value) {
            if(remainder.getNotifyAt().isBefore(now)) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("can't add remainder for a date before now")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
