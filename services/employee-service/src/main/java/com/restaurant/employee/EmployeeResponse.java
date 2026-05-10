package com.restaurant.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        String employeeId,
        String tenantId,
        String propertyId,
        String name,
        EmployeeRole role,
        boolean available,
        String email,
        String phoneNumber,
        String shiftName,
        BigDecimal salaryAmount,
        String employmentStatus,
        LocalDate hireDate
) {
}
