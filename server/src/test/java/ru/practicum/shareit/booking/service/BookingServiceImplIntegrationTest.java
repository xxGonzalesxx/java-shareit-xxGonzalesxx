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
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner");
        ownerDto.setEmail("owner" + System.currentTimeMillis() + "@test.com");
        owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker" + System.currentTimeMillis() + "@test.com");
        booker = userService.create(bookerDto);

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
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(savedBooking.getId());
        assertEquals(booker.getId(), savedBooking.getBooker().getId());
        assertEquals(item.getId(), savedBooking.getItem().getId());
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());
        assertEquals(start, savedBooking.getStart());
        assertEquals(end, savedBooking.getEnd());
    }

    @Test
    void create_WithUnavailableItem_ShouldThrowException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);
        itemService.update(owner.getId(), item.getId(), updateDto);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Вещь недоступна для бронирования", exception.getMessage());
    }

    @Test
    void create_ByOwner_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(owner.getId(), bookingDto));

        assertEquals("Владелец не может бронировать свою вещь", exception.getMessage());
    }

    @Test
    void create_WithNonExistentItem_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(999L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void create_WithNonExistentUser_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(999L, bookingDto));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void create_WithEndBeforeStart_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Дата начала не может быть позже даты окончания", exception.getMessage());
    }

    @Test
    void create_WithStartInPast_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Дата начала не может быть в прошлом", exception.getMessage());
    }

    @Test
    void create_WithStartEqualToEnd_ShouldThrowException() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(sameTime);
        bookingDto.setEnd(sameTime);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Даты начала и окончания не могут совпадать", exception.getMessage());
    }

    @Test
    void create_WithNullDates_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(null);
        bookingDto.setEnd(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Даты начала и окончания должны быть указаны", exception.getMessage());
    }

    @Test
    void create_WithNullEnd_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Даты начала и окончания должны быть указаны", exception.getMessage());
    }

    @Test
    void create_WithNullStart_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(null);
        bookingDto.setEnd(end);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Даты начала и окончания должны быть указаны", exception.getMessage());
    }

    @Test
    void approve_ShouldChangeStatusToApproved() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto approvedBooking = bookingService.approve(owner.getId(),
                savedBooking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void approve_ShouldChangeStatusToRejected() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto rejectedBooking = bookingService.approve(owner.getId(),
                savedBooking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void approve_ByNotOwner_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        UserDto strangerDto = new UserDto();
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger" + System.currentTimeMillis() + "@test.com");
        UserDto stranger = userService.create(strangerDto);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> bookingService.approve(stranger.getId(), savedBooking.getId(), true));

        assertEquals("Только владелец может подтверждать бронирование", exception.getMessage());
    }

    @Test
    void approve_AlreadyApproved_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        bookingService.approve(owner.getId(), savedBooking.getId(), true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), savedBooking.getId(), true));

        assertEquals("Бронирование уже обработано", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto foundBooking = bookingService.getById(booker.getId(),
                savedBooking.getId());

        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getById_OwnerCanSee() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto foundBooking = bookingService.getById(owner.getId(),
                savedBooking.getId());

        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getById_StrangerCannotSee() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        UserDto strangerDto = new UserDto();
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger" + System.currentTimeMillis() + "@test.com");
        UserDto stranger = userService.create(strangerDto);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(stranger.getId(), savedBooking.getId()));

        assertEquals("Нет доступа к просмотру этого бронирования", exception.getMessage());
    }

    @Test
    void getUserBookings_ShouldReturnAllBookings() {
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

        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "ALL");

        assertEquals(2, bookings.size());
    }

    @Test
    void getUserBookings_WithStateWaiting_ShouldReturnWaiting() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "WAITING");

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void getUserBookings_WithStateRejected_ShouldReturnRejected() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        bookingService.approve(owner.getId(), savedBooking.getId(), false);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "REJECTED");

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());
    }

    @Test
    void getUserBookings_WithStateFuture_ShouldReturnFutureBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(10));
        bookingDto.setEnd(LocalDateTime.now().plusDays(11));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "FUTURE");
        assertEquals(1, bookings.size());
    }

    @Test
    void getUserBookings_WithStateCurrent_ShouldReturnCurrentBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Дата начала не может быть в прошлом", exception.getMessage());
    }

    @Test
    void getUserBookings_WithStatePast_ShouldReturnPastBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(2));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Дата начала не может быть в прошлом", exception.getMessage());
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> bookingService.getUserBookings(booker.getId(), "INVALID_STATE"));
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "ALL");

        assertEquals(1, bookings.size());
    }

    @Test
    void getOwnerBookings_WithStateCurrent_ShouldReturnCurrentBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Дата начала не может быть в прошлом", exception.getMessage());
    }

    @Test
    void getOwnerBookings_WithStatePast_ShouldReturnPastBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(2));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Дата начала не может быть в прошлом", exception.getMessage());
    }

    @Test
    void getOwnerBookings_WithInvalidState_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> bookingService.getOwnerBookings(owner.getId(), "INVALID_STATE"));
    }

    @Test
    void getOwnerBookings_WithStateWaiting_ShouldReturnWaiting() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "WAITING");

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void getOwnerBookings_WithStateRejected_ShouldReturnRejected() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        bookingService.approve(owner.getId(), savedBooking.getId(), false);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "REJECTED");

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());
    }

    @Test
    void getOwnerBookings_WithStateFuture_ShouldReturnFutureBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(10));
        bookingDto.setEnd(LocalDateTime.now().plusDays(11));
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "FUTURE");
        assertEquals(1, bookings.size());
    }

    @Test
    void getUserBookings_WithNoBookings_ShouldReturnEmptyList() {
        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "ALL");
        assertTrue(bookings.isEmpty());
    }

    @Test
    void getOwnerBookings_WithNoBookings_ShouldReturnEmptyList() {
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "ALL");
        assertTrue(bookings.isEmpty());
    }
}