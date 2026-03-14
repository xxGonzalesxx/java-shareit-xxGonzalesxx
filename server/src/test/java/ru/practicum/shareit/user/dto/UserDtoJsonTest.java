package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void shouldConvertToJson() throws Exception {
        // Подготовка
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");

        // Действие
        String jsonString = json.write(user).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":\"John Doe\"");
        assertThat(jsonString).contains("\"email\":\"john@test.com\"");
    }

    @Test
    void shouldConvertFromJson() throws Exception {
        // Подготовка
        String jsonString = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@test.com\"}";

        // Действие
        UserDto user = json.parse(jsonString).getObject();

        // Проверка
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void shouldWorkWithNullFields() throws Exception {
        // Подготовка
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("John Doe");

        // Действие
        String jsonString = json.write(user).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":\"John Doe\"");
        assertThat(jsonString).contains("\"email\":null");
    }
}