package com.example.end.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Map;

public class FoldersValidator implements ConstraintValidator<Folders, Map<String, List<String>>> {

    @Override
    public boolean isValid(Map<String, List<String>> value, ConstraintValidatorContext context) {
        var all = value.get("all");

        if(all == null) {
            return false;
        }

        for(List<String> ids: value.values()) {
            if(!all.containsAll(ids)) {
                return false;
            }
        }

        return true;
    }
}
