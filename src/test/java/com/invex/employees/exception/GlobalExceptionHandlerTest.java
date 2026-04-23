package com.invex.employees.exception;

import com.invex.employees.controller.EmployeeController;
import com.invex.employees.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del GlobalExceptionHandler verificando que cada tipo de excepción
 * devuelve el HTTP status y body correcto.
 */
@WebMvcTest(controllers = EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GlobalExceptionHandler — Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService service;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 with error body for EmployeeNotFoundException")
    void shouldReturn404ForEmployeeNotFound() throws Exception {
        when(service.findById(anyLong()))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: 1"));

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found with id: 1"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 with fieldErrors for @Valid failure")
    void shouldReturn400WithFieldErrorsForValidationFailure() throws Exception {
        // Body con firstName vacío — disparará MethodArgumentNotValidException
        String invalidJson = """
                [{
                  "firstName": "",
                  "paternalSurname": "García",
                  "age": 30,
                  "gender": "MALE",
                  "birthDate": "15-06-1993",
                  "position": "Developer"
                }]
                """;

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.fieldErrors[0].message").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 for type mismatch in path variable")
    void shouldReturn400ForTypeMismatch() throws Exception {
        // "abc" no es un Long válido para {id}
        mockMvc.perform(get("/api/v1/employees/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("id")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgument() throws Exception {
        when(service.searchByName(""))
                .thenThrow(new IllegalArgumentException("Search name must not be blank"));

        mockMvc.perform(get("/api/v1/employees/search")
                        .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 500 for unexpected exceptions")
    void shouldReturn500ForUnexpectedException() throws Exception {
        when(service.findById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected DB connection error"));

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(
                        "An unexpected error occurred. Please contact support."));
    }
}
