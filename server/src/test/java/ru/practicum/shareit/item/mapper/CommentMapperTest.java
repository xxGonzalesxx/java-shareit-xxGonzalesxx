package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toCommentDto_ShouldMapAllFields() {
        // Подготовка
        User author = new User();
        author.setId(5L);
        author.setName("Test User");

        Item item = new Item();
        item.setId(10L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная вещь");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.of(2025, 1, 1, 10, 0));

        // Действие
        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        // Проверка
        assertEquals(1L, commentDto.getId());
        assertEquals("Отличная вещь", commentDto.getText());
        assertEquals("Test User", commentDto.getAuthorName());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), commentDto.getCreated());
    }

    @Test
    void toCommentDto_WithNull_ShouldReturnNull() {
        assertNull(CommentMapper.toCommentDto(null));
    }

    @Test
    void toComment_ShouldMapAllFields() {
        // Подготовка
        String text = "Отличная вещь";
        Item item = new Item();
        item.setId(10L);
        User author = new User();
        author.setId(5L);

        // Действие
        Comment comment = CommentMapper.toComment(text, item, author);

        // Проверка
        assertEquals("Отличная вещь", comment.getText());
        assertEquals(10L, comment.getItem().getId());
        assertEquals(5L, comment.getAuthor().getId());
        assertNotNull(comment.getCreated());
    }

    @Test
    void toComment_WithNull_ShouldHandleNull() {
        Comment comment = CommentMapper.toComment(null, null, null);
        assertNull(comment.getText());
        assertNull(comment.getItem());
        assertNull(comment.getAuthor());
        assertNotNull(comment.getCreated());
    }
}