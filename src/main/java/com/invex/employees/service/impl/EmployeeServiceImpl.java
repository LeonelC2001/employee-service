package com.invex.employees.service.impl;

import com.invex.employees.constants.EmployeeConstants;
import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.entity.Employee;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.mapper.EmployeeMapper;
import com.invex.employees.repository.EmployeeRepository;
import com.invex.employees.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> findAll() {
        log.info("Fetching all employees");
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto findById(Long id) {
        log.info("Fetching employee with id: {}", id);
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND + id));
        return mapper.toResponseDto(employee);
    }

    @Override
    @Transactional
    public List<EmployeeResponseDto> create(List<EmployeeRequestDto> employees) {
        log.info("Creating {} employee(s)", employees.size());
        List<Employee> entities = employees.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        List<Employee> saved = repository.saveAll(entities);
        log.info("Successfully created {} employee(s)", saved.size());
        return saved.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {
        log.info("Updating employee with id: {}", id);
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND + id));
        mapper.updateEntityFromDto(dto, employee);
        Employee updated = repository.save(employee);
        log.info("Employee {} updated successfully", id);
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting employee with id: {}", id);
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND + id);
        }
        repository.deleteById(id);
        log.info("Employee {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> searchByName(String name) {
        log.info("Searching employees by name: {}", name);
        return repository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
