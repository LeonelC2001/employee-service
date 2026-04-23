package com.invex.employees.constants;

public final class EmployeeConstants {

    private EmployeeConstants() {}

    // Date
    public static final String DATE_FORMAT = "dd-MM-yyyy";

    // Status
    public static final Boolean DEFAULT_ACTIVE_STATUS = true;

    // Messages
    public static final String EMPLOYEE_NOT_FOUND = "Employee not found with id: ";
    public static final String EMPLOYEES_RETRIEVED = "Employees retrieved successfully";
    public static final String EMPLOYEE_RETRIEVED = "Employee retrieved successfully";
    public static final String EMPLOYEE_CREATED = "Employee(s) created successfully";
    public static final String EMPLOYEE_UPDATED = "Employee updated successfully";
    public static final String EMPLOYEE_DELETED = "Employee deleted successfully";
    public static final String EMPLOYEES_SEARCH = "Search results retrieved successfully";

    // Validation
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String PATERNAL_SURNAME_REQUIRED = "Paternal surname is required";
    public static final String AGE_MUST_BE_POSITIVE = "Age must be a positive number";
    public static final String GENDER_REQUIRED = "Gender is required";
    public static final String BIRTH_DATE_REQUIRED = "Birth date is required";
    public static final String POSITION_REQUIRED = "Position is required";

    // API paths
    public static final String API_BASE_PATH = "/api/v1";
    public static final String EMPLOYEES_PATH = "/employees";
    public static final String EMPLOYEE_BY_ID_PATH = "/{id}";
    public static final String EMPLOYEES_SEARCH_PATH = "/search";

    // Swagger
    public static final String API_TITLE = "Employee Service API";
    public static final String API_DESCRIPTION = "RESTful API for employee management";
    public static final String API_VERSION = "1.0.0";
}
