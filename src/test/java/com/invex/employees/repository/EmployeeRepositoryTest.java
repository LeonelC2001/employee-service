package com.invex.employees.repository;

import com.invex.employees.entity.Employee;
import com.invex.employees.enums.Gender;
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

/**
 * Tests de repositorio con @DataJpaTest.
 *
 * @DataJpaTest levanta solo la capa JPA con una BD H2 en memoria.
 * No carga Controllers, Services ni Security — es muy rápido.
 * TestEntityManager se usa para insertar datos de prueba directamente
 * sin pasar por el Service.
 *
 * Estos tests verifican que las queries JPQL funcionen correctamente.
 */
@DataJpaTest
@DisplayName("EmployeeRepository — JPA Tests")
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository repository;

    private Employee activeEmployee;
    private Employee inactiveEmployee;

    @BeforeEach
    void setUp() {
        activeEmployee = entityManager.persistAndFlush(Employee.builder()
                .firstName("Juan")
                .secondName("Carlos")
                .paternalSurname("García")
                .maternalSurname("López")
                .age(30)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .active(true)
                .build());

        inactiveEmployee = entityManager.persistAndFlush(Employee.builder()
                .firstName("Pedro")
                .paternalSurname("Martínez")
                .age(45)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1979, 1, 20))
                .position("Manager")
                .active(false)
                .build());

        entityManager.persistAndFlush(Employee.builder()
                .firstName("Ana")
                .secondName("María")
                .paternalSurname("Hernández")
                .age(28)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1996, 3, 8))
                .position("Junior Developer")
                .active(true)
                .build());
    }

    // ─── findByNameContainingIgnoreCase ───────────────────────────────────────

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
            List<Employee> result = repository.findByNameContainingIgnoreCase("GARCÍA");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPaternalSurname()).isEqualTo("García");
        }

        @Test
        @DisplayName("Should find by partial match")
        void shouldFindByPartialMatch() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("ern");
            // Encuentra "Hernández"
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getPaternalSurname()).contains("Hern");
        }

        @Test
        @DisplayName("Should find by secondName")
        void shouldFindBySecondName() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("María");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSecondName()).isEqualTo("María");
        }

        @Test
        @DisplayName("Should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            List<Employee> result = repository.findByNameContainingIgnoreCase("xyz999");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return multiple results when name matches several employees")
        void shouldReturnMultipleMatches() {
            // "a" aparece en Juan (paternalSurname García), Ana, Hernández
            List<Employee> result = repository.findByNameContainingIgnoreCase("ana");
            assertThat(result).isNotEmpty();
        }
    }

    // ─── findAllByActiveTrue ──────────────────────────────────────────────────

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

            assertThat(result)
                    .noneMatch(e -> e.getFirstName().equals("Pedro"));
        }
    }

    // ─── Herencia de JpaRepository ────────────────────────────────────────────

    @Nested
    @DisplayName("JpaRepository inherited methods")
    class JpaRepositoryMethods {

        @Test
        @DisplayName("findById should return employee when exists")
        void findByIdShouldReturnEmployee() {
            assertThat(repository.findById(activeEmployee.getId())).isPresent();
        }

        @Test
        @DisplayName("findById should return empty when not exists")
        void findByIdShouldReturnEmpty() {
            assertThat(repository.findById(999L)).isEmpty();
        }

        @Test
        @DisplayName("existsById should return true for existing employee")
        void existsByIdShouldReturnTrue() {
            assertThat(repository.existsById(activeEmployee.getId())).isTrue();
        }

        @Test
        @DisplayName("existsById should return false for nonexistent employee")
        void existsByIdShouldReturnFalse() {
            assertThat(repository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("save should persist employee and return with generated id")
        void saveShouldPersistEmployee() {
            Employee newEmployee = Employee.builder()
                    .firstName("Luis")
                    .paternalSurname("Ramírez")
                    .age(35)
                    .gender(Gender.MALE)
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
        @DisplayName("count should return total number of employees")
        void countShouldReturnTotal() {
            assertThat(repository.count()).isEqualTo(3);
        }
    }
}
