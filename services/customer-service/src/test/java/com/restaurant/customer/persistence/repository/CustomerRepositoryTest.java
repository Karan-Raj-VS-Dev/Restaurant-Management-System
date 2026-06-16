package com.restaurant.customer.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.customer.persistence.entity.CustomerEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:customerrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=customer",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findByTenantPropertyAndPhoneNumberReturnsMatchingCustomer() {
        CustomerEntity entity = CustomerEntity.create("cust-001", "bikini-bottom", "krusty-krab", "Dom", "8901913123");
        customerRepository.save(entity);

        assertThat(customerRepository.findByTenantIdAndPropertyIdAndPhoneNumber("bikini-bottom", "krusty-krab", "8901913123"))
                .get()
                .extracting(CustomerEntity::getCustomerId, CustomerEntity::getFullName)
                .containsExactly("cust-001", "Dom");
    }

    @Test
    void listCustomersOrdersNewestFirst() {
        CustomerEntity older = CustomerEntity.create("cust-older", "bikini-bottom", "krusty-krab", "Older", "9000000001");
        older.setCreatedAt(Instant.parse("2026-06-15T08:00:00Z"));
        older.setUpdatedAt(Instant.parse("2026-06-15T08:00:00Z"));
        CustomerEntity newer = CustomerEntity.create("cust-newer", "bikini-bottom", "krusty-krab", "Newer", "9000000002");
        newer.setCreatedAt(Instant.parse("2026-06-15T10:00:00Z"));
        newer.setUpdatedAt(Instant.parse("2026-06-15T10:00:00Z"));
        customerRepository.saveAll(List.of(older, newer));

        List<CustomerEntity> customers = customerRepository.findByTenantIdAndPropertyIdOrderByCreatedAtDesc("bikini-bottom", "krusty-krab");

        assertThat(customers).extracting(CustomerEntity::getCustomerId).containsExactly("cust-newer", "cust-older");
    }

    @Test
    void phoneLookupIsScopedToTenantAndProperty() {
        CustomerEntity first = CustomerEntity.create("cust-krusty", "bikini-bottom", "krusty-krab", "Dom", "8901913123");
        CustomerEntity second = CustomerEntity.create("cust-chum", "bikini-bottom", "chum-bucket", "Plankton", "8901913123");
        customerRepository.saveAll(List.of(first, second));

        assertThat(customerRepository.findByTenantIdAndPropertyIdAndPhoneNumber("bikini-bottom", "krusty-krab", "8901913123"))
                .get()
                .extracting(CustomerEntity::getCustomerId, CustomerEntity::getFullName)
                .containsExactly("cust-krusty", "Dom");
    }
}
