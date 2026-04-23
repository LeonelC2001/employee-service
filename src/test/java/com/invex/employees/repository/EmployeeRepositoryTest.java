package com.invex.employees.repository;

import com.invex.employees.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("EmployeeRepository - JPA Tests")
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository repository;

    private Employee activeEmployee;

    @BeforeEach
    void setUp() {
        activeEmployee = entityManager.persistAndFlush(Employee.builder()
                .firstName("Juan")
                .secondName("Carlos")
                .paternalSurname("Garcia")
                .maternalSurname("Lopez")
                .age(30)
                .gender("Male")
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .active(true)
                .build());

        entityManager.persistAndFlush(Employee.builder()
                .firstName("Pedro")
                .paternalSurname("Martinez")
                .age(45)
                .gender("Male")
                .birthDate(LocalDate.of(1979, 1, 20))
                .position("Manager")
                .active(false)
                .build());

        entityManager.persistAndFlush(Employee.builder()
                .firstName("Ana")
                .secondName("Maria")
                .paternalSurname("Hernandez")
                .age(28)
                .gender("Female")
                .birthDate(LocalDate.of(1996, 3, 8))
                .position("Junior Developer")
                .active(true)
                .build());
    }

    @Nested
    @DisplayName("findByNameContainingIgnoreCase()")
    class FindByName {

        @Test
        @DisplayName("Should find by firstName case-insensitive")
        void shouldFindByFirstName() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("juan");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Should find by paternalSurname case-insensitive")
        void shouldFindByPaternalSurname() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("GARCIA");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPaternalSurname()).isEqualTo("Garcia");
        }

        @Test
        @DisplayName("Should find by partial match")
        void shouldFindByPartialMatch() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("ern");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should find by secondName")
        void shouldFindBySecondName() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("Maria");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSecondName()).isEqualTo("Maria");
        }

        @Test
        @DisplayName("Should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("xyz999");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByActiveTrue()")
    class FindAllActive {

        @Test
        @DisplayName("Should return only active employees")
        void shouldReturnOnlyActive() {
            List<Employee> result = repository.findAllByActiveTrue();
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(Employee::getActive);
        }

        @Test
        @DisplayName("Should not return inactive employees")
        void shouldNotReturnInactive() {
            List<Employee> result = repository.findAllByActiveTrue();
            assertThat(result).noneMatch(e -> e.getFirstName().equals("Pedro"));
        }
    }

    @Nested
    @DisplayName("JpaRepository methods")
    class JpaRepositoryMethods {

        @Test
        @DisplayName("findById should return employee when exists")
        void findByIdShouldReturn() {
            assertThat(repository.findById(activeEmployee.getId())).isPresent();
        }

        @Test
        @DisplayName("findById should return empty when not exists")
        void findByIdShouldReturnEmpty() {
            assertThat(repository.findById(999L)).isEmpty();
        }

        @Test
        @DisplayName("existsById should return true for existing")
        void existsByIdTrue() {
            assertThat(repository.existsById(activeEmployee.getId())).isTrue();
        }

        @Test
        @DisplayName("existsById should return false for nonexistent")
        void existsByIdFalse() {
            assertThat(repository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("save should persist and return with id")
        void saveShouldPersist() {
            Employee newEmployee = Employee.builder()
                    .firstName("Luis")
                    .paternalSurname("Ramirez")
                    .age(35)
                    .gender("Male")
                    .birthDate(LocalDate.of(1989, 7, 20))
                    .position("Architect")
                    .active(true)
                    .build();

            Employee saved = repository.save(newEmployee);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFirstName()).isEqualTo("Luis");
        }

        @Test
        @DisplayName("deleteById should remove the employee")
        void deleteByIdShouldRemove() {
            Long id = activeEmployee.getId();
            repository.deleteById(id);
            entityManager.flush();
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("count should return total number")
        void countShouldReturnTotal() {
            assertThat(repository.count()).isEqualTo(3);
        }
    }
}
