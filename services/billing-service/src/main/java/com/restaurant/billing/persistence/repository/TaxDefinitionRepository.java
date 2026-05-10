package com.restaurant.billing.persistence.repository;

import com.restaurant.billing.persistence.entity.TaxDefinitionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxDefinitionRepository extends JpaRepository<TaxDefinitionEntity, String> {

    List<TaxDefinitionEntity> findByTenantIdAndPropertyIdOrderByTaxNameAsc(String tenantId, String propertyId);
}
