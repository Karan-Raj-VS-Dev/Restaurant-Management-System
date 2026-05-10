package com.restaurant.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateEmployeeRequest(
        @NotBlank(message = "Employee name is required")
        String name,
        @NotNull(message = "Role is required")
        EmployeeRole role,
        @Email(message = "Enter a valid email address")
        String email,
        String phoneNumber,
        @NotBlank(message = "Shift name is required")
        String shiftName,
        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Salary cannot be negative")
        BigDecimal salaryAmount,
        Boolean available,
        String employmentStatus
) {
}
