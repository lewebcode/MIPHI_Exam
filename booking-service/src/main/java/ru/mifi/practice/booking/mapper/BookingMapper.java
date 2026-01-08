package ru.mifi.practice.booking.mapper;

import org.mapstruct.Mapper;
import ru.mifi.practice.booking.dto.BookingResponse;
import ru.mifi.practice.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingResponse toResponse(Booking booking);
}
