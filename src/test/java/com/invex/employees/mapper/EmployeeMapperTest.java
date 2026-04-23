package com.invex.employees.mapper;

import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmployeeMapper - Unit Tests")
class EmployeeMapperTest {

    private EmployeeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EmployeeMapper();
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntity {

        @Test
        @DisplayName("Should map all fields from DTO to Entity")
        void shouldMapAllFields() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .secondName("Carlos")
                    .paternalSurname("Garcia")
                    .maternalSurname("Lopez")
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Senior Developer")
                    .active(true)
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getFirstName()).isEqualTo("Juan");
            assertThat(entity.getSecondName()).isEqualTo("Carlos");
            assertThat(entity.getPaternalSurname()).isEqualTo("Garcia");
            assertThat(entity.getMaternalSurname()).isEqualTo("Lopez");
            assertThat(entity.getAge()).isEqualTo(30);
            assertThat(entity.getGender()).isEqualTo("Male");
            assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1993, 6, 15));
            assertThat(entity.getPosition()).isEqualTo("Senior Developer");
            assertThat(entity.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should default active to true when null")
        void shouldDefaultActiveToTrue() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .paternalSurname("Garcia")
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(null)
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should use false when active is explicitly false")
        void shouldRespectExplicitFalse() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .paternalSurname("Garcia")
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(false)
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .secondName(null)
                    .paternalSurname("Garcia")
                    .maternalSurname(null)
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .build();

            Employee entity = mapper.toEntity(dto);

            assertThat(entity.getSecondName()).isNull();
            assertThat(entity.getMaternalSurname()).isNull();
        }
    }

    @Nested
    @DisplayName("toResponseDto()")
    class ToResponseDto {

        @Test
        @DisplayName("Should map all fields from Entity to DTO")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();
            Employee entity = Employee.builder()
                    .id(1L)
                    .firstName("Juan")
                    .secondName("Carlos")
                    .paternalSurname("Garcia")
                    .maternalSurname("Lopez")
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Senior Developer")
                    .createdAt(now)
                    .active(true)
                    .build();

            EmployeeResponseDto dto = mapper.toResponseDto(entity);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getFirstName()).isEqualTo("Juan");
            assertThat(dto.getSecondName()).isEqualTo("Carlos");
            assertThat(dto.getPaternalSurname()).isEqualTo("Garcia");
            assertThat(dto.getAge()).isEqualTo(30);
            assertThat(dto.getGender()).isEqualTo("Male");
            assertThat(dto.getBirthDate()).isEqualTo(LocalDate.of(1993, 6, 15));
            assertThat(dto.getPosition()).isEqualTo("Senior Developer");
            assertThat(dto.getCreatedAt()).isEqualTo(now);
            assertThat(dto.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateEntityFromDto()")
    class UpdateEntityFromDto {

        @Test
        @DisplayName("Should update only non-null fields")
        void shouldUpdateOnlyNonNullFields() {
            Employee entity = Employee.builder()
                    .firstName("Juan")
                    .paternalSurname("Garcia")
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(true)
                    .build();

            EmployeeRequestDto dto = EmployeeRequestDto.builder()
                    .firstName("Carlos")
                    .position("Team Lead")
                    .build();

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getFirstName()).isEqualTo("Carlos");
            assertThat(entity.getPosition()).isEqualTo("Team Lead");
            assertThat(entity.getPaternalSurname()).isEqualTo("Garcia");
            assertThat(entity.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should not update any field when all DTO fields are null")
        void shouldNotUpdateWhenAllNull() {
            Employee entity = Employee.builder()
                    .firstName("Juan")
                    .paternalSurname("Garcia")
                    .age(30)
                    .gender("Male")
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .active(true)
                    .build();

            EmployeeRequestDto dto = new EmployeeRequestDto();

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getFirstName()).isEqualTo("Juan");
            assertThat(entity.getAge()).isEqualTo(30);
        }
    }
}
