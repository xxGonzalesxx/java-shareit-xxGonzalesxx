package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

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
    private BookingClient bookingClient;

    private BookItemRequestDto validBookingDto;
    private final long userId = 1L;
    private final long bookingId = 1L;

    @BeforeEach
    void setUp() {
        validBookingDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );
    }

    @Test
    void getBookings_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookings_whenInvalidState_thenThrowException() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "INVALID_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    /*@Test
    void getBookings_whenInvalidFromParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }*/

    /*@Test
    void getBookings_whenInvalidSizeParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }*/

    @Test
    void bookItem_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void bookItem_whenStartInPast_thenReturnBadRequest() throws Exception {
        BookItemRequestDto invalidDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_whenEndInPast_thenReturnBadRequest() throws Exception {
        BookItemRequestDto invalidDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusHours(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_whenEndBeforeStart_thenReturnOk() throws Exception {
        BookItemRequestDto invalidDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1)
        );

        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isOk());
    }

    @Test
    void bookItem_whenItemIdIsZero_thenReturnOk() throws Exception {
        BookItemRequestDto invalidDto = new BookItemRequestDto(
                0L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getBooking_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void approveBooking_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_whenInvalidState_thenThrowException() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "INVALID_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}