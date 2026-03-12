package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_ShouldMapAllFields() {
        // Подготовка
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");

        // Действие
        UserDto userDto = UserMapper.toUserDto(user);

        // Проверка
        assertEquals(1L, userDto.getId());
        assertEquals("John Doe", userDto.getName());
        assertEquals("john@test.com", userDto.getEmail());
    }

    @Test
    void toUserDto_WithNull_ShouldReturnNull() {
        assertNull(UserMapper.toUserDto(null));
    }

    @Test
    void toUser_ShouldMapAllFields() {
        // Подготовка
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@test.com");

        // Действие
        User user = UserMapper.toUser(userDto);

        // Проверка
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@test.com", user.getEmail());
    }

    @Test
    void toUser_WithNull_ShouldReturnNull() {
        assertNull(UserMapper.toUser(null));
    }

    // Удаляем тест с updateUser, так как этого метода нет в маппере
}