package com.restaurant.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;

public record UpdateEmployeeRequest(
        String name,
        EmployeeRole role,
        @Email(message = "Enter a valid email address")
        String email,
        String phoneNumber,
        String shiftName,
        @DecimalMin(value = "0.0", inclusive = true, message = "Salary cannot be negative")
        BigDecimal salaryAmount,
        Boolean available,
        String employmentStatus
) {
}
