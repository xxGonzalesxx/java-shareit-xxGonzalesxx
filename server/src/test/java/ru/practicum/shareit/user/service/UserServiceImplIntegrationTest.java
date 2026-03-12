package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@test.com");
    }

    @Test
    void create_ShouldSaveUser() {
        // Действие
        UserDto savedUser = userService.create(userDto);

        // Проверка
        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@test.com", savedUser.getEmail());
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowException() {
        // Подготовка
        userService.create(userDto);

        UserDto duplicateUser = new UserDto();
        duplicateUser.setName("Jane Doe");
        duplicateUser.setEmail("john@test.com"); // Тот же email

        // Действие и проверка
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.create(duplicateUser));

        assertEquals("Email уже существует", exception.getMessage());
    }

    @Test
    void findById_ShouldReturnUser() {
        // Подготовка
        UserDto savedUser = userService.create(userDto);

        // Действие
        UserDto foundUser = userService.findById(savedUser.getId());

        // Проверка
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getName(), foundUser.getName());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void findById_WithWrongId_ShouldThrowException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findById(999L));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Подготовка
        userService.create(userDto);

        UserDto secondUser = new UserDto();
        secondUser.setName("Jane Doe");
        secondUser.setEmail("jane@test.com");
        userService.create(secondUser);

        // Действие
        List<UserDto> users = userService.findAll();

        // Проверка
        assertEquals(2, users.size());
    }

    @Test
    void update_ShouldChangeUserData() {
        // Подготовка
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@test.com");

        // Действие
        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        // Проверка
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
        assertEquals(savedUser.getId(), updatedUser.getId());
    }

    @Test
    void update_OnlyName_ShouldNotChangeEmail() {
        // Подготовка
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Only Name");

        // Действие
        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        // Проверка
        assertEquals("Only Name", updatedUser.getName());
        assertEquals(savedUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_OnlyEmail_ShouldNotChangeName() {
        // Подготовка
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setEmail("only.email@test.com");

        // Действие
        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        // Проверка
        assertEquals(savedUser.getName(), updatedUser.getName());
        assertEquals("only.email@test.com", updatedUser.getEmail());
    }

    @Test
    void update_WithDuplicateEmail_ShouldThrowException() {
        // Подготовка - создаем первого пользователя
        userService.create(userDto); // email: john@test.com

        // Создаем второго пользователя с другим email
        UserDto secondUserDto = new UserDto();
        secondUserDto.setName("Jane Doe");
        secondUserDto.setEmail("jane@test.com");
        UserDto secondUser = userService.create(secondUserDto);

        // Сохраняем email первого пользователя в финальную переменную
        final String existingEmail = "john@test.com";

        UserDto updateDto = new UserDto();
        updateDto.setEmail(existingEmail); // Пытаемся установить email первого пользователя

        // Действие и проверка
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.update(secondUser.getId(), updateDto));

        assertEquals("Email уже существует", exception.getMessage());
    }

    @Test
    void update_WithSameEmail_ShouldSucceed() {
        // Подготовка
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setEmail("john@test.com"); // Тот же email
        updateDto.setName("New Name");

        // Действие
        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        // Проверка
        assertEquals("New Name", updatedUser.getName());
        assertEquals("john@test.com", updatedUser.getEmail());
    }

    @Test
    void update_WithWrongId_ShouldThrowException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(999L, new UserDto()));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Подготовка
        UserDto savedUser = userService.create(userDto);
        Long userId = savedUser.getId();

        // Действие
        userService.delete(userId);

        // Проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findById(userId));

        assertEquals("Пользователь не найден", exception.getMessage());
    }
}