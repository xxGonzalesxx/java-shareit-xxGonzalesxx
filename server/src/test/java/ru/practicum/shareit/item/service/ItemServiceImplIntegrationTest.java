package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner");
        ownerDto.setEmail("owner" + System.currentTimeMillis() + "@test.com");
        owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker" + System.currentTimeMillis() + "@test.com");
        booker = userService.create(bookerDto);

        itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
    }

    @Test
    void create_ShouldSaveItem() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(savedItem.getId());
        assertEquals("Дрель", savedItem.getName());
        assertEquals("Аккумуляторная дрель", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
    }

    @Test
    void create_WithUnknownUser_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(999L, itemDto));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void findById_ShouldReturnItem() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto foundItem = itemService.findById(savedItem.getId());

        assertEquals(savedItem.getId(), foundItem.getId());
    }

    @Test
    void findById_WithWrongId_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.findById(999L));

        assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void update_ShouldChangeAllFields() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое имя");
        updateDto.setDescription("Новое описание");
        updateDto.setAvailable(false);

        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        assertEquals("Новое имя", updatedItem.getName());
        assertEquals("Новое описание", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void update_OnlyName_ShouldWork() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Только имя");

        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        assertEquals("Только имя", updatedItem.getName());
        assertEquals(savedItem.getDescription(), updatedItem.getDescription());
        assertEquals(savedItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_OnlyDescription_ShouldWork() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("Только описание");

        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        assertEquals(savedItem.getName(), updatedItem.getName());
        assertEquals("Только описание", updatedItem.getDescription());
        assertEquals(savedItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_OnlyAvailable_ShouldWork() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);

        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        assertEquals(savedItem.getName(), updatedItem.getName());
        assertEquals(savedItem.getDescription(), updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void update_ByNotOwner_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Хакер");

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> itemService.update(booker.getId(), savedItem.getId(), updateDto));

        assertEquals("Доступ запрещён", exception.getMessage());
    }

    @Test
    void findAllByUser_ShouldReturnAllItems() {
        itemService.create(owner.getId(), itemDto);

        ItemDto secondItem = new ItemDto();
        secondItem.setName("Перфоратор");
        secondItem.setDescription("Мощный");
        secondItem.setAvailable(true);
        itemService.create(owner.getId(), secondItem);

        List<ItemDto> items = itemService.findAllByUser(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void findAllByUser_WithNoItems_ShouldReturnEmptyList() {
        List<ItemDto> items = itemService.findAllByUser(booker.getId());
        assertTrue(items.isEmpty());
    }

    @Test
    void search_ShouldFindItemsByText() {
        itemService.create(owner.getId(), itemDto);

        ItemDto secondItem = new ItemDto();
        secondItem.setName("Молоток");
        secondItem.setDescription("Тяжелый");
        secondItem.setAvailable(true);
        itemService.create(owner.getId(), secondItem);

        List<ItemDto> foundByName = itemService.search("Дрель");
        assertEquals(1, foundByName.size());

        List<ItemDto> foundByDesc = itemService.search("Тяжелый");
        assertEquals(1, foundByDesc.size());

        List<ItemDto> foundByPart = itemService.search("дре");
        assertEquals(1, foundByPart.size());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        itemService.create(owner.getId(), itemDto);

        assertTrue(itemService.search("").isEmpty());
        assertTrue(itemService.search(null).isEmpty());
        assertTrue(itemService.search("   ").isEmpty());
    }

    @Test
    void delete_ShouldRemoveItem() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        itemService.delete(savedItem.getId());

        assertThrows(NotFoundException.class,
                () -> itemService.findById(savedItem.getId()));
    }

    @Test
    void create_WithWrongRequestId_ShouldThrowException() {
        itemDto.setRequestId(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(owner.getId(), itemDto));

        assertEquals("Запрос не найден", exception.getMessage());
    }

    @Test
    void addComment_WithEmptyText_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        // Не создаем бронирование, проверяем только пустой текст
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), savedItem.getId(), ""));

        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void addComment_WithNullText_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        // Не создаем бронирование, проверяем только null
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), savedItem.getId(), null));

        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void addComment_WithUnknownUser_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(999L, savedItem.getId(), "Комментарий"));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void addComment_WithUnknownItem_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(booker.getId(), 999L, "Комментарий"));

        assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void addComment_WithoutBooking_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), savedItem.getId(), "Отзыв"));

        assertEquals("Вы можете оставить комментарий только после завершения аренды",
                exception.getMessage());
    }
}