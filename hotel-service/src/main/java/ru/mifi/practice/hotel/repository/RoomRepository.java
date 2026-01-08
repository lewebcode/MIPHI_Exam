package ru.mifi.practice.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mifi.practice.hotel.model.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAvailableTrue();

    @Query("select r from Room r where r.available = true order by r.timesBooked asc, r.id asc")
    List<Room> findRecommended();

    List<Room> findAllByAvailable(Boolean available, org.springframework.data.domain.Sort sort);
}
