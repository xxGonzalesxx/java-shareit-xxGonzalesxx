package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createItem_ShouldReturnOk() throws Exception {
        // Подготовка
        ItemDto itemToCreate = new ItemDto();
        itemToCreate.setName("Дрель");
        itemToCreate.setDescription("Аккумуляторная");
        itemToCreate.setAvailable(true);

        ItemDto createdItem = new ItemDto();
        createdItem.setId(1L);
        createdItem.setName("Дрель");
        createdItem.setDescription("Аккумуляторная");
        createdItem.setAvailable(true);

        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(createdItem);

        // Действие и проверка
        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Аккумуляторная"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        // Подготовка
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);

        when(itemService.findById(1L)).thenReturn(item);

        // Действие и проверка
        mockMvc.perform(get("/items/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        // Подготовка
        ItemDto updateData = new ItemDto();
        updateData.setName("Новое имя");

        ItemDto updatedItem = new ItemDto();
        updatedItem.setId(1L);
        updatedItem.setName("Новое имя");
        updatedItem.setDescription("Аккумуляторная");
        updatedItem.setAvailable(true);

        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(updatedItem);

        // Действие и проверка
        mockMvc.perform(patch("/items/1")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void getAllByOwner_ShouldReturnList() throws Exception {
        // Подготовка
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Дрель");

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Перфоратор");

        List<ItemDto> items = List.of(item1, item2);

        when(itemService.findAllByUser(1L)).thenReturn(items);

        // Действие и проверка
        mockMvc.perform(get("/items")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Перфоратор"));
    }

    @Test
    void search_ShouldReturnItems() throws Exception {
        // Подготовка
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Дрель");

        List<ItemDto> items = List.of(item1);

        when(itemService.search("дрель")).thenReturn(items);

        // Действие и проверка
        mockMvc.perform(get("/items/search")
                        .header(userIdHeader, 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_ShouldReturnComment() throws Exception {
        // Подготовка
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Отличная вещь!");

        CommentDto responseDto = new CommentDto();
        responseDto.setId(1L);
        responseDto.setText("Отличная вещь!");
        responseDto.setAuthorName("User");
        responseDto.setCreated(LocalDateTime.now());

        when(itemService.addComment(eq(1L), eq(1L), eq("Отличная вещь!")))
                .thenReturn(responseDto);

        // Действие и проверка
        mockMvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }
}