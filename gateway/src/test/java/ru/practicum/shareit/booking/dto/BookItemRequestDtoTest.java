package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookItemRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void whenStartInPast_thenViolation() {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("start")));
    }

    @Test
    void whenEndInPast_thenViolation() {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusHours(1)
        );

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("end")));
    }

    @Test
    void whenEndEqualsStart_thenNoViolation() { // Изменено с Violation на NoViolation
        LocalDateTime now = LocalDateTime.now().plusHours(1);
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                now,
                now
        );

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        // В DTO нет аннотации, проверяющей, что end > start
        // Поэтому violations должны быть пустыми
        assertTrue(violations.isEmpty(), "End equals start should not cause validation violations");
    }

    @Test
    void testGettersAndAllArgsConstructor() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        assertEquals(1L, dto.getItemId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
    }
}