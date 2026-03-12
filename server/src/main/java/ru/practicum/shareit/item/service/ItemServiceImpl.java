package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public List<ItemDto> findAllByUser(Long userId) {
        log.info("Получение всех вещей пользователя {}", userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        return items.stream()
                .map(item -> {
                    List<Booking> bookings = bookingRepository.findByItemIdOrderByStartDesc(item.getId());
                    List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());

                    Booking lastBooking = null;
                    Booking nextBooking = null;

                    // Ищем последнее завершённое бронирование
                    lastBooking = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getEnd().isBefore(LocalDateTime.now()))
                            .findFirst()
                            .orElse(null);

                    // Ищем следующее будущее бронирование
                    nextBooking = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getStart().isAfter(LocalDateTime.now()))
                            .findFirst()
                            .orElse(null);

                    BookingDto lastBookingDto = lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null;
                    BookingDto nextBookingDto = nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null;

                    return ItemMapper.toItemDtoWithBookingsAndComments(item, lastBookingDto, nextBookingDto, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(Long id) {
        log.info("Получение вещи с id {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(id);
        log.info("Для вещи {} найдено комментариев: {}", id, comments.size());

        return ItemMapper.toItemDtoWithBookingsAndComments(item, null, null, comments);
    }

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи для пользователя {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (itemDto.getRequestId() != null) {
            requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(userId);

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи {} для пользователя {}", itemId, userId);

        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!userId.equals(existing.getOwnerId())) {
            throw new ForbiddenException("Доступ запрещён");
        }

        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        Item updated = itemRepository.save(existing);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Удаление вещи {}", id);
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        log.info("Добавление комментария к вещи {} от пользователя {}", itemId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверка, что пользователь брал вещь в аренду И аренда завершилась
        boolean hasCompletedBooking = bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        log.info("Проверка завершённого бронирования для пользователя {} и вещи {}: {}",
                userId, itemId, hasCompletedBooking);

        if (!hasCompletedBooking) {
            log.warn("Пользователь {} не имеет завершённого бронирования вещи {}", userId, itemId);
            throw new ValidationException("Вы можете оставить комментарий только после завершения аренды");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = CommentMapper.toComment(text, item, user);
        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий сохранён с id: {} для вещи: {}", savedComment.getId(), itemId);

        return CommentMapper.toCommentDto(savedComment);
    }
}