package com.restaurant.employee.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.employee.persistence.entity.EmployeeEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:employeerepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=employee",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void findByTenantPropertyAndEmployeeIdReturnsMatchingEmployee() {
        EmployeeEntity entity = employee("emp-001", "John Walker", "WAITER", true);
        employeeRepository.save(entity);

        assertThat(employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId("bikini-bottom", "krusty-krab", "emp-001"))
                .get()
                .extracting(EmployeeEntity::getEmployeeId, EmployeeEntity::getFullName)
                .containsExactly("emp-001", "John Walker");
    }

    @Test
    void availableWaiterLookupFiltersAndSortsByName() {
        employeeRepository.saveAll(List.of(
                employee("emp-001", "Neha", "WAITER", true),
                employee("emp-002", "Anu", "WAITER", true),
                employee("emp-003", "Karthi", "COOK", true),
                employee("emp-004", "Blocked", "WAITER", false)
        ));

        List<EmployeeEntity> employees = employeeRepository
                .findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
                        "bikini-bottom",
                        "krusty-krab",
                        "WAITER",
                        "ACTIVE"
                );

        assertThat(employees).extracting(EmployeeEntity::getEmployeeId).containsExactly("emp-002", "emp-001");
    }

    private EmployeeEntity employee(String employeeId, String name, String roleCode, boolean available) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setEmployeeId(employeeId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setEmployeeCode("KRK-" + employeeId);
        entity.setFullName(name);
        entity.setRoleCode(roleCode);
        entity.setShiftName("Day Shift");
        entity.setSalaryAmount(BigDecimal.valueOf(25000));
        entity.setEmploymentStatus("ACTIVE");
        entity.setAvailable(available);
        entity.setHireDate(LocalDate.parse("2026-06-15"));
        return entity;
    }
}
