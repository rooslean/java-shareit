package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoRightsForUpdateException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.comments.Comment;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.comments.CommentMapper;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new ObjectNotFoundException("Предмет", itemId);
        }
        ItemDto itemDto;
        List<Booking> bookings;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            bookings = bookingRepository.findLastAndNearFutureBookingsByItemId(itemId, LocalDateTime.now());
        } else {
            bookings = new ArrayList<>();
        }
        List<CommentDto> comments = CommentMapper.mapToCommentDto(commentRepository.findByItemIdOrderByCreated(itemId));
        itemDto = ItemMapper.mapToItemDtoWithBookings(item, bookings, comments);

        return itemDto;
    }

    @Override
    public List<ItemDto> findItemsByOwnerId(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        List<ItemDto> itemsWithBookings = new ArrayList<>();
        for (Item item : items) {
            List<Booking> bookings = bookingRepository.findLastAndNearFutureBookingsByItemId(item.getId(), LocalDateTime.now());
            List<CommentDto> comments = CommentMapper.mapToCommentDto(commentRepository.findByItemIdOrderByCreated(item.getId()));
            ItemDto itemDtoWithBookings = ItemMapper.mapToItemDtoWithBookings(item, bookings, comments);
            itemsWithBookings.add(itemDtoWithBookings);
        }
        return itemsWithBookings;
    }

    @Override
    public List<ItemDto> searchItemsByPhrase(String searchPhrase) {
        if (searchPhrase == null || searchPhrase.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.findByNameOrDescription(searchPhrase)
                .stream()
                .map(ItemMapper::mapItemToItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        isValidForCreation(itemDto);
        User owner = userRepository.getUserById(ownerId);
        if (owner == null) {
            throw new ObjectNotFoundException("Пользователь", ownerId);
        }
        Item item = ItemMapper.mapItemDtoToItem(itemDto, owner);
        itemDto = ItemMapper.mapItemToItemDto(itemRepository.save(item));
        log.info("Предмет с идентификатором {} был добавлен для пользователя {} был создан", item.getId(), ownerId);
        return itemDto;
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto) {
        isValidForUpdate(itemDto);
        User owner = userRepository.getUserById(ownerId);
        if (owner == null) {
            throw new ObjectNotFoundException("Пользователь", ownerId);
        }
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new ObjectNotFoundException("Предмет", itemId);
        }
        if (!Objects.equals(owner.getId(), item.getOwner().getId())) {
            throw new NoRightsForUpdateException();
        }
        User newOwner = Objects.equals(ownerId, itemDto.getOwnerId()) || itemDto.getOwnerId() == null
                ? owner : userRepository.getUserById(itemDto.getOwnerId());
        if (newOwner == null) {
            throw new ObjectNotFoundException("Пользователь", ownerId);
        }
        ItemMapper.mapItemDtoToItemForUpdate(itemDto, item, newOwner);
        itemDto = ItemMapper.mapItemToItemDto(itemRepository.save(item));
        log.info("Данные предмета с идентификатором {} были обновлены", item.getId());
        return itemDto;
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new ObjectNotFoundException();
        }
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new ObjectNotFoundException();
        }
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusNotAndEndBefore(
                itemId, userId, BookingStatus.REJECTED, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new BadRequestException("Нельзя оставить комментарий без бронирования");
        }
        Comment comment = commentRepository.save(CommentMapper.mapToComment(commentDto, item, user));
        return CommentMapper.mapToCommentDto(comment);
    }

    private void isValidForCreation(ItemDto itemDto) {
        if (itemDto.getName() == null
                || itemDto.getName().isEmpty()
                || itemDto.getDescription() == null
                || itemDto.getDescription().isEmpty()
                || itemDto.getAvailable() == null) {
            throw new ObjectNotValidException();
        }
    }

    private void isValidForUpdate(ItemDto itemDto) {
        if (itemDto.getName() != null
                && itemDto.getName().isEmpty()
                || itemDto.getDescription() != null
                && itemDto.getDescription().isEmpty()) {
            throw new ObjectNotValidException();
        }
    }
}
