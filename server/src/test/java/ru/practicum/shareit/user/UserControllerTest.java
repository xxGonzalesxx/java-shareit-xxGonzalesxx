package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void findAll_ShouldReturnList() throws Exception {
        // Подготовка
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setName("User1");
        user1.setEmail("user1@test.com");

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setName("User2");
        user2.setEmail("user2@test.com");

        when(userService.findAll()).thenReturn(List.of(user1, user2));

        // Действие и проверка
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[0].email").value("user1@test.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("User2"))
                .andExpect(jsonPath("$[1].email").value("user2@test.com"));
    }

    @Test
    void findById_ShouldReturnUser() throws Exception {
        // Подготовка
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("User");
        user.setEmail("user@test.com");

        when(userService.findById(1L)).thenReturn(user);

        // Действие и проверка
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void create_ShouldReturnCreated() throws Exception {
        // Подготовка
        UserDto userToCreate = new UserDto();
        userToCreate.setName("New User");
        userToCreate.setEmail("new@test.com");

        UserDto createdUser = new UserDto();
        createdUser.setId(1L);
        createdUser.setName("New User");
        createdUser.setEmail("new@test.com");

        when(userService.create(any(UserDto.class))).thenReturn(createdUser);

        // Действие и проверка
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void update_ShouldReturnUpdatedUser() throws Exception {
        // Подготовка
        UserDto updateData = new UserDto();
        updateData.setName("Updated Name");

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("user@test.com");

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updatedUser);

        // Действие и проверка
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void delete_ShouldReturnOk() throws Exception {
        // Подготовка
        doNothing().when(userService).delete(1L);

        // Действие и проверка
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}