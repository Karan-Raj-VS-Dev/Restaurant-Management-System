package com.restaurant.customer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.restaurant.customer.persistence.entity.CustomerEntity;
import com.restaurant.customer.persistence.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> listCustomers(String tenantId, String propertyId) {
        return customerRepository.findByTenantIdAndPropertyIdOrderByCreatedAtDesc(tenantId, propertyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomer(String tenantId, String propertyId, String customerId) {
        CustomerEntity entity = customerRepository.findByTenantIdAndPropertyIdAndCustomerId(tenantId, propertyId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer was not found."));
        return toResponse(entity);
    }

    public CustomerLookupResponse lookupCustomerByPhone(String tenantId, String propertyId, String phoneNumber) {
        String normalizedPhone = normalizePhone(phoneNumber);
        return customerRepository.findByTenantIdAndPropertyIdAndPhoneNumber(tenantId, propertyId, normalizedPhone)
                .map(entity -> new CustomerLookupResponse(
                        true,
                        entity.getCustomerId(),
                        entity.getFullName(),
                        entity.getPhoneNumber()
                ))
                .orElseGet(() -> new CustomerLookupResponse(false, null, null, normalizedPhone));
    }

    public CustomerResponse createCustomer(String tenantId, String propertyId, CreateCustomerRequest request) {
        String normalizedPhone = normalizePhone(request.phoneNumber());
        String normalizedName = blankToNull(request.name());
        CustomerEntity entity = customerRepository.findByTenantIdAndPropertyIdAndPhoneNumber(tenantId, propertyId, normalizedPhone)
                .orElseGet(() -> CustomerEntity.create(
                        "cust-" + UUID.randomUUID(),
                        tenantId,
                        propertyId,
                        normalizedName,
                        normalizedPhone
                ));
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setPhoneNumber(normalizedPhone);
        if (normalizedName != null) {
            entity.setFullName(normalizedName);
        }
        return toResponse(customerRepository.save(entity));
    }

    private CustomerResponse toResponse(CustomerEntity entity) {
        return new CustomerResponse(
                entity.getCustomerId(),
                entity.getTenantId(),
                entity.getPropertyId(),
                entity.getFullName(),
                entity.getPhoneNumber(),
                entity.getCreatedAt()
        );
    }

    private String normalizePhone(String phoneNumber) {
        String normalized = phoneNumber == null ? "" : phoneNumber.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required.");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
