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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GlobalExceptionHandler - Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService service;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 for EmployeeNotFoundException")
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
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgument() throws Exception {
        when(service.searchByName(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid search parameter"));

        mockMvc.perform(get("/api/v1/employees/search")
                        .param("name", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid search parameter"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 500 for unexpected exception")
    void shouldReturn500ForUnexpectedException() throws Exception {
        when(service.findById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

   /* @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 for blank firstName validation")
    void shouldReturn400ForBlankFirstName() throws Exception {
        String invalidJson = """
                [{
                  "firstName": "",
                  "paternalSurname": "Garcia",
                  "age": 30,
                  "gender": "Male",
                  "birthDate": "15-06-1993",
                  "position": "Developer"
                }]
                """;

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }*/

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 path with error response")
    void shouldIncludePathInErrorResponse() throws Exception {
        when(service.findById(anyLong()))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: 5"));

        mockMvc.perform(get("/api/v1/employees/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/v1/employees/5"));
    }
}
