package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @ParameterizedTest
    @MethodSource("provideValidStates")
    void from_whenValidState_thenReturnOptional(String input, BookingState expected) {
        Optional<BookingState> result = BookingState.from(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void from_whenInvalidState_thenReturnEmpty() {
        Optional<BookingState> result = BookingState.from("INVALID_STATE");

        assertFalse(result.isPresent());
    }

    @Test
    void from_whenCaseInsensitive_thenReturnState() {
        Optional<BookingState> result = BookingState.from("waiting");

        assertTrue(result.isPresent());
        assertEquals(BookingState.WAITING, result.get());
    }

    private static Stream<Arguments> provideValidStates() {
        return Stream.of(
                Arguments.of("ALL", BookingState.ALL),
                Arguments.of("CURRENT", BookingState.CURRENT),
                Arguments.of("FUTURE", BookingState.FUTURE),
                Arguments.of("PAST", BookingState.PAST),
                Arguments.of("REJECTED", BookingState.REJECTED),
                Arguments.of("WAITING", BookingState.WAITING)
        );
    }
}