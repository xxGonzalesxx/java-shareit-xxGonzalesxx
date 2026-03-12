package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    // Создать новый запрос
    ItemRequestResponseDto create(Long userId, ItemRequestDto requestDto);

    // Получить свои запросы (с ответами)
    List<ItemRequestResponseDto> getUserRequests(Long userId);

    // Получить запросы других пользователей
    List<ItemRequestResponseDto> getAllRequests(Long userId);

    // Получить запрос по id
    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}