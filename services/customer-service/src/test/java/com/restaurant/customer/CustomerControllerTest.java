package com.restaurant.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private CustomerController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerController(customerService);
    }

    @Test
    void listCustomersUsesPathScopeOverRequestScope() {
        List<CustomerResponse> expected = List.of(customer("cust-001", "Dom"));
        when(customerService.listCustomers("tenant-path", "property-path")).thenReturn(expected);

        List<CustomerResponse> response = controller.listCustomers("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(customerService).listCustomers("tenant-path", "property-path");
    }

    @Test
    void listCustomersFallsBackToDefaultScope() {
        List<CustomerResponse> expected = List.of(customer("cust-001", "Dom"));
        when(customerService.listCustomers("bikini-bottom", "krusty-krab")).thenReturn(expected);

        List<CustomerResponse> response = controller.listCustomers(null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(customerService).listCustomers("bikini-bottom", "krusty-krab");
    }

    @Test
    void getCustomerDelegatesUsingResolvedScope() {
        CustomerResponse expected = customer("cust-002", "Bubble Bass");
        when(customerService.getCustomer("bikini-bottom", "krusty-krab", "cust-002")).thenReturn(expected);

        CustomerResponse response = controller.getCustomer("cust-002", null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(customerService).getCustomer("bikini-bottom", "krusty-krab", "cust-002");
    }

    @Test
    void lookupCustomerByPhoneUsesRequestScopeWhenPathScopeMissing() {
        CustomerLookupResponse expected = new CustomerLookupResponse(true, "cust-003", "Larry", "9000000000");
        when(customerService.lookupCustomerByPhone("tenant-query", "property-query", "9000000000")).thenReturn(expected);

        CustomerLookupResponse response = controller.lookupCustomerByPhone(null, null, "tenant-query", "property-query", "9000000000");

        assertThat(response).isEqualTo(expected);
        verify(customerService).lookupCustomerByPhone("tenant-query", "property-query", "9000000000");
    }

    @Test
    void createCustomerDelegatesUsingResolvedScope() {
        CreateCustomerRequest request = new CreateCustomerRequest("Dom", "8901913123");
        CustomerResponse expected = customer("cust-004", "Dom");
        when(customerService.createCustomer("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        CustomerResponse response = controller.createCustomer(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(customerService).createCustomer("bikini-bottom", "krusty-krab", request);
    }

    private CustomerResponse customer(String id, String name) {
        return new CustomerResponse(id, "bikini-bottom", "krusty-krab", name, "8901913123", Instant.parse("2026-06-15T10:15:30Z"));
    }
}
