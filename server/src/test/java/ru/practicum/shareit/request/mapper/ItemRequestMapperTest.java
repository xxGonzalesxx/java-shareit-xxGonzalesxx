package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    @Test
    void toItemRequestDto_ShouldMapAllFields() {
        // Подготовка
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2025, 1, 1, 10, 0));

        // Действие
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(request);

        // Проверка
        assertEquals(1L, requestDto.getId());
        assertEquals("Нужна дрель", requestDto.getDescription());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), requestDto.getCreated());
    }

    @Test
    void toItemRequestDto_WithNull_ShouldReturnNull() {
        assertNull(ItemRequestMapper.toItemRequestDto(null));
    }

    @Test
    void toItemRequestResponseDto_ShouldMapAllFields() {
        // Подготовка
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2025, 1, 1, 10, 0));

        ItemDto item1 = new ItemDto();
        item1.setId(5L);
        item1.setName("Дрель");

        ItemDto item2 = new ItemDto();
        item2.setId(6L);
        item2.setName("Перфоратор");

        List<ItemDto> items = List.of(item1, item2);

        // Действие
        ItemRequestResponseDto responseDto = ItemRequestMapper.toItemRequestResponseDto(request, items);

        // Проверка
        assertEquals(1L, responseDto.getId());
        assertEquals("Нужна дрель", responseDto.getDescription());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), responseDto.getCreated());
        assertEquals(2, responseDto.getItems().size());
        assertEquals(5L, responseDto.getItems().get(0).getId());
        assertEquals("Дрель", responseDto.getItems().get(0).getName());
        assertEquals(6L, responseDto.getItems().get(1).getId());
        assertEquals("Перфоратор", responseDto.getItems().get(1).getName());
    }

    @Test
    void toItemRequestResponseDto_WithNull_ShouldReturnNull() {
        assertNull(ItemRequestMapper.toItemRequestResponseDto(null, null));
    }

    @Test
    void toItemRequestResponseDto_WithEmptyItems_ShouldReturnEmptyList() {
        // Подготовка
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2025, 1, 1, 10, 0));

        // Действие
        ItemRequestResponseDto responseDto = ItemRequestMapper.toItemRequestResponseDto(request, List.of());

        // Проверка
        assertEquals(1L, responseDto.getId());
        assertEquals("Нужна дрель", responseDto.getDescription());
        assertNotNull(responseDto.getItems());
        assertTrue(responseDto.getItems().isEmpty());
    }

    @Test
    void toItemRequest_ShouldMapAllFields() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        User requestor = new User();
        requestor.setId(1L);
        requestor.setName("User");

        // Действие
        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, requestor);

        // Проверка
        assertNull(request.getId()); // ID не должен устанавливаться
        assertEquals("Нужна дрель", request.getDescription());
        assertEquals(1L, request.getRequestor().getId());
        assertEquals("User", request.getRequestor().getName());
        assertNotNull(request.getCreated());
    }

    @Test
    void toItemRequest_WithNullDto_ShouldReturnNull() {
        User requestor = new User();
        requestor.setId(1L);

        assertNull(ItemRequestMapper.toItemRequest(null, requestor));
    }
}