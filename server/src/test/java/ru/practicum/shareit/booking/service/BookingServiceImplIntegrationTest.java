package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        // Создаем владельца
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner");
        ownerDto.setEmail("owner" + System.currentTimeMillis() + "@test.com");
        owner = userService.create(ownerDto);

        // Создаем арендатора
        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker" + System.currentTimeMillis() + "@test.com");
        booker = userService.create(bookerDto);

        // Создаем вещь
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        item = itemService.create(owner.getId(), itemDto);

        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(2);
    }

    @Test
    void create_ShouldSaveBooking() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        // Действие
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Проверка
        assertNotNull(savedBooking.getId());
        assertEquals(booker.getId(), savedBooking.getBooker().getId());
        assertEquals(item.getId(), savedBooking.getItem().getId());
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());
        assertEquals(start, savedBooking.getStart());
        assertEquals(end, savedBooking.getEnd());
    }

    @Test
    void create_WithUnavailableItem_ShouldThrowException() {
        // Подготовка - делаем вещь недоступной
        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);
        // ИСПРАВЛЕНО: правильный порядок параметров
        itemService.update(owner.getId(), item.getId(), updateDto);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Вещь недоступна для бронирования", exception.getMessage());
    }

    @Test
    void create_ByOwner_ShouldThrowException() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(owner.getId(), bookingDto));

        assertEquals("Владелец не может бронировать свою вещь", exception.getMessage());
    }

    @Test
    void approve_ShouldChangeStatusToApproved() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Действие
        BookingResponseDto approvedBooking = bookingService.approve(owner.getId(),
                savedBooking.getId(), true);

        // Проверка
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void approve_ShouldChangeStatusToRejected() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Действие
        BookingResponseDto rejectedBooking = bookingService.approve(owner.getId(),
                savedBooking.getId(), false);

        // Проверка
        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void approve_ByNotOwner_ShouldThrowException() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Создаем постороннего пользователя
        UserDto strangerDto = new UserDto();
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger" + System.currentTimeMillis() + "@test.com");
        UserDto stranger = userService.create(strangerDto);

        // Действие и проверка
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> bookingService.approve(stranger.getId(), savedBooking.getId(), true));

        assertEquals("Только владелец может подтверждать бронирование", exception.getMessage());
    }

    @Test
    void approve_AlreadyApproved_ShouldThrowException() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Подтверждаем бронирование
        bookingService.approve(owner.getId(), savedBooking.getId(), true);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), savedBooking.getId(), true));

        assertEquals("Бронирование уже обработано", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnBooking() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Действие
        BookingResponseDto foundBooking = bookingService.getById(booker.getId(),
                savedBooking.getId());

        // Проверка
        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getById_OwnerCanSee() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Действие
        BookingResponseDto foundBooking = bookingService.getById(owner.getId(),
                savedBooking.getId());

        // Проверка
        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getById_StrangerCannotSee() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        // Создаем постороннего пользователя
        UserDto strangerDto = new UserDto();
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger" + System.currentTimeMillis() + "@test.com");
        UserDto stranger = userService.create(strangerDto);

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(stranger.getId(), savedBooking.getId()));

        assertEquals("Нет доступа к просмотру этого бронирования", exception.getMessage());
    }

    @Test
    void getUserBookings_ShouldReturnAllBookings() {
        // Подготовка - создаем два бронирования
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setItemId(item.getId());
        bookingDto1.setStart(start);
        bookingDto1.setEnd(end);
        bookingService.create(booker.getId(), bookingDto1);

        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setItemId(item.getId());
        bookingDto2.setStart(start.plusDays(3));
        bookingDto2.setEnd(end.plusDays(3));
        bookingService.create(booker.getId(), bookingDto2);

        // Действие
        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "ALL");

        // Проверка
        assertEquals(2, bookings.size());
    }

    @Test
    void getUserBookings_WithStateWaiting_ShouldReturnWaiting() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        // Действие
        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(),
                "WAITING");

        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookings() {
        // Подготовка
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        // Действие
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "ALL");

        // Проверка
        assertEquals(1, bookings.size());
    }
}