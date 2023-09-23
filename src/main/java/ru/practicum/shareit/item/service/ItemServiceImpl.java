package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NoRightsForUpdateException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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

    @Override
    public List<ItemDto> getAllItems() {
        return ItemMapper.mapItemToItemDto(itemRepository.findAll());
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new ObjectNotFoundException("Предмет", itemId);
        }
        return ItemMapper.mapItemToItemDto(item);
    }

    @Override
    public List<ItemDto> findItemsByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId)
                .stream()
                .map(ItemMapper::mapItemToItemDto)
                .collect(Collectors.toList());
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
