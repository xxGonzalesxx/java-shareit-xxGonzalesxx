package ru.practicum.shareit.request.repository;

import ru.practicum.shareit.request.model.ItemRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    // Все запросы пользователя (свои)
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    // Все запросы других пользователей (чужие)
    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId);
}