package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void toBookingResponseDto_ShouldMapAllFields() {
        // Подготовка
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        User booker = new User();
        booker.setId(2L);
        booker.setName("User");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2025, 1, 1, 10, 0));
        booking.setEnd(LocalDateTime.of(2025, 1, 2, 10, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        // Действие
        BookingResponseDto responseDto = BookingMapper.toBookingResponseDto(booking);

        // Проверка
        assertEquals(1L, responseDto.getId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), responseDto.getStart());
        assertEquals(LocalDateTime.of(2025, 1, 2, 10, 0), responseDto.getEnd());
        assertEquals(BookingStatus.APPROVED, responseDto.getStatus());
        assertEquals(1L, responseDto.getItem().getId());
        assertEquals("Дрель", responseDto.getItem().getName());
        assertEquals(2L, responseDto.getBooker().getId());
        assertEquals("User", responseDto.getBooker().getName());
    }

    @Test
    void toBookingResponseDto_WithNull_ShouldReturnNull() {
        assertNull(BookingMapper.toBookingResponseDto(null));
    }

    @Test
    void toBookingResponseDto_WithNullFields_ShouldHandleGracefully() {
        Booking booking = new Booking();
        booking.setId(1L);

        BookingResponseDto responseDto = BookingMapper.toBookingResponseDto(booking);

        assertEquals(1L, responseDto.getId());
        assertNull(responseDto.getBooker());
        assertNull(responseDto.getItem());
    }

    @Test
    void toBookingDto_ShouldMapAllFields() {
        // Подготовка
        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2025, 1, 1, 10, 0));
        booking.setEnd(LocalDateTime.of(2025, 1, 2, 10, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        // Действие
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        // Проверка
        assertEquals(1L, bookingDto.getId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), bookingDto.getStart());
        assertEquals(LocalDateTime.of(2025, 1, 2, 10, 0), bookingDto.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(2L, bookingDto.getBookerId());
        assertEquals(BookingStatus.WAITING, bookingDto.getStatus());
    }

    @Test
    void toBookingDto_WithNull_ShouldReturnNull() {
        assertNull(BookingMapper.toBookingDto(null));
    }

    @Test
    void toBooking_ShouldMapAllFields() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.of(2025, 1, 1, 10, 0));
        bookingDto.setEnd(LocalDateTime.of(2025, 1, 2, 10, 0));

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(2L);

        // Действие
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        // Проверка
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), booking.getStart());
        assertEquals(LocalDateTime.of(2025, 1, 2, 10, 0), booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void toBooking_WithNullDto_ShouldReturnNull() {
        Item item = new Item();
        User booker = new User();

        assertNull(BookingMapper.toBooking(null, item, booker));
    }

    @Test
    void toBooking_WithNullStatus_ShouldSetWaiting() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.of(2025, 1, 1, 10, 0));
        bookingDto.setEnd(LocalDateTime.of(2025, 1, 2, 10, 0));
        // status не установлен

        Item item = new Item();
        User booker = new User();

        // Действие
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        // Проверка
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }
}