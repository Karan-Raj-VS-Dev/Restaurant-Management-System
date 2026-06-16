package com.restaurant.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.restaurant.customer.persistence.entity.CustomerEntity;
import com.restaurant.customer.persistence.repository.CustomerRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private final Map<String, CustomerEntity> customersById = new LinkedHashMap<>();

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);

        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity entity = invocation.getArgument(0);
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }
            if (entity.getUpdatedAt() == null) {
                entity.setUpdatedAt(entity.getCreatedAt());
            }
            customersById.put(entity.getCustomerId(), entity);
            return entity;
        });
        when(customerRepository.findByTenantIdAndPropertyIdAndCustomerId(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(customersById.get(invocation.getArgument(2))));
        when(customerRepository.findByTenantIdAndPropertyIdOrderByCreatedAtDesc(anyString(), anyString()))
                .thenAnswer(invocation -> customersById.values().stream().toList());
        when(customerRepository.findByTenantIdAndPropertyIdAndPhoneNumber(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> customersById.values().stream()
                        .filter(customer -> invocation.getArgument(0).equals(customer.getTenantId()))
                        .filter(customer -> invocation.getArgument(1).equals(customer.getPropertyId()))
                        .filter(customer -> invocation.getArgument(2).equals(customer.getPhoneNumber()))
                        .findFirst());
    }

    @Test
    void lookupCustomerByPhoneReturnsExistingCustomerMetadata() {
        CustomerEntity existing = CustomerEntity.create("cust-001", "bikini-bottom", "krusty-krab", "Dom", "8901913123");
        customersById.put(existing.getCustomerId(), existing);

        CustomerLookupResponse response = customerService.lookupCustomerByPhone("bikini-bottom", "krusty-krab", "8901913123");

        assertThat(response.existing()).isTrue();
        assertThat(response.customerId()).isEqualTo("cust-001");
        assertThat(response.name()).isEqualTo("Dom");
    }

    @Test
    void lookupCustomerByPhoneReturnsNewMarkerWhenPhoneDoesNotExist() {
        CustomerLookupResponse response = customerService.lookupCustomerByPhone("bikini-bottom", "krusty-krab", "9990001111");

        assertThat(response.existing()).isFalse();
        assertThat(response.customerId()).isNull();
        assertThat(response.phoneNumber()).isEqualTo("9990001111");
    }

    @Test
    void lookupCustomerByPhoneIsScopedToTenantAndProperty() {
        customersById.put("cust-krusty", CustomerEntity.create("cust-krusty", "bikini-bottom", "krusty-krab", "Dom", "8901913123"));
        customersById.put("cust-chum", CustomerEntity.create("cust-chum", "bikini-bottom", "chum-bucket", "Plankton", "8901913123"));

        CustomerLookupResponse response = customerService.lookupCustomerByPhone("bikini-bottom", "krusty-krab", "8901913123");

        assertThat(response.existing()).isTrue();
        assertThat(response.customerId()).isEqualTo("cust-krusty");
        assertThat(response.name()).isEqualTo("Dom");
    }

    @Test
    void createCustomerCreatesNewCustomerAndUsesPhoneAsLookupKey() {
        CustomerResponse response = customerService.createCustomer(
                "bikini-bottom",
                "krusty-krab",
                new CreateCustomerRequest("Dom", "8901913123")
        );

        assertThat(response.phoneNumber()).isEqualTo("8901913123");
        assertThat(response.name()).isEqualTo("Dom");
        assertThat(customersById).hasSize(1);
    }

    @Test
    void createCustomerUpdatesExistingNameButDoesNotBlankItOut() {
        CustomerEntity existing = CustomerEntity.create("cust-002", "bikini-bottom", "krusty-krab", "Dominic", "8901913123");
        customersById.put(existing.getCustomerId(), existing);

        CustomerResponse renamed = customerService.createCustomer(
                "bikini-bottom",
                "krusty-krab",
                new CreateCustomerRequest("Dom", "8901913123")
        );
        CustomerResponse preserved = customerService.createCustomer(
                "bikini-bottom",
                "krusty-krab",
                new CreateCustomerRequest("   ", "8901913123")
        );

        assertThat(renamed.customerId()).isEqualTo("cust-002");
        assertThat(renamed.name()).isEqualTo("Dom");
        assertThat(preserved.name()).isEqualTo("Dom");
    }

    @Test
    void listAndGetCustomerExposeStoredRecords() {
        CustomerEntity first = CustomerEntity.create("cust-003", "bikini-bottom", "krusty-krab", "Sandy", "1111111111");
        CustomerEntity second = CustomerEntity.create("cust-004", "bikini-bottom", "krusty-krab", null, "2222222222");
        customersById.put(first.getCustomerId(), first);
        customersById.put(second.getCustomerId(), second);

        List<CustomerResponse> customers = customerService.listCustomers("bikini-bottom", "krusty-krab");
        CustomerResponse fetched = customerService.getCustomer("bikini-bottom", "krusty-krab", "cust-003");

        assertThat(customers).hasSize(2);
        assertThat(fetched.name()).isEqualTo("Sandy");
        assertThat(fetched.phoneNumber()).isEqualTo("1111111111");
    }

    @Test
    void createCustomerRequiresPhoneNumber() {
        assertThatThrownBy(() -> customerService.createCustomer(
                "bikini-bottom",
                "krusty-krab",
                new CreateCustomerRequest("Dom", "   ")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Phone number is required");
    }
}
