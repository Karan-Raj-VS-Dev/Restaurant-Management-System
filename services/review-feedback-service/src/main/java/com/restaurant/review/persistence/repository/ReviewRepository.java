package com.restaurant.review.persistence.repository;

import com.restaurant.review.persistence.entity.ReviewEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {

    List<ReviewEntity> findByTenantIdAndPropertyId(String tenantId, String propertyId);

    List<ReviewEntity> findByCustomerId(String customerId);
}
