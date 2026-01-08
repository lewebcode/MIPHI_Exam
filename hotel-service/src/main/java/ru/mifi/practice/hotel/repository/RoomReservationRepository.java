package ru.mifi.practice.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mifi.practice.hotel.model.RoomReservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomReservationRepository extends JpaRepository<RoomReservation, Long> {

    Optional<RoomReservation> findByRequestId(String requestId);

    void deleteByRequestId(String requestId);

    @Query("select rr from RoomReservation rr where rr.room.id = :roomId and rr.endDate >= :start and rr.startDate <= :end")
    List<RoomReservation> findOverlaps(Long roomId, LocalDate start, LocalDate end);
}
