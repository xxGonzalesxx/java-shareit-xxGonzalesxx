package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
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

    private UserDto owner;
    private UserDto booker;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        // Создаем владельца
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner");
        ownerDto.setEmail("owner" + System.currentTimeMillis() + "@test.com");
        owner = userService.create(ownerDto);

        // Создаем арендатора (для комментариев)
        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker" + System.currentTimeMillis() + "@test.com");
        booker = userService.create(bookerDto);

        // Создаем вещь
        itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
    }

    @Test
    void create_ShouldSaveItem() {
        // Действие
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        // Проверка
        assertNotNull(savedItem.getId());
        assertEquals("Дрель", savedItem.getName());
        assertEquals("Аккумуляторная дрель", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
    }

    @Test
    void create_WithUnknownUser_ShouldThrowException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(999L, itemDto));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void findById_ShouldReturnItem() {
        // Подготовка
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        // Действие
        ItemDto foundItem = itemService.findById(savedItem.getId());

        // Проверка
        assertEquals(savedItem.getId(), foundItem.getId());
        assertEquals(savedItem.getName(), foundItem.getName());
        assertEquals(savedItem.getDescription(), foundItem.getDescription());
    }

    @Test
    void findById_WithWrongId_ShouldThrowException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.findById(999L));

        assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void update_ShouldChangeItemData() {
        // Подготовка
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое имя");
        updateDto.setDescription("Новое описание");
        updateDto.setAvailable(false);

        // Действие - ИСПРАВЛЕНО: правильный порядок параметров
        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        // Проверка
        assertEquals("Новое имя", updatedItem.getName());
        assertEquals("Новое описание", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
        assertEquals(savedItem.getId(), updatedItem.getId());
    }

    @Test
    void update_OnlyName_ShouldNotChangeOtherFields() {
        // Подготовка
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Только имя");

        // Действие - ИСПРАВЛЕНО: правильный порядок параметров
        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        // Проверка
        assertEquals("Только имя", updatedItem.getName());
        assertEquals(savedItem.getDescription(), updatedItem.getDescription());
        assertEquals(savedItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_ByNotOwner_ShouldThrowException() {
        // Подготовка
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Хакер");

        // Действие и проверка - ИСПРАВЛЕНО: booker пытается обновить вещь owner
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> itemService.update(booker.getId(), savedItem.getId(), updateDto));

        assertEquals("Доступ запрещён", exception.getMessage());
    }

    @Test
    void findAllByUser_ShouldReturnAllItems() {
        // Подготовка
        itemService.create(owner.getId(), itemDto);

        ItemDto secondItem = new ItemDto();
        secondItem.setName("Перфоратор");
        secondItem.setDescription("Мощный");
        secondItem.setAvailable(true);
        itemService.create(owner.getId(), secondItem);

        // Действие
        List<ItemDto> items = itemService.findAllByUser(owner.getId());

        // Проверка
        assertEquals(2, items.size());
    }

    @Test
    void search_ShouldFindItemsByText() {
        // Подготовка
        itemService.create(owner.getId(), itemDto);

        ItemDto secondItem = new ItemDto();
        secondItem.setName("Молоток");
        secondItem.setDescription("Тяжелый");
        secondItem.setAvailable(true);
        itemService.create(owner.getId(), secondItem);

        // Действие и проверка - поиск по имени
        List<ItemDto> foundByName = itemService.search("Дрель");
        assertEquals(1, foundByName.size());
        assertEquals("Дрель", foundByName.get(0).getName());

        // Поиск по описанию
        List<ItemDto> foundByDesc = itemService.search("Тяжелый");
        assertEquals(1, foundByDesc.size());
        assertEquals("Молоток", foundByDesc.get(0).getName());

        // Поиск по части слова
        List<ItemDto> foundByPart = itemService.search("дре");
        assertEquals(1, foundByPart.size());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        // Подготовка
        itemService.create(owner.getId(), itemDto);

        // Действие и проверка
        List<ItemDto> result = itemService.search("");
        assertTrue(result.isEmpty());

        result = itemService.search(null);
        assertTrue(result.isEmpty());

        result = itemService.search("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_WithUnknownUser_ShouldThrowException() {
        // Подготовка
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(999L, savedItem.getId(), "Комментарий"));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void addComment_WithUnknownItem_ShouldThrowException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(booker.getId(), 999L, "Комментарий"));

        assertEquals("Вещь не найдена", exception.getMessage());
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
    void addComment_WithoutBooking_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), savedItem.getId(), "Отзыв"));

        assertEquals("Вы можете оставить комментарий только после завершения аренды",
                exception.getMessage());
    }

    @Test
    void addComment_WithEmptyText_ShouldThrowException() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), savedItem.getId(), ""));
    }
}