package com.restaurant.customer;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    public List<CustomerResponse> listCustomers(String tenantId, String propertyId) {
        return List.of(
                new CustomerResponse("cust-1001", tenantId, propertyId, "Aarav Shah", "+91-9000000001", "aarav@example.com", Instant.now()),
                new CustomerResponse("cust-1002", tenantId, propertyId, "Anika Rao", "+91-9000000002", "anika@example.com", Instant.now())
        );
    }

    public CustomerResponse getCustomer(String tenantId, String propertyId, String customerId) {
        return new CustomerResponse(customerId, tenantId, propertyId, "Walk-in Customer", "+91-9000009999", "walkin@example.com", Instant.now());
    }

    public CustomerResponse createCustomer(String tenantId, String propertyId, CreateCustomerRequest request) {
        return new CustomerResponse(
                "cust-" + UUID.randomUUID(),
                tenantId,
                propertyId,
                request.name(),
                request.phoneNumber(),
                request.email(),
                Instant.now()
        );
    }
}
