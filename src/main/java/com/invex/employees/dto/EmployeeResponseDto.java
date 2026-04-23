package com.invex.employees.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.constants.EmployeeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee data returned by the API")
public class EmployeeResponseDto {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "First name", example = "Juan")
    private String firstName;

    @Schema(description = "Second name", example = "Carlos")
    private String secondName;

    @Schema(description = "Paternal surname", example = "García")
    private String paternalSurname;

    @Schema(description = "Maternal surname", example = "López")
    private String maternalSurname;

    @Schema(description = "Age", example = "30")
    private Integer age;

    @Schema(description = "Gender", example = "Male")
    private String gender;

    @JsonFormat(pattern = EmployeeConstants.DATE_FORMAT)
    @Schema(description = "Birth date", example = "15-06-1993")
    private LocalDate birthDate;

    @Schema(description = "Job position", example = "Senior Software Engineer")
    private String position;

    @Schema(description = "Timestamp when the record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Active status", example = "true")
    private Boolean active;
}
