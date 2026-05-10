package com.restaurant.employee;

import com.restaurant.employee.persistence.entity.EmployeeEntity;
import com.restaurant.employee.persistence.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeResponse> listEmployees(String tenantId, String propertyId) {
        return employeeRepository.findByTenantIdAndPropertyIdOrderByFullNameAsc(tenantId, propertyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponse assignWaiter(String tenantId, String propertyId) {
        return employeeRepository
                .findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
                        tenantId,
                        propertyId,
                        EmployeeRole.WAITER.name(),
                        "ACTIVE"
                )
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active waiter is available for this property"));
    }

    public EmployeeResponse assignCook(String tenantId, String propertyId) {
        return employeeRepository
                .findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
                        tenantId,
                        propertyId,
                        EmployeeRole.COOK.name(),
                        "ACTIVE"
                )
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active cook is available for this property"));
    }

    public EmployeeResponse getEmployee(String tenantId, String propertyId, String employeeId) {
        return employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId(tenantId, propertyId, employeeId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee was not found"));
    }

    public EmployeeResponse createEmployee(String tenantId, String propertyId, CreateEmployeeRequest request) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setEmployeeId("emp-" + randomId());
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setEmployeeCode(buildEmployeeCode(propertyId, request.name()));
        entity.setFullName(request.name().trim());
        entity.setEmail(blankToNull(request.email()));
        entity.setPhoneNumber(blankToNull(request.phoneNumber()));
        entity.setRoleCode(request.role().name());
        entity.setShiftName(normalizeShiftName(request.shiftName()));
        entity.setSalaryAmount(normalizeSalary(request.salaryAmount()));
        entity.setEmploymentStatus(normalizeStatus(request.employmentStatus()));
        entity.setAvailable(request.available() == null || request.available());
        entity.setHireDate(LocalDate.now());
        return toResponse(employeeRepository.save(entity));
    }

    public EmployeeResponse updateEmployee(String tenantId, String propertyId, String employeeId, UpdateEmployeeRequest request) {
        EmployeeEntity entity = employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId(tenantId, propertyId, employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee was not found"));

        if (request.name() != null && !request.name().isBlank()) {
            entity.setFullName(request.name().trim());
        }
        if (request.role() != null) {
            entity.setRoleCode(request.role().name());
        }
        if (request.email() != null) {
            entity.setEmail(blankToNull(request.email()));
        }
        if (request.phoneNumber() != null) {
            entity.setPhoneNumber(blankToNull(request.phoneNumber()));
        }
        if (request.shiftName() != null && !request.shiftName().isBlank()) {
            entity.setShiftName(normalizeShiftName(request.shiftName()));
        }
        if (request.salaryAmount() != null) {
            entity.setSalaryAmount(normalizeSalary(request.salaryAmount()));
        }
        if (request.available() != null) {
            entity.setAvailable(request.available());
        }
        if (request.employmentStatus() != null && !request.employmentStatus().isBlank()) {
            entity.setEmploymentStatus(normalizeStatus(request.employmentStatus()));
        }
        return toResponse(employeeRepository.save(entity));
    }

    public void deleteEmployee(String tenantId, String propertyId, String employeeId) {
        EmployeeEntity entity = employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId(tenantId, propertyId, employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee was not found"));
        employeeRepository.delete(entity);
    }

    private EmployeeResponse toResponse(EmployeeEntity entity) {
        return new EmployeeResponse(
                entity.getEmployeeId(),
                entity.getTenantId(),
                entity.getPropertyId(),
                entity.getFullName(),
                parseRole(entity.getRoleCode()),
                entity.isAvailable(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getShiftName(),
                entity.getSalaryAmount(),
                entity.getEmploymentStatus(),
                entity.getHireDate()
        );
    }

    private EmployeeRole parseRole(String roleCode) {
        try {
            return EmployeeRole.valueOf(roleCode.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return EmployeeRole.WAITER;
        }
    }

    private String buildEmployeeCode(String propertyId, String name) {
        String propertyCode = propertyId.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String nameCode = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return propertyCode + "-" + nameCode + "-" + randomId().substring(0, 4).toUpperCase(Locale.ROOT);
    }

    private String normalizeShiftName(String shiftName) {
        return shiftName == null || shiftName.isBlank() ? "Normal Shift" : shiftName.trim();
    }

    private BigDecimal normalizeSalary(BigDecimal salaryAmount) {
        if (salaryAmount == null) {
            return BigDecimal.ZERO;
        }
        if (salaryAmount.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary cannot be negative");
        }
        return salaryAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
