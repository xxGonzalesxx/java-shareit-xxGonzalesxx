package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void shouldConvertToJson() throws Exception {
        // Подготовка
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        item.setRequestId(5L);

        // Действие
        String jsonString = json.write(item).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":\"Дрель\"");
        assertThat(jsonString).contains("\"description\":\"Аккумуляторная\"");
        assertThat(jsonString).contains("\"available\":true");
        assertThat(jsonString).contains("\"requestId\":5");
    }

    @Test
    void shouldConvertFromJson() throws Exception {
        // Подготовка
        String jsonString = "{\"id\":1,\"name\":\"Дрель\",\"description\":\"Аккумуляторная\",\"available\":true,\"requestId\":5}";

        // Действие
        ItemDto item = json.parse(jsonString).getObject();

        // Проверка
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Аккумуляторная");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getRequestId()).isEqualTo(5L);
    }

    @Test
    void shouldWorkWithNullFields() throws Exception {
        // Подготовка
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");

        // Действие
        String jsonString = json.write(item).getJson();

        // Проверка
        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":\"Дрель\"");
        assertThat(jsonString).contains("\"description\":null");
        assertThat(jsonString).contains("\"available\":null");
        assertThat(jsonString).contains("\"requestId\":null");
    }
}