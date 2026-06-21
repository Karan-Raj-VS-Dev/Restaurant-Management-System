package com.restaurant.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.employee.persistence.entity.EmployeeEntity;
import com.restaurant.employee.persistence.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository);
    }

    @Test
    void listEmployeesMapsEntitiesToResponses() {
        when(employeeRepository.findByTenantIdAndPropertyIdOrderByFullNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(employeeEntity("emp-001", "John Walker", "WAITER", true)));

        List<EmployeeResponse> responses = employeeService.listEmployees("bikini-bottom", "krusty-krab");

        assertThat(responses).singleElement()
                .extracting(EmployeeResponse::employeeId, EmployeeResponse::name, EmployeeResponse::role)
                .containsExactly("emp-001", "John Walker", EmployeeRole.WAITER);
    }

    @Test
    void assignWaiterReturnsFirstAvailableWaiter() {
        when(employeeRepository.findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
                "bikini-bottom", "krusty-krab", "WAITER", "ACTIVE"))
                .thenReturn(List.of(
                        employeeEntity("emp-001", "Anu", "WAITER", true),
                        employeeEntity("emp-002", "Neha", "WAITER", true)
                ));

        EmployeeResponse response = employeeService.assignWaiter("bikini-bottom", "krusty-krab");

        assertThat(response.employeeId()).isEqualTo("emp-001");
        assertThat(response.name()).isEqualTo("Anu");
    }

    @Test
    void assignCookThrowsWhenNoCookIsAvailable() {
        when(employeeRepository.findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
                "bikini-bottom", "krusty-krab", "COOK", "ACTIVE"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> employeeService.assignCook("bikini-bottom", "krusty-krab"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No active cook is available");
    }

    @Test
    void assignWaiterThrowsWhenNoWaiterIsAvailable() {
        when(employeeRepository.findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
                "bikini-bottom", "krusty-krab", "WAITER", "ACTIVE"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> employeeService.assignWaiter("bikini-bottom", "krusty-krab"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No active waiter is available");
    }

    @Test
    void createEmployeeAppliesDefaultsAndNormalization() {
        when(employeeRepository.save(any(EmployeeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse response = employeeService.createEmployee(
                "bikini-bottom",
                "krusty-krab",
                new CreateEmployeeRequest(
                        "  John Walker  ",
                        EmployeeRole.WAITER,
                        "  john@example.com  ",
                        " 9999999999 ",
                        "",
                        BigDecimal.valueOf(25000.236),
                        null,
                        ""
                )
        );

        ArgumentCaptor<EmployeeEntity> captor = ArgumentCaptor.forClass(EmployeeEntity.class);
        verify(employeeRepository).save(captor.capture());
        EmployeeEntity saved = captor.getValue();

        assertThat(saved.getFullName()).isEqualTo("John Walker");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getPhoneNumber()).isEqualTo("9999999999");
        assertThat(saved.getShiftName()).isEqualTo("Normal Shift");
        assertThat(saved.getEmploymentStatus()).isEqualTo("ACTIVE");
        assertThat(saved.isAvailable()).isTrue();
        assertThat(saved.getSalaryAmount()).isEqualByComparingTo("25000.24");
        assertThat(response.name()).isEqualTo("John Walker");
    }

    @Test
    void updateEmployeeRejectsNegativeSalary() {
        when(employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId("bikini-bottom", "krusty-krab", "emp-001"))
                .thenReturn(Optional.of(employeeEntity("emp-001", "John Walker", "WAITER", true)));

        assertThatThrownBy(() -> employeeService.updateEmployee(
                "bikini-bottom",
                "krusty-krab",
                "emp-001",
                new UpdateEmployeeRequest(null, null, null, null, null, BigDecimal.valueOf(-1), null, null)
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Salary cannot be negative");
    }

    @Test
    void updateEmployeeAppliesRoleAvailabilityAndClearsBlankContacts() {
        EmployeeEntity entity = employeeEntity("emp-001", "John Walker", "WAITER", true);
        when(employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId("bikini-bottom", "krusty-krab", "emp-001"))
                .thenReturn(Optional.of(entity));
        when(employeeRepository.save(any(EmployeeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse response = employeeService.updateEmployee(
                "bikini-bottom",
                "krusty-krab",
                "emp-001",
                new UpdateEmployeeRequest("Chef Anu", EmployeeRole.COOK, "   ", "   ", "Night Shift", BigDecimal.valueOf(31500), false, "inactive")
        );

        assertThat(entity.getFullName()).isEqualTo("Chef Anu");
        assertThat(entity.getRoleCode()).isEqualTo("COOK");
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getPhoneNumber()).isNull();
        assertThat(entity.getShiftName()).isEqualTo("Night Shift");
        assertThat(entity.getSalaryAmount()).isEqualByComparingTo("31500.00");
        assertThat(entity.isAvailable()).isFalse();
        assertThat(entity.getEmploymentStatus()).isEqualTo("INACTIVE");
        assertThat(response.role()).isEqualTo(EmployeeRole.COOK);
        assertThat(response.available()).isFalse();
    }

    @Test
    void createEmployeeRejectsNegativeSalary() {
        assertThatThrownBy(() -> employeeService.createEmployee(
                "bikini-bottom",
                "krusty-krab",
                new CreateEmployeeRequest(
                        "John Walker",
                        EmployeeRole.WAITER,
                        null,
                        null,
                        null,
                        BigDecimal.valueOf(-10),
                        true,
                        "ACTIVE"
                )
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Salary cannot be negative");
    }

    @Test
    void listEmployeesFallsBackToWaiterRoleForUnknownStoredRoleCode() {
        when(employeeRepository.findByTenantIdAndPropertyIdOrderByFullNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(employeeEntity("emp-001", "Mystery User", "UNKNOWN_ROLE", true)));

        List<EmployeeResponse> responses = employeeService.listEmployees("bikini-bottom", "krusty-krab");

        assertThat(responses).singleElement()
                .extracting(EmployeeResponse::role)
                .isEqualTo(EmployeeRole.WAITER);
    }

    @Test
    void getEmployeeThrowsWhenEmployeeIsMissing() {
        when(employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId("bikini-bottom", "krusty-krab", "missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployee("bikini-bottom", "krusty-krab", "missing"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Employee was not found");
    }

    @Test
    void deleteEmployeeRemovesExistingEntity() {
        EmployeeEntity entity = employeeEntity("emp-001", "John Walker", "WAITER", true);
        when(employeeRepository.findByTenantIdAndPropertyIdAndEmployeeId("bikini-bottom", "krusty-krab", "emp-001"))
                .thenReturn(Optional.of(entity));

        employeeService.deleteEmployee("bikini-bottom", "krusty-krab", "emp-001");

        verify(employeeRepository).delete(entity);
    }

    private EmployeeEntity employeeEntity(String employeeId, String fullName, String roleCode, boolean available) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setEmployeeId(employeeId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setEmployeeCode("KRK-" + employeeId);
        entity.setFullName(fullName);
        entity.setRoleCode(roleCode);
        entity.setShiftName("Day Shift");
        entity.setSalaryAmount(BigDecimal.valueOf(25000));
        entity.setEmploymentStatus("ACTIVE");
        entity.setAvailable(available);
        entity.setHireDate(LocalDate.parse("2026-06-15"));
        entity.setEmail("john@example.com");
        entity.setPhoneNumber("9999999999");
        return entity;
    }
}
