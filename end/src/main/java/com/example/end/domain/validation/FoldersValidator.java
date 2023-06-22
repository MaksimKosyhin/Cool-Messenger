package com.example.end.domain.validation;

import com.mongodb.DBRef;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;
import java.util.Set;

public class FoldersValidator implements ConstraintValidator<Folders, Map<String, Set<String>>> {

    @Override
    public boolean isValid(Map<String, Set<String>> folders, ConstraintValidatorContext context) {
        if(folders == null) {
            return true;
        }

        var all = folders.get("all");

        if(all == null) {
            return false;
        }

        for(Set<String> ids: folders.values()) {
            if(!all.containsAll(ids)) {
                return false;
            }
        }

        return true;
    }
}
