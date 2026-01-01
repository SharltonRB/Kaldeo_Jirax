package com.issuetracker.validation;

import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.entity.IssueType;
import com.issuetracker.repository.IssueTypeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator for epic hierarchy rules.
 * Validates that epic issues don't have parents and non-epic issues have parents.
 */
public class EpicHierarchyValidator implements ConstraintValidator<ValidEpicHierarchy, CreateIssueRequest> {

    @Autowired
    private IssueTypeRepository issueTypeRepository;

    @Override
    public void initialize(ValidEpicHierarchy constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(CreateIssueRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getIssueTypeId() == null) {
            return true; // Let other validators handle null checks
        }

        // Get the issue type to check if it's an epic
        IssueType issueType = issueTypeRepository.findById(request.getIssueTypeId()).orElse(null);
        if (issueType == null) {
            return true; // Let other validators handle invalid issue type
        }

        boolean isEpicType = "EPIC".equals(issueType.getName());
        boolean hasParent = request.getParentIssueId() != null;

        if (isEpicType && hasParent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Epic issues cannot have a parent issue")
                   .addConstraintViolation();
            return false;
        }

        if (!isEpicType && !hasParent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Non-epic issues must be assigned to an epic")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}