package com.invex.employees.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LocalDateFlexibleDeserializer - Unit Tests")
class LocalDateFlexibleDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateFlexibleDeserializer());
        objectMapper.registerModule(module);
    }

    @ParameterizedTest(name = "Should parse date format: {0}")
    @ValueSource(strings = {
            "\"15-06-1993\"",
            "\"1993-06-15\"",
            "\"15/06/1993\"",
            "\"1993/06/15\""
    })
    @DisplayName("Should parse multiple date formats")
    void shouldParseMultipleDateFormats(String dateJson) throws Exception {
        LocalDate result = objectMapper.readValue(dateJson, LocalDate.class);
        assertThat(result).isEqualTo(LocalDate.of(1993, 6, 15));
    }

    @Test
    @DisplayName("Should throw for invalid date format")
    void shouldThrowForInvalidFormat() {
        assertThatThrownBy(() ->
                objectMapper.readValue("\"not-a-date\"", LocalDate.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should trim whitespace from date string")
    void shouldTrimWhitespace() throws Exception {
        LocalDate result = objectMapper.readValue("\"  15-06-1993  \"", LocalDate.class);
        assertThat(result).isEqualTo(LocalDate.of(1993, 6, 15));
    }
}
