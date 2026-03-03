package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public static ItemDto toItemDtoWithBookingsAndComments(Item item,
                                                           BookingDto lastBooking,
                                                           BookingDto nextBooking,
                                                           List<Comment> comments) {
        if (item == null) return null;

        List<CommentDto> commentDtos = comments != null
                ? comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList())
                : List.of();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        if (itemDto == null) return null;

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
    }
}