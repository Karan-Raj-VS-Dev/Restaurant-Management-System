package com.restaurant.customer.persistence.repository;

import com.restaurant.customer.persistence.entity.CustomerEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    List<CustomerEntity> findByTenantIdAndPropertyIdOrderByCreatedAtDesc(String tenantId, String propertyId);

    Optional<CustomerEntity> findByTenantIdAndPropertyIdAndCustomerId(String tenantId, String propertyId, String customerId);

    Optional<CustomerEntity> findByTenantIdAndPropertyIdAndPhoneNumber(String tenantId, String propertyId, String phoneNumber);
}
