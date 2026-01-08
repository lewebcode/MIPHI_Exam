package ru.mifi.practice.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mifi.practice.hotel.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
