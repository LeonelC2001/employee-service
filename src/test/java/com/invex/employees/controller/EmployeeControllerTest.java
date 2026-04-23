package com.invex.employees.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.invex.employees.dto.EmployeeRequestDto;
import com.invex.employees.dto.EmployeeResponseDto;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("EmployeeController - Web Layer Tests")
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
                .paternalSurname("Garcia")
                .age(30)
                .gender("Male")
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .active(true)
                .build();

        sampleRequest = EmployeeRequestDto.builder()
                .firstName("Juan")
                .paternalSurname("Garcia")
                .age(30)
                .gender("Male")
                .birthDate(LocalDate.of(1993, 6, 15))
                .position("Senior Developer")
                .build();
    }

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
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with empty list")
        void shouldReturn200WithEmptyList() throws Exception {
            when(service.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
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
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("POST /employees")
    class Create {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 201 when creating valid employee")
        void shouldReturn201WhenCreated() throws Exception {
            when(service.create(anyList())).thenReturn(List.of(sampleResponse));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sampleRequest))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].firstName").value("Juan"));
        }

        /*@Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER tries to delete")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1")
                            .with(csrf()))
                    .andExpect(status().is4xxClientError());

            verify(service, never()).delete(anyLong());
        }*/
    }

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
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when updating nonexistent employee")
        void shouldReturn404WhenNotFound() throws Exception {
            when(service.update(eq(99L), any()))
                    .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

            mockMvc.perform(put(BASE_URL + "/99")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /employees/{id}")
    class Delete {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 when delete is successful")
        void shouldReturn200WhenDeleted() throws Exception {
            doNothing().when(service).delete(1L);

            mockMvc.perform(delete(BASE_URL + "/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when employee not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new EmployeeNotFoundException("Employee not found with id: 99"))
                    .when(service).delete(99L);

            mockMvc.perform(delete(BASE_URL + "/99")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        /*@Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER tries to delete")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }*/
    }

    @Nested
    @DisplayName("GET /employees/search")
    class Search {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with results")
        void shouldReturn200WithResults() throws Exception {
            when(service.searchByName("juan")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/search").param("name", "juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with empty list")
        void shouldReturn200WithEmptyResults() throws Exception {
            when(service.searchByName("xyz")).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/search").param("name", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}