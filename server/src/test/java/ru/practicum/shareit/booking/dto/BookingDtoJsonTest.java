package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldConvertToJson() throws Exception {
        // Подготовка
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 10, 0);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(5L);
        bookingDto.setBookerId(10L);
        bookingDto.setStatus(BookingStatus.WAITING);

        // Действие
        String jsonString = json.write(bookingDto).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"start\":\"2025-01-01T10:00:00\"");
        assertThat(jsonString).contains("\"end\":\"2025-01-02T10:00:00\"");
        assertThat(jsonString).contains("\"itemId\":5");
        assertThat(jsonString).contains("\"bookerId\":10");
        assertThat(jsonString).contains("\"status\":\"WAITING\"");
    }

    @Test
    void shouldConvertFromJson() throws Exception {
        // Подготовка
        String jsonString = "{\"id\":1,\"start\":\"2025-01-01T10:00:00\",\"end\":\"2025-01-02T10:00:00\",\"itemId\":5,\"bookerId\":10,\"status\":\"WAITING\"}";

        // Действие
        BookingDto bookingDto = json.parse(jsonString).getObject();

        // Проверка
        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 0));
        assertThat(bookingDto.getItemId()).isEqualTo(5L);
        assertThat(bookingDto.getBookerId()).isEqualTo(10L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void shouldWorkWithNullFields() throws Exception {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);

        // Действие
        String jsonString = json.write(bookingDto).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"start\":null");
        assertThat(jsonString).contains("\"end\":null");
        assertThat(jsonString).contains("\"itemId\":null");
        assertThat(jsonString).contains("\"bookerId\":null");
        assertThat(jsonString).contains("\"status\":null");
    }
}