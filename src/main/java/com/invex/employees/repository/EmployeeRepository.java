package com.invex.employees.repository;

import com.invex.employees.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(e.secondName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(e.paternalSurname) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(e.maternalSurname) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Employee> findByNameContainingIgnoreCase(@Param("name") String name);

    List<Employee> findAllByActiveTrue();
}
