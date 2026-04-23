package com.invex.employees.service;

import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.entity.Employee;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.mapper.EmployeeMapper;
import com.invex.employees.repository.EmployeeRepository;
import com.invex.employees.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl - Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeMapper mapper;

    @InjectMocks
    private EmployeeServiceImpl service;

    private Employee employeeEntity;
    private EmployeeResponseDto employeeResponseDto;
    private EmployeeRequestDto employeeRequestDto;

    @BeforeEach
    void setUp() {
        employeeEntity = Employee.builder()
                .id(1L)
                .firstName("Juan")
                .paternalSurname("Garcia")
                .age(30)
                .gender("Male")
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        employeeResponseDto = EmployeeResponseDto.builder()
                .id(1L)
                .firstName("Juan")
                .paternalSurname("Garcia")
                .age(30)
                .gender("Male")
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .active(true)
                .build();

        employeeRequestDto = EmployeeRequestDto.builder()
                .firstName("Juan")
                .paternalSurname("Garcia")
                .age(30)
                .gender("Male")
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .build();
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Should return list of all employees")
        void shouldReturnAllEmployees() {
            when(repository.findAll()).thenReturn(List.of(employeeEntity));
            when(mapper.toResponseDto(employeeEntity)).thenReturn(employeeResponseDto);

            List<EmployeeResponseDto> result = service.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Juan");
            verify(repository).findAll();
            verify(mapper).toResponseDto(employeeEntity);
        }

        @Test
        @DisplayName("Should return empty list when no employees exist")
        void shouldReturnEmptyList() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            List<EmployeeResponseDto> result = service.findAll();

            assertThat(result).isEmpty();
            verify(repository).findAll();
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Should return employee when found")
        void shouldReturnEmployeeWhenFound() {
            when(repository.findById(1L)).thenReturn(Optional.of(employeeEntity));
            when(mapper.toResponseDto(employeeEntity)).thenReturn(employeeResponseDto);

            EmployeeResponseDto result = service.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining("99");

            verify(mapper, never()).toResponseDto(any());
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Should create single employee successfully")
        void shouldCreateSingleEmployee() {
            when(mapper.toEntity(employeeRequestDto)).thenReturn(employeeEntity);
            when(repository.saveAll(anyList())).thenReturn(List.of(employeeEntity));
            when(mapper.toResponseDto(employeeEntity)).thenReturn(employeeResponseDto);

            List<EmployeeResponseDto> result = service.create(List.of(employeeRequestDto));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Juan");
            verify(repository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should create multiple employees in batch")
        void shouldCreateMultipleEmployees() {
            EmployeeRequestDto dto2 = EmployeeRequestDto.builder()
                    .firstName("Maria").paternalSurname("Lopez")
                    .age(25).gender("Female")
                    .birthDate(LocalDate.of(1998, 3, 10))
                    .position("Junior Developer").build();

            Employee entity2 = Employee.builder().id(2L).firstName("Maria").build();
            EmployeeResponseDto response2 = EmployeeResponseDto.builder()
                    .id(2L).firstName("Maria").build();

            when(mapper.toEntity(employeeRequestDto)).thenReturn(employeeEntity);
            when(mapper.toEntity(dto2)).thenReturn(entity2);
            when(repository.saveAll(anyList())).thenReturn(List.of(employeeEntity, entity2));
            when(mapper.toResponseDto(employeeEntity)).thenReturn(employeeResponseDto);
            when(mapper.toResponseDto(entity2)).thenReturn(response2);

            List<EmployeeResponseDto> result = service.create(List.of(employeeRequestDto, dto2));

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Should update employee successfully")
        void shouldUpdateEmployee() {
            when(repository.findById(1L)).thenReturn(Optional.of(employeeEntity));
            when(repository.save(employeeEntity)).thenReturn(employeeEntity);
            when(mapper.toResponseDto(employeeEntity)).thenReturn(employeeResponseDto);

            EmployeeResponseDto result = service.update(1L, employeeRequestDto);

            assertThat(result).isNotNull();
            verify(mapper).updateEntityFromDto(employeeRequestDto, employeeEntity);
            verify(repository).save(employeeEntity);
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when updating nonexistent employee")
        void shouldThrowWhenEmployeeNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(99L, employeeRequestDto))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete employee when exists")
        void shouldDeleteEmployee() {
            when(repository.existsById(1L)).thenReturn(true);

            service.delete(1L);

            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when deleting nonexistent employee")
        void shouldThrowWhenNotFound() {
            when(repository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(99L))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("searchByName()")
    class SearchByName {

        @Test
        @DisplayName("Should return matching employees")
        void shouldReturnMatchingEmployees() {
            when(repository.findByNameContainingIgnoreCase("juan"))
                    .thenReturn(List.of(employeeEntity));
            when(mapper.toResponseDto(employeeEntity)).thenReturn(employeeResponseDto);

            List<EmployeeResponseDto> result = service.searchByName("juan");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyWhenNoMatches() {
            when(repository.findByNameContainingIgnoreCase("xyz"))
                    .thenReturn(Collections.emptyList());

            List<EmployeeResponseDto> result = service.searchByName("xyz");

            assertThat(result).isEmpty();
        }
    }
}
