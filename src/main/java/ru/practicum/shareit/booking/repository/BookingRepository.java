package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Все бронирования пользователя
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    // Все бронирования вещей владельца
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    // Поиск по статусу для пользователя
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Поиск по статусу для владельца
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    // Текущие бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND :now BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findCurrentByBooker(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Прошедшие бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByBooker(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Будущие бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByBooker(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Текущие бронирования владельца
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId " +
            "AND :now BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findCurrentByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Прошедшие бронирования владельца
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId " +
            "AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Будущие бронирования владельца
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId " +
            "AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime end);

    List<Booking> findByItemIdOrderByStartDesc(Long itemId);
}