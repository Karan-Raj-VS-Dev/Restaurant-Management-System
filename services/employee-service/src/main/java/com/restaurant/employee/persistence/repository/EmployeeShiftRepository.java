package com.restaurant.employee.persistence.repository;

import com.restaurant.employee.persistence.entity.EmployeeShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeShiftRepository extends JpaRepository<EmployeeShiftEntity, String> {
}
