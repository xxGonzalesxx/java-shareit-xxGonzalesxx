package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_ShouldMapAllFields() {
        // Подготовка
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");
        item.setAvailable(true);
        item.setOwnerId(2L);
        item.setRequestId(3L);

        // Действие
        ItemDto itemDto = ItemMapper.toItemDto(item);

        // Проверка
        assertEquals(1L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Аккумуляторная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
        assertEquals(3L, itemDto.getRequestId());
    }

    @Test
    void toItemDto_WithNull_ShouldReturnNull() {
        assertNull(ItemMapper.toItemDto(null));
    }

    @Test
    void toItemDtoWithBookingsAndComments_ShouldMapAllFields() {
        // Подготовка
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");
        item.setAvailable(true);

        BookingDto lastBooking = new BookingDto();
        lastBooking.setId(10L);

        BookingDto nextBooking = new BookingDto();
        nextBooking.setId(11L);

        User author = new User();
        author.setId(5L);
        author.setName("User");

        Comment comment = new Comment();
        comment.setId(20L);
        comment.setText("Отличная вещь");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        List<Comment> comments = List.of(comment);

        // Действие
        ItemDto itemDto = ItemMapper.toItemDtoWithBookingsAndComments(item, lastBooking, nextBooking, comments);

        // Проверка
        assertEquals(1L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Аккумуляторная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
        assertEquals(10L, itemDto.getLastBooking().getId());
        assertEquals(11L, itemDto.getNextBooking().getId());
        assertEquals(1, itemDto.getComments().size());
        assertEquals(20L, itemDto.getComments().get(0).getId());
        assertEquals("Отличная вещь", itemDto.getComments().get(0).getText());
    }

    @Test
    void toItemDtoWithBookingsAndComments_WithNullComments_ShouldReturnEmptyList() {
        // Подготовка
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        // Действие
        ItemDto itemDto = ItemMapper.toItemDtoWithBookingsAndComments(item, null, null, null);

        // Проверка
        assertNotNull(itemDto.getComments());
        assertTrue(itemDto.getComments().isEmpty());
    }

    @Test
    void toItem_ShouldMapAllFields() {
        // Подготовка
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(3L);

        // Действие
        Item item = ItemMapper.toItem(itemDto);

        // Проверка
        assertEquals(1L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Аккумуляторная дрель", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(3L, item.getRequestId());
    }

    @Test
    void toItem_WithNull_ShouldReturnNull() {
        assertNull(ItemMapper.toItem(null));
    }
}