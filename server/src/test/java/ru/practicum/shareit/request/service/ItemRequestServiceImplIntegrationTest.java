package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto requestor;
    private UserDto anotherUser;

    @BeforeEach
    void setUp() {
        // Создаем пользователя, который создает запросы
        UserDto requestorDto = new UserDto();
        requestorDto.setName("Requestor");
        requestorDto.setEmail("requestor" + System.currentTimeMillis() + "@test.com");
        requestor = userService.create(requestorDto);

        // Создаем другого пользователя (владельца вещей)
        UserDto anotherDto = new UserDto();
        anotherDto.setName("Another");
        anotherDto.setEmail("another" + System.currentTimeMillis() + "@test.com");
        anotherUser = userService.create(anotherDto);
    }

    @Test
    void create_ShouldSaveRequest() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен мощный перфоратор");

        // Действие
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        // Проверка
        assertNotNull(savedRequest.getId());
        assertEquals("Нужен мощный перфоратор", savedRequest.getDescription());
        assertNotNull(savedRequest.getCreated());
        assertTrue(savedRequest.getItems().isEmpty());
    }

    @Test
    void create_WithEmptyDescription_ShouldThrowException() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("");

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class,
                () -> requestService.create(requestor.getId(), requestDto));

        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void create_WithNullDescription_ShouldThrowException() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class,
                () -> requestService.create(requestor.getId(), requestDto));

        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void create_WithUnknownUser_ShouldThrowException() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.create(999L, requestDto));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void getUserRequests_ShouldReturnAllUserRequests() {
        // Подготовка
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Нужна дрель");
        requestService.create(requestor.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Нужен перфоратор");
        requestService.create(requestor.getId(), request2);

        // Действие
        List<ItemRequestResponseDto> requests = requestService.getUserRequests(requestor.getId());

        // Проверка (должны быть отсортированы от новых к старым)
        assertEquals(2, requests.size());
        assertEquals("Нужен перфоратор", requests.get(0).getDescription());
        assertEquals("Нужна дрель", requests.get(1).getDescription());
    }

    @Test
    void getUserRequests_WithItems_ShouldIncludeItems() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        // Создаем вещь в ответ на запрос (другой пользователь)
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());
        itemService.create(anotherUser.getId(), itemDto);

        // Действие
        List<ItemRequestResponseDto> requests = requestService.getUserRequests(requestor.getId());

        // Проверка
        assertEquals(1, requests.size());
        assertEquals(1, requests.get(0).getItems().size());
        assertEquals("Дрель", requests.get(0).getItems().get(0).getName());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        // Подготовка
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Нужна дрель");
        requestService.create(requestor.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Нужен перфоратор");
        requestService.create(anotherUser.getId(), request2);

        ItemRequestDto request3 = new ItemRequestDto();
        request3.setDescription("Нужна пила");
        requestService.create(anotherUser.getId(), request3);

        // Действие - получаем запросы других пользователей (не свои)
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(requestor.getId());

        // Проверка - должны быть только запросы от anotherUser
        assertEquals(2, requests.size());
        assertTrue(requests.stream().allMatch(r ->
                r.getDescription().contains("Нужен") || r.getDescription().contains("Нужна")));
    }

    @Test
    void getAllRequests_WithNoOtherRequests_ShouldReturnEmptyList() {
        // Подготовка - создаем запрос только от requestor
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        requestService.create(requestor.getId(), requestDto);

        // Действие
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(requestor.getId());

        // Проверка
        assertTrue(requests.isEmpty());
    }

    @Test
    void getRequestById_ShouldReturnRequestWithItems() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        // Создаем вещь в ответ на запрос
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());
        itemService.create(anotherUser.getId(), itemDto);

        // Действие
        ItemRequestResponseDto foundRequest = requestService.getRequestById(anotherUser.getId(),
                savedRequest.getId());

        // Проверка
        assertEquals(savedRequest.getId(), foundRequest.getId());
        assertEquals("Нужна дрель", foundRequest.getDescription());
        assertEquals(1, foundRequest.getItems().size());
        assertEquals("Дрель", foundRequest.getItems().get(0).getName());
    }

    @Test
    void getRequestById_WithWrongId_ShouldThrowException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(requestor.getId(), 999L));

        assertEquals("Запрос с id=999 не найден", exception.getMessage());
    }

    @Test
    void getRequestById_WithUnknownUser_ShouldThrowException() {
        // Подготовка
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, savedRequest.getId()));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }
}