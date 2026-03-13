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
        UserDto requestorDto = new UserDto();
        requestorDto.setName("Requestor");
        requestorDto.setEmail("requestor" + System.currentTimeMillis() + "@test.com");
        requestor = userService.create(requestorDto);

        UserDto anotherDto = new UserDto();
        anotherDto.setName("Another");
        anotherDto.setEmail("another" + System.currentTimeMillis() + "@test.com");
        anotherUser = userService.create(anotherDto);
    }

    @Test
    void create_ShouldSaveRequest() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен мощный перфоратор");

        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        assertNotNull(savedRequest.getId());
        assertEquals("Нужен мощный перфоратор", savedRequest.getDescription());
        assertNotNull(savedRequest.getCreated());
        assertTrue(savedRequest.getItems().isEmpty());
    }

    @Test
    void create_WithEmptyDescription_ShouldThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> requestService.create(requestor.getId(), requestDto));

        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void create_WithNullDescription_ShouldThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> requestService.create(requestor.getId(), requestDto));

        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void create_WithUnknownUser_ShouldThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.create(999L, requestDto));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void getUserRequests_ShouldReturnAllUserRequests() {
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Нужна дрель");
        requestService.create(requestor.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Нужен перфоратор");
        requestService.create(requestor.getId(), request2);

        List<ItemRequestResponseDto> requests = requestService.getUserRequests(requestor.getId());

        assertEquals(2, requests.size());
    }

    @Test
    void getUserRequests_WithItems_ShouldIncludeItems() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());
        itemService.create(anotherUser.getId(), itemDto);

        List<ItemRequestResponseDto> requests = requestService.getUserRequests(requestor.getId());

        assertEquals(1, requests.size());
        assertEquals(1, requests.get(0).getItems().size());
        assertEquals("Дрель", requests.get(0).getItems().get(0).getName());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Нужна дрель");
        requestService.create(requestor.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Нужен перфоратор");
        requestService.create(anotherUser.getId(), request2);

        ItemRequestDto request3 = new ItemRequestDto();
        request3.setDescription("Нужна пила");
        requestService.create(anotherUser.getId(), request3);

        List<ItemRequestResponseDto> requests = requestService.getAllRequests(requestor.getId());

        assertEquals(2, requests.size());
    }

    @Test
    void getAllRequests_WithNoOtherRequests_ShouldReturnEmptyList() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        requestService.create(requestor.getId(), requestDto);

        List<ItemRequestResponseDto> requests = requestService.getAllRequests(requestor.getId());

        assertTrue(requests.isEmpty());
    }

    @Test
    void getAllRequests_WithPagination_ShouldWorkCorrectly() {
        UserDto anotherUser2 = new UserDto();
        anotherUser2.setName("Another2");
        anotherUser2.setEmail("another2" + System.currentTimeMillis() + "@test.com");
        anotherUser2 = userService.create(anotherUser2);

        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Запрос 1");
        requestService.create(anotherUser.getId(), request1);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Запрос 2");
        requestService.create(anotherUser2.getId(), request2);

        ItemRequestDto request3 = new ItemRequestDto();
        request3.setDescription("Запрос 3");
        requestService.create(anotherUser.getId(), request3);

        List<ItemRequestResponseDto> requests = requestService.getAllRequests(requestor.getId());

        assertEquals(3, requests.size());
    }

    @Test
    void getRequestById_ShouldReturnRequestWithItems() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(savedRequest.getId());
        itemService.create(anotherUser.getId(), itemDto);

        ItemRequestResponseDto foundRequest = requestService.getRequestById(anotherUser.getId(),
                savedRequest.getId());

        assertEquals(savedRequest.getId(), foundRequest.getId());
        assertEquals("Нужна дрель", foundRequest.getDescription());
        assertEquals(1, foundRequest.getItems().size());
        assertEquals("Дрель", foundRequest.getItems().get(0).getName());
    }

    @Test
    void getRequestById_WithWrongId_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(requestor.getId(), 999L));

        assertEquals("Запрос с id=999 не найден", exception.getMessage());
    }

    @Test
    void getRequestById_WithUnknownUser_ShouldThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestResponseDto savedRequest = requestService.create(requestor.getId(), requestDto);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, savedRequest.getId()));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }
}