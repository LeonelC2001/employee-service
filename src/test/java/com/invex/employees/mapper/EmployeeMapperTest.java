package com.invex.employees.mapper;

import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.entity.Employee;
import com.invex.employees.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para EmployeeMapper.
 * No necesita Spring — es una clase POJO pura, se instancia directamente.
 */
@DisplayName("EmployeeMapper — Unit Tests")
class EmployeeMapperTest {

    private EmployeeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EmployeeMapper();
    }

    // ─── toEntity ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toEntity(EmployeeRequestDto)")
    class ToEntity {

        @Test
        @DisplayName("Should map all fields from DTO to Entity")
        void shouldMapAllFields() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .secondName("Carlos")
                    .paternalSurname("García")
                    .maternalSurname("López")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Senior Developer")
                    .active(true)
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getFirstName()).isEqualTo("Juan");
            assertThat(entity.getSecondName()).isEqualTo("Carlos");
            assertThat(entity.getPaternalSurname()).isEqualTo("García");
            assertThat(entity.getMaternalSurname()).isEqualTo("López");
            assertThat(entity.getAge()).isEqualTo(30);
            assertThat(entity.getGender()).isEqualTo(Gender.MALE);
            assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1993, 6, 15));
            assertThat(entity.getPosition()).isEqualTo("Senior Developer");
            assertThat(entity.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should default active to true when null in DTO")
        void shouldDefaultActiveToTrue() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .paternalSurname("García")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(null)  // null → debe quedar true por defecto
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should use false when active is explicitly false")
        void shouldRespectExplicitFalse() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .paternalSurname("García")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(false)
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getActive()).isFalse();
        }

        @Test
        @DisplayName("Should trim whitespace in string fields")
        void shouldTrimStringFields() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("  Juan  ")
                    .paternalSurname("  García  ")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("  Senior Developer  ")
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getFirstName()).isEqualTo("Juan");
            assertThat(entity.getPaternalSurname()).isEqualTo("García");
            assertThat(entity.getPosition()).isEqualTo("Senior Developer");
        }

        @Test
        @DisplayName("Should handle null optional fields gracefully")
        void shouldHandleNullOptionalFields() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .secondName(null)       // opcional
                    .paternalSurname("García")
                    .maternalSurname(null)  // opcional
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getSecondName()).isNull();
            assertThat(entity.getMaternalSurname()).isNull();
        }
    }

    // ─── toResponseDto ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toResponseDto(Employee)")
    class ToResponseDto {

        @Test
        @DisplayName("Should map all fields from Entity to DTO")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();
            Employee entity = Employee.builder()
                    .id(1L)
                    .firstName("Juan")
                    .secondName("Carlos")
                    .paternalSurname("García")
                    .maternalSurname("López")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Senior Developer")
                    .createdAt(now)
                    .updatedAt(now)
                    .active(true)
                    .build();

            EmployeeResponseDto dto = mapper.toResponseDto(entity);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getFirstName()).isEqualTo("Juan");
            assertThat(dto.getSecondName()).isEqualTo("Carlos");
            assertThat(dto.getPaternalSurname()).isEqualTo("García");
            assertThat(dto.getAge()).isEqualTo(30);
            assertThat(dto.getGender()).isEqualTo(Gender.MALE);
            assertThat(dto.getBirthDate()).isEqualTo(LocalDate.of(1993, 6, 15));
            assertThat(dto.getPosition()).isEqualTo("Senior Developer");
            assertThat(dto.getCreatedAt()).isEqualTo(now);
            assertThat(dto.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should include calculatedAge from entity")
        void shouldIncludeCalculatedAge() {
            // Empleado nacido hace 30 años (aproximado)
            LocalDate birthDate = LocalDate.now().minusYears(30);
            Employee entity = Employee.builder()
                    .id(1L)
                    .firstName("Juan")
                    .paternalSurname("García")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(birthDate)
                    .position("Developer")
                    .active(true)
                    .build();

            EmployeeResponseDto dto = mapper.toResponseDto(entity);

            assertThat(dto.getCalculatedAge()).isEqualTo(30);
        }
    }

    // ─── updateEntityFromDto ──────────────────────────────────────────────────

    @Nested
    @DisplayName("updateEntityFromDto()")
    class UpdateEntityFromDto {

        @Test
        @DisplayName("Should update only non-null fields")
        void shouldUpdateOnlyNonNullFields() {
            Employee entity = Employee.builder()
                    .firstName("Juan")
                    .paternalSurname("García")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(true)
                    .build();

            // Solo actualiza firstName y position
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Carlos")
                    .position("Team Lead")
                    .build();

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getFirstName()).isEqualTo("Carlos");
            assertThat(entity.getPosition()).isEqualTo("Team Lead");
            // Los demás no cambian
            assertThat(entity.getPaternalSurname()).isEqualTo("García");
            assertThat(entity.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should trim strings in updates")
        void shouldTrimStringsOnUpdate() {
            Employee entity = Employee.builder()
                    .firstName("Juan").paternalSurname("García")
                    .age(30).gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer").active(true).build();

            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("  Carlos  ")
                    .build();

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getFirstName()).isEqualTo("Carlos");
        }

        @Test
        @DisplayName("Should not update any field when all DTO fields are null")
        void shouldNotUpdateWhenAllNull() {
            Employee entity = Employee.builder()
                    .firstName("Juan").paternalSurname("García")
                    .age(30).gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer").active(true).build();

            EmployeeRequestDto dto = new EmployeeRequestDto(); // todos null

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getFirstName()).isEqualTo("Juan");
            assertThat(entity.getAge()).isEqualTo(30);
        }
    }
}
