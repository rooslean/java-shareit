package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemMapper {
    public static ItemDto mapItemToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .ownerId(item.getOwner().getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemDto mapToItemDtoWithBookings(Item item, List<Booking> bookings, List<CommentDto> comments) {
        ShortBookingDto lastBooking = null;
        ShortBookingDto nextBooking = null;
        if (bookings.size() > 1) {
            lastBooking = BookingMapper.mapToShortBookingDto(bookings.get(0));
            nextBooking = BookingMapper.mapToShortBookingDto(bookings.get(1));
        } else if (bookings.size() == 1) {
            LocalDateTime now = LocalDateTime.now();
            if (bookings.get(0).getStart().isAfter(now)) {
                nextBooking = BookingMapper.mapToShortBookingDto(bookings.get(0));
            } else {
                lastBooking = BookingMapper.mapToShortBookingDto(bookings.get(0));
            }
        }
        return new ItemDtoWithBookings(item.getId(), item.getOwner().getId(),
                item.getName(), item.getDescription(), item.getAvailable(), lastBooking, nextBooking, comments);
    }

    public static List<ItemDto> mapItemToItemDto(Iterable<Item> items) {
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : items) {
            itemsDto.add(ItemMapper.mapItemToItemDto(item));
        }
        return itemsDto;
    }

    public static Item mapItemDtoToItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .owner(owner)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static void mapItemDtoToItemForUpdate(ItemDto itemDto, Item item, User owner) {
        if (itemDto.getOwnerId() != null) {
            item.setOwner(owner);
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }
}
