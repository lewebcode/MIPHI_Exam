package ru.mifi.practice.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.mifi.practice.booking.model.Booking;
import ru.mifi.practice.booking.model.User;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    Page<Booking> findByUser(User user, Pageable pageable);

    Optional<Booking> findByRequestId(String requestId);
}
