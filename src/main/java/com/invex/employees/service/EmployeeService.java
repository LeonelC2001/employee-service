package com.invex.employees.service;

import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponseDto> findAll();

    EmployeeResponseDto findById(Long id);

    List<EmployeeResponseDto> create(List<EmployeeRequestDto> employees);

    EmployeeResponseDto update(Long id, EmployeeRequestDto employee);

    void delete(Long id);

    List<EmployeeResponseDto> searchByName(String name);
}
