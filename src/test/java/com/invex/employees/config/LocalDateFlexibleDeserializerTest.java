package com.invex.employees.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.invex.employees.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests para el deserializador de fechas y el enum Gender.
 * Usa ObjectMapper directamente sin Spring.
 */
@DisplayName("LocalDateFlexibleDeserializer — Unit Tests")
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
            "\"15-06-1993\"",   // dd-MM-yyyy (formato primario)
            "\"1993-06-15\"",   // yyyy-MM-dd (ISO)
            "\"15/06/1993\"",   // dd/MM/yyyy
            "\"1993/06/15\""    // yyyy/MM/dd
    })
    @DisplayName("Should parse multiple date formats")
    void shouldParseMultipleDateFormats(String dateJson) throws Exception {
        LocalDate result = objectMapper.readValue(dateJson, LocalDate.class);
        assertThat(result).isEqualTo(LocalDate.of(1993, 6, 15));
    }

    @Test
    @DisplayName("Should throw IOException for invalid date format")
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

    // ─── Gender Enum Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Gender: should deserialize MALE case-insensitive")
    void shouldDeserializeMaleCaseInsensitive() throws Exception {
        assertThat(Gender.fromValue("male")).isEqualTo(Gender.MALE);
        assertThat(Gender.fromValue("MALE")).isEqualTo(Gender.MALE);
        assertThat(Gender.fromValue("Male")).isEqualTo(Gender.MALE);
    }

    @Test
    @DisplayName("Gender: should deserialize FEMALE case-insensitive")
    void shouldDeserializeFemale() {
        assertThat(Gender.fromValue("female")).isEqualTo(Gender.FEMALE);
        assertThat(Gender.fromValue("FEMALE")).isEqualTo(Gender.FEMALE);
    }

    @Test
    @DisplayName("Gender: should deserialize OTHER")
    void shouldDeserializeOther() {
        assertThat(Gender.fromValue("other")).isEqualTo(Gender.OTHER);
        assertThat(Gender.fromValue("Other")).isEqualTo(Gender.OTHER);
    }

    @Test
    @DisplayName("Gender: should throw for invalid value")
    void shouldThrowForInvalidGender() {
        assertThatThrownBy(() -> Gender.fromValue("hombre"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hombre");
    }

    @Test
    @DisplayName("Gender: should return null for null input")
    void shouldReturnNullForNull() {
        assertThat(Gender.fromValue(null)).isNull();
    }

    @Test
    @DisplayName("Gender: getDisplayName should return proper display string")
    void shouldReturnDisplayName() {
        assertThat(Gender.MALE.getDisplayName()).isEqualTo("Male");
        assertThat(Gender.FEMALE.getDisplayName()).isEqualTo("Female");
        assertThat(Gender.OTHER.getDisplayName()).isEqualTo("Other");
    }
}
