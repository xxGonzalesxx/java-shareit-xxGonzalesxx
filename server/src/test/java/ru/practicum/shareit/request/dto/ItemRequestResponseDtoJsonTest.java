package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    void shouldConvertToJson() throws Exception {
        // Подготовка
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 10, 0);

        ItemDto item1 = new ItemDto();
        item1.setId(5L);
        item1.setName("Дрель");

        ItemDto item2 = new ItemDto();
        item2.setId(6L);
        item2.setName("Перфоратор");

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужны инструменты");
        responseDto.setCreated(created);
        responseDto.setItems(List.of(item1, item2));

        // Действие
        String jsonString = json.write(responseDto).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"description\":\"Нужны инструменты\"");
        assertThat(jsonString).contains("\"created\":\"2025-01-01T10:00:00\"");
        assertThat(jsonString).contains("\"items\":");
        assertThat(jsonString).contains("\"id\":5");
        assertThat(jsonString).contains("\"name\":\"Дрель\"");
        assertThat(jsonString).contains("\"id\":6");
        assertThat(jsonString).contains("\"name\":\"Перфоратор\"");
    }

    @Test
    void shouldConvertFromJson() throws Exception {
        // Подготовка
        String jsonString = "{\"id\":1,\"description\":\"Нужны инструменты\",\"created\":\"2025-01-01T10:00:00\",\"items\":[{\"id\":5,\"name\":\"Дрель\"},{\"id\":6,\"name\":\"Перфоратор\"}]}";

        // Действие
        ItemRequestResponseDto responseDto = json.parse(jsonString).getObject();

        // Проверка
        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getDescription()).isEqualTo("Нужны инструменты");
        assertThat(responseDto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(responseDto.getItems()).hasSize(2);
        assertThat(responseDto.getItems().get(0).getId()).isEqualTo(5L);
        assertThat(responseDto.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(responseDto.getItems().get(1).getId()).isEqualTo(6L);
        assertThat(responseDto.getItems().get(1).getName()).isEqualTo("Перфоратор");
    }

    @Test
    void shouldWorkWithEmptyItems() throws Exception {
        // Подготовка
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 10, 0);

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужны инструменты");
        responseDto.setCreated(created);
        responseDto.setItems(List.of());

        // Действие
        String jsonString = json.write(responseDto).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"description\":\"Нужны инструменты\"");
        assertThat(jsonString).contains("\"created\":\"2025-01-01T10:00:00\"");
        assertThat(jsonString).contains("\"items\":[]");
    }
}