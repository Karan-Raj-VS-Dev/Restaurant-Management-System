package com.restaurant.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        controller = new EmployeeController(employeeService);
    }

    @Test
    void listEmployeesPrefersPathScopedTenantAndProperty() {
        List<EmployeeResponse> expected = List.of(employeeResponse("emp-001", EmployeeRole.WAITER));
        when(employeeService.listEmployees("tenant-path", "property-path")).thenReturn(expected);

        List<EmployeeResponse> response = controller.listEmployees("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(employeeService).listEmployees("tenant-path", "property-path");
    }

    @Test
    void assignEndpointsUseDefaultScopeWhenNothingProvided() {
        EmployeeResponse waiter = employeeResponse("emp-waiter", EmployeeRole.WAITER);
        EmployeeResponse cook = employeeResponse("emp-cook", EmployeeRole.COOK);
        when(employeeService.assignWaiter("bikini-bottom", "krusty-krab")).thenReturn(waiter);
        when(employeeService.assignCook("bikini-bottom", "krusty-krab")).thenReturn(cook);

        assertThat(controller.assignWaiter(null, null, null, null)).isEqualTo(waiter);
        assertThat(controller.assignCook(null, null, null, null)).isEqualTo(cook);

        verify(employeeService).assignWaiter("bikini-bottom", "krusty-krab");
        verify(employeeService).assignCook("bikini-bottom", "krusty-krab");
    }

    @Test
    void createUpdateAndDeleteDelegateToService() {
        CreateEmployeeRequest createRequest = new CreateEmployeeRequest(
                "John Walker",
                EmployeeRole.WAITER,
                "john@example.com",
                "9999999999",
                "Day Shift",
                BigDecimal.valueOf(25000),
                true,
                "active"
        );
        UpdateEmployeeRequest updateRequest = new UpdateEmployeeRequest(
                "John Walker Senior",
                EmployeeRole.WAITER,
                "john.senior@example.com",
                "8888888888",
                "Night Shift",
                BigDecimal.valueOf(27000),
                false,
                "inactive"
        );
        EmployeeResponse created = employeeResponse("emp-123", EmployeeRole.WAITER);
        EmployeeResponse updated = employeeResponse("emp-123", EmployeeRole.WAITER);
        when(employeeService.createEmployee("bikini-bottom", "krusty-krab", createRequest)).thenReturn(created);
        when(employeeService.updateEmployee("bikini-bottom", "krusty-krab", "emp-123", updateRequest)).thenReturn(updated);

        assertThat(controller.createEmployee(null, null, null, null, createRequest)).isEqualTo(created);
        assertThat(controller.updateEmployee("emp-123", null, null, null, null, updateRequest)).isEqualTo(updated);
        controller.deleteEmployee("emp-123", null, null, null, null);

        verify(employeeService).createEmployee("bikini-bottom", "krusty-krab", createRequest);
        verify(employeeService).updateEmployee("bikini-bottom", "krusty-krab", "emp-123", updateRequest);
        verify(employeeService).deleteEmployee("bikini-bottom", "krusty-krab", "emp-123");
    }

    private EmployeeResponse employeeResponse(String employeeId, EmployeeRole role) {
        return new EmployeeResponse(
                employeeId,
                "bikini-bottom",
                "krusty-krab",
                "John Walker",
                role,
                true,
                "john@example.com",
                "9999999999",
                "Day Shift",
                BigDecimal.valueOf(25000),
                "ACTIVE",
                LocalDate.parse("2026-06-15")
        );
    }
}
