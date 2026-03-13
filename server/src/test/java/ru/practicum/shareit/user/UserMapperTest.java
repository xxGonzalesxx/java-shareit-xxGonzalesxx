package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_ShouldMapAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");

        UserDto userDto = UserMapper.toUserDto(user);

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
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@test.com");

        User user = UserMapper.toUser(userDto);

        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@test.com", user.getEmail());
    }

    @Test
    void toUser_WithNull_ShouldReturnNull() {
        assertNull(UserMapper.toUser(null));
    }

    // ТЕСТЫ ДЛЯ ПОКРЫТИЯ ВСЕХ СТРОК:

    @Test
    void toUserDto_WithNullId_ShouldWork() {
        User user = new User();
        user.setId(null);
        user.setName("John");
        user.setEmail("john@test.com");

        UserDto userDto = UserMapper.toUserDto(user);

        assertNull(userDto.getId());
        assertEquals("John", userDto.getName());
        assertEquals("john@test.com", userDto.getEmail());
    }

    @Test
    void toUserDto_WithNullName_ShouldWork() {
        User user = new User();
        user.setId(1L);
        user.setName(null);
        user.setEmail("john@test.com");

        UserDto userDto = UserMapper.toUserDto(user);

        assertEquals(1L, userDto.getId());
        assertNull(userDto.getName());
        assertEquals("john@test.com", userDto.getEmail());
    }

    @Test
    void toUserDto_WithNullEmail_ShouldWork() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail(null);

        UserDto userDto = UserMapper.toUserDto(user);

        assertEquals(1L, userDto.getId());
        assertEquals("John", userDto.getName());
        assertNull(userDto.getEmail());
    }

    @Test
    void toUserDto_WithAllFieldsNull_ShouldWork() {
        User user = new User();
        user.setId(null);
        user.setName(null);
        user.setEmail(null);

        UserDto userDto = UserMapper.toUserDto(user);

        assertNull(userDto.getId());
        assertNull(userDto.getName());
        assertNull(userDto.getEmail());
    }

    @Test
    void toUser_WithNullId_ShouldWork() {
        UserDto userDto = new UserDto();
        userDto.setId(null);
        userDto.setName("John");
        userDto.setEmail("john@test.com");

        User user = UserMapper.toUser(userDto);

        assertNull(user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
    }

    @Test
    void toUser_WithNullName_ShouldWork() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName(null);
        userDto.setEmail("john@test.com");

        User user = UserMapper.toUser(userDto);

        assertEquals(1L, user.getId());
        assertNull(user.getName());
        assertEquals("john@test.com", user.getEmail());
    }

    @Test
    void toUser_WithNullEmail_ShouldWork() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John");
        userDto.setEmail(null);

        User user = UserMapper.toUser(userDto);

        assertEquals(1L, user.getId());
        assertEquals("John", user.getName());
        assertNull(user.getEmail());
    }

    @Test
    void toUser_WithAllFieldsNull_ShouldWork() {
        UserDto userDto = new UserDto();
        userDto.setId(null);
        userDto.setName(null);
        userDto.setEmail(null);

        User user = UserMapper.toUser(userDto);

        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
    }
}