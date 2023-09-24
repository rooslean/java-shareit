package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.ShortBookingDto;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoWithBookings extends ItemDto {
    ShortBookingDto lastBooking;
    ShortBookingDto nextBooking;

    public ItemDtoWithBookings(Long id, Long ownerId, String name, String description, Boolean available,
                               ShortBookingDto lastBooking, ShortBookingDto nextBooking) {
        super(id, ownerId, name, description, available);
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
    }
}
