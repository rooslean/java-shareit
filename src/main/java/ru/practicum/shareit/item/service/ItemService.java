package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> findItemsByOwnerId(Long ownerId);

    List<ItemDto> searchItemsByPhrase(String searchPhrase);

    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto);
}
