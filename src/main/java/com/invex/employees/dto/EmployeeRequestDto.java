package com.invex.employees.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.constants.EmployeeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.invex.employees.config.LocalDateFlexibleDeserializer;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating an employee")
public class EmployeeRequestDto {

    @NotBlank(message = EmployeeConstants.FIRST_NAME_REQUIRED)
    @Schema(description = "Employee's first name", example = "Juan")
    private String firstName;

    @Schema(description = "Employee's second name", example = "Carlos")
    private String secondName;

    @NotBlank(message = EmployeeConstants.PATERNAL_SURNAME_REQUIRED)
    @Schema(description = "Employee's paternal surname", example = "García")
    private String paternalSurname;

    @Schema(description = "Employee's maternal surname", example = "López")
    private String maternalSurname;

    @NotNull(message = EmployeeConstants.AGE_MUST_BE_POSITIVE)
    @Positive(message = EmployeeConstants.AGE_MUST_BE_POSITIVE)
    @Schema(description = "Employee's age", example = "30")
    private Integer age;

    @NotBlank(message = EmployeeConstants.GENDER_REQUIRED)
    @Schema(description = "Employee's gender", example = "Male", allowableValues = {"Male", "Female", "Other"})
    private String gender;

    @NotNull(message = EmployeeConstants.BIRTH_DATE_REQUIRED)
    @JsonDeserialize(using = LocalDateFlexibleDeserializer.class)
    @Schema(description = "Employee's birth date", example = "15-06-1993")
    private LocalDate birthDate;

    @NotBlank(message = EmployeeConstants.POSITION_REQUIRED)
    @Schema(description = "Employee's job position", example = "Senior Software Engineer")
    private String position;

    @Schema(description = "Whether the employee is active", example = "true")
    private Boolean active;
}