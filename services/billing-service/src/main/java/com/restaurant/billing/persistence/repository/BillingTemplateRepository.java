package com.restaurant.billing.persistence.repository;

import com.restaurant.billing.persistence.entity.BillingTemplateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingTemplateRepository extends JpaRepository<BillingTemplateEntity, String> {

    List<BillingTemplateEntity> findByTenantIdAndPropertyIdOrderByTemplateNameAsc(String tenantId, String propertyId);
}
