package com.restaurant.employee.bootstrap;

import com.restaurant.employee.persistence.entity.EmployeeEntity;
import com.restaurant.employee.persistence.entity.EmployeeShiftEntity;
import com.restaurant.employee.persistence.repository.EmployeeRepository;
import com.restaurant.employee.persistence.repository.EmployeeShiftRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class EmployeeSeedInitializer implements CommandLineRunner {

    private static final String TENANT_ID = "bikini-bottom";
    private static final String PROPERTY_ID = "krusty-krab";

    private final EmployeeRepository employeeRepository;
    private final EmployeeShiftRepository employeeShiftRepository;

    public EmployeeSeedInitializer(
            EmployeeRepository employeeRepository,
            EmployeeShiftRepository employeeShiftRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeShiftRepository = employeeShiftRepository;
    }

    @Override
    public void run(String... args) {
        EmployeeEntity entity = employeeRepository.findById("emp-neha-001").orElseGet(EmployeeEntity::new);
        if (entity.getEmployeeId() == null) {
            entity.setEmployeeId("emp-neha-001");
            entity.setTenantId(TENANT_ID);
            entity.setPropertyId(PROPERTY_ID);
            entity.setEmployeeCode("EMP-NEHA-001");
            entity.setFullName("Neha");
            entity.setEmail("neha@krustykrab.local");
            entity.setPhoneNumber("+919999999999");
            entity.setRoleCode("WAITER");
            entity.setShiftName("Normal Shift");
            entity.setSalaryAmount(new BigDecimal("15000.00").setScale(2, RoundingMode.HALF_UP));
            entity.setEmploymentStatus("ACTIVE");
            entity.setAvailable(true);
            entity.setHireDate(LocalDate.now());
            employeeRepository.save(entity);
        }

        EmployeeShiftEntity shift = new EmployeeShiftEntity();
        shift.setShiftId("shift-neha-normal");
        shift.setEmployeeId(entity.getEmployeeId());
        shift.setTenantId(TENANT_ID);
        shift.setPropertyId(PROPERTY_ID);
        shift.setShiftName("Normal Shift");
        shift.setShiftDate(LocalDate.now());
        shift.setStartTime(LocalDate.now().atTime(9, 0).toInstant(ZoneOffset.UTC));
        shift.setEndTime(LocalDate.now().atTime(18, 0).toInstant(ZoneOffset.UTC));
        shift.setShiftStatus("SCHEDULED");
        if (!employeeShiftRepository.existsById("shift-neha-normal")) {
            employeeShiftRepository.save(shift);
        }
    }
}
