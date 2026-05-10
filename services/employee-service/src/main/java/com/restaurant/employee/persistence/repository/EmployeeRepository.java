package com.restaurant.employee.persistence.repository;

import com.restaurant.employee.persistence.entity.EmployeeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {

    long countByTenantIdAndPropertyId(String tenantId, String propertyId);

    Optional<EmployeeEntity> findByTenantIdAndPropertyIdAndEmployeeId(String tenantId, String propertyId, String employeeId);

    List<EmployeeEntity> findByTenantIdAndPropertyIdOrderByFullNameAsc(String tenantId, String propertyId);

    List<EmployeeEntity> findByTenantIdAndPropertyIdAndRoleCodeAndEmploymentStatusAndAvailableTrueOrderByFullNameAsc(
            String tenantId,
            String propertyId,
            String roleCode,
            String employmentStatus
    );
}
