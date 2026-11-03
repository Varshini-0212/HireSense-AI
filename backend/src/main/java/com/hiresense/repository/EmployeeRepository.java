package com.hiresense.repository;

import com.hiresense.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findAllByDepartmentIgnoreCase(String department, Pageable pageable);
    Page<Employee> findAllByAttrition(Boolean attrition, Pageable pageable);
}
