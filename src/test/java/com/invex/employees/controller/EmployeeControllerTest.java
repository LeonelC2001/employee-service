package com.invex.employees.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
import com.invex.employees.enums.Gender;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.exception.GlobalExceptionHandler;
import com.invex.employees.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de Controller con MockMvc.
 *
 * @WebMvcTest levanta solo la capa web (Controller + Filters + Security),
 * sin levantar toda la aplicación ni conectar a BD.
 * El Service se mockea con @MockBean.
 *
 * @WithMockUser simula un usuario autenticado para los tests
 * sin tener que enviar credenciales reales.
 */
@WebMvcTest(controllers = EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("EmployeeController — Web Layer Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService service;

    private ObjectMapper objectMapper;
    private EmployeeResponseDto sampleResponse;
    private EmployeeRequestDto sampleRequest;

    private static final String BASE_URL = "/api/v1/employees";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        sampleResponse = EmployeeResponseDto.builder()
                .id(1L)
                .firstName("Juan")
                .paternalSurname("García")
                .age(30)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .active(true)
                .build();

        sampleRequest = EmployeeRequestDto.builder()
                .firstName("Juan")
                .paternalSurname("García")
                .age(30)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .build();
    }

    // ─── GET /employees ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /employees")
    class GetAll {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with list of employees")
        void shouldReturn200WithEmployees() throws Exception {
            when(service.findAll()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].firstName").value("Juan"))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with empty list when no employees")
        void shouldReturn200WithEmptyList() throws Exception {
            when(service.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /employees/{id} ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /employees/{id}")
    class GetById {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 when employee exists")
        void shouldReturn200WhenFound() throws Exception {
            when(service.findById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.firstName").value("Juan"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when employee not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(service.findById(99L))
                    .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
        }
    }

    // ─── POST /employees ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /employees")
    class Create {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 201 when creating valid employee")
        void shouldReturn201WhenCreated() throws Exception {
            when(service.create(anyList())).thenReturn(List.of(sampleResponse));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sampleRequest))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].firstName").value("Juan"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 when firstName is blank")
        void shouldReturn400WhenFirstNameBlank() throws Exception {
            EmployeeRequestDto invalidDto = EmployeeRequestDto.builder()
                    .firstName("")  // inválido
                    .paternalSurname("García")
                    .age(30)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(invalidDto))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 when age is negative")
        void shouldReturn400WhenAgeNegative() throws Exception {
            EmployeeRequestDto invalidDto = EmployeeRequestDto.builder()
                    .firstName("Juan")
                    .paternalSurname("García")
                    .age(-5)  // inválido
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1993, 6, 15))
                    .position("Developer")
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(invalidDto))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER tries to create employee")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sampleRequest))))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── PUT /employees/{id} ──────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /employees/{id}")
    class Update {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 when update is successful")
        void shouldReturn200WhenUpdated() throws Exception {
            when(service.update(eq(1L), any(EmployeeRequestDto.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when updating nonexistent employee")
        void shouldReturn404WhenNotFound() throws Exception {
            when(service.update(eq(99L), any()))
                    .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

            mockMvc.perform(put(BASE_URL + "/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── DELETE /employees/{id} ───────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /employees/{id}")
    class Delete {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 when delete is successful")
        void shouldReturn200WhenDeleted() throws Exception {
            doNothing().when(service).delete(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when deleting nonexistent employee")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new EmployeeNotFoundException("Employee not found with id: 99"))
                    .when(service).delete(99L);

            mockMvc.perform(delete(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER tries to delete")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── GET /employees/search ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /employees/search")
    class Search {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with matching employees")
        void shouldReturn200WithResults() throws Exception {
            when(service.searchByName("juan")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/search")
                            .param("name", "juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with empty list when no match")
        void shouldReturn200WithEmptyResults() throws Exception {
            when(service.searchByName("xyz")).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/search")
                            .param("name", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
