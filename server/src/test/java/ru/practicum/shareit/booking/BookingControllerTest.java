package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createBooking_ShouldReturnOk() throws Exception {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        responseDto.setItem(itemDto);

        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setName("Booker");
        responseDto.setBooker(userDto);

        responseDto.setStart(bookingDto.getStart());
        responseDto.setEnd(bookingDto.getEnd());
        responseDto.setStatus(BookingStatus.WAITING);

        when(bookingService.create(eq(2L), any(BookingDto.class))).thenReturn(responseDto);

        // Действие и проверка
        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.item.id").value(1))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_ShouldReturnApproved() throws Exception {
        // Подготовка
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.approve(eq(1L), eq(1L), eq(true))).thenReturn(responseDto);

        // Действие и проверка
        mockMvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_AsBooker_ShouldReturnBooking() throws Exception {
        // Подготовка
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        responseDto.setItem(itemDto);

        UserDto userDto = new UserDto();
        userDto.setId(2L);
        responseDto.setBooker(userDto);

        when(bookingService.getById(2L, 1L)).thenReturn(responseDto);

        // Действие и проверка
        mockMvc.perform(get("/bookings/1")
                        .header(userIdHeader, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.item.id").value(1))
                .andExpect(jsonPath("$.booker.id").value(2));
    }

    @Test
    void getUserBookings_WithDefaultState_ShouldReturnList() throws Exception {
        // Подготовка
        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);

        BookingResponseDto booking2 = new BookingResponseDto();
        booking2.setId(2L);

        List<BookingResponseDto> bookings = List.of(booking1, booking2);

        when(bookingService.getUserBookings(eq(1L), eq("ALL"))).thenReturn(bookings);

        // Действие и проверка
        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getUserBookings_WithStateWaiting_ShouldReturnWaiting() throws Exception {
        // Подготовка
        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.WAITING);

        List<BookingResponseDto> bookings = List.of(booking1);

        when(bookingService.getUserBookings(eq(1L), eq("WAITING"))).thenReturn(bookings);

        // Действие и проверка
        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getOwnerBookings_ShouldReturnList() throws Exception {
        // Подготовка
        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);

        BookingResponseDto booking2 = new BookingResponseDto();
        booking2.setId(2L);

        List<BookingResponseDto> bookings = List.of(booking1, booking2);

        when(bookingService.getOwnerBookings(eq(1L), eq("ALL"))).thenReturn(bookings);

        // Действие и проверка
        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getOwnerBookings_WithStateWaiting_ShouldReturnWaiting() throws Exception {
        // Подготовка
        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.WAITING);

        List<BookingResponseDto> bookings = List.of(booking1);

        when(bookingService.getOwnerBookings(eq(1L), eq("WAITING"))).thenReturn(bookings);

        // Действие и проверка
        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }
}