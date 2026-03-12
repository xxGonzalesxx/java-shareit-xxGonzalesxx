package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_ShouldReturnOk() throws Exception {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен мощный перфоратор");

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужен мощный перфоратор");
        responseDto.setCreated(LocalDateTime.now());

        when(requestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(responseDto);

        // Действие и проверка
        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужен мощный перфоратор"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void getUserRequests_ShouldReturnList() throws Exception {
        // Подготовка
        ItemRequestResponseDto request1 = new ItemRequestResponseDto();
        request1.setId(1L);
        request1.setDescription("Нужна дрель");
        request1.setCreated(LocalDateTime.now().minusDays(1));

        ItemRequestResponseDto request2 = new ItemRequestResponseDto();
        request2.setId(2L);
        request2.setDescription("Нужен перфоратор");
        request2.setCreated(LocalDateTime.now());

        List<ItemRequestResponseDto> requests = List.of(request2, request1); // новые сначала

        when(requestService.getUserRequests(1L)).thenReturn(requests);

        // Действие и проверка
        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Нужен перфоратор"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].description").value("Нужна дрель"));
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() throws Exception {
        // Подготовка
        ItemRequestResponseDto request1 = new ItemRequestResponseDto();
        request1.setId(1L);
        request1.setDescription("Чужой запрос 1");
        request1.setCreated(LocalDateTime.now().minusDays(1));

        ItemRequestResponseDto request2 = new ItemRequestResponseDto();
        request2.setId(2L);
        request2.setDescription("Чужой запрос 2");
        request2.setCreated(LocalDateTime.now());

        List<ItemRequestResponseDto> requests = List.of(request2, request1); // новые сначала

        // Метод принимает только userId
        when(requestService.getAllRequests(1L)).thenReturn(requests);

        // Действие и проверка
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Чужой запрос 2"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].description").value("Чужой запрос 1"));
    }

    @Test
    void getRequestById_ShouldReturnRequest() throws Exception {
        // Подготовка
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужна дрель");
        responseDto.setCreated(LocalDateTime.now());

        when(requestService.getRequestById(eq(1L), eq(1L))).thenReturn(responseDto);

        // Действие и проверка
        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.created").exists());
    }
}