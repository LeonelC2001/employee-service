package com.invex.employees.controller;

import com.invex.employees.constants.EmployeeConstants;
import com.invex.employees.dto.ApiResponseWrapper;
import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(EmployeeConstants.API_BASE_PATH + EmployeeConstants.EMPLOYEES_PATH)
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management operations")
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping
    @Operation(summary = "Get all employees", description = "Returns a list of all registered employees")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<ApiResponseWrapper<List<EmployeeResponseDto>>> findAll() {
        List<EmployeeResponseDto> employees = service.findAll();
        return ResponseEntity.ok(ApiResponseWrapper.ok(EmployeeConstants.EMPLOYEES_RETRIEVED, employees));
    }

    @GetMapping(EmployeeConstants.EMPLOYEE_BY_ID_PATH)
    @Operation(summary = "Get employee by ID", description = "Returns a single employee by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
    })
    public ResponseEntity<ApiResponseWrapper<EmployeeResponseDto>> findById(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseWrapper.ok(EmployeeConstants.EMPLOYEE_RETRIEVED, service.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create employees", description = "Creates one or multiple employees in a single request")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee(s) created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<ApiResponseWrapper<List<EmployeeResponseDto>>> create(
            @Valid @RequestBody List<@Valid EmployeeRequestDto> employees) {
        List<EmployeeResponseDto> created = service.create(employees);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.ok(EmployeeConstants.EMPLOYEE_CREATED, created));
    }

    @PutMapping(EmployeeConstants.EMPLOYEE_BY_ID_PATH)
    @Operation(summary = "Update employee", description = "Updates all or some fields of an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
    })
    public ResponseEntity<ApiResponseWrapper<EmployeeResponseDto>> update(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(ApiResponseWrapper.ok(EmployeeConstants.EMPLOYEE_UPDATED, service.update(id, dto)));
    }

    @DeleteMapping(EmployeeConstants.EMPLOYEE_BY_ID_PATH)
    @Operation(summary = "Delete employee", description = "Deletes an employee by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted"),
            @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
    })
    public ResponseEntity<ApiResponseWrapper<Void>> delete(
            @Parameter(description = "Employee ID", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseWrapper.ok(EmployeeConstants.EMPLOYEE_DELETED, null));
    }

    @GetMapping(EmployeeConstants.EMPLOYEES_SEARCH_PATH)
    @Operation(summary = "Search employees by name", description = "Partial case-insensitive search across all name fields")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned")
    })
    public ResponseEntity<ApiResponseWrapper<List<EmployeeResponseDto>>> searchByName(
            @Parameter(description = "Name to search for", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponseWrapper.ok(EmployeeConstants.EMPLOYEES_SEARCH, service.searchByName(name)));
    }
}