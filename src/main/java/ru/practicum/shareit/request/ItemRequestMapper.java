package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest mapToItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(user)
                .created(LocalDateTime.now())
                .build();

    }

    public static List<ItemRequestDto> mapToItemRequestDto(List<ItemRequest> itemRequests, Map<Long, List<Item>> items) {
        return itemRequests.stream()
                .map(req ->
                        ItemRequestMapper.mapToItemRequestDto(req, items.getOrDefault(req.getId(), new ArrayList<>())))
                .collect(Collectors.toList());

    }

    public static ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest) {
        return mapToItemRequestDto(itemRequest, new ArrayList<>());
    }

    public static ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestDtoWithItems itemRequestDto = new ItemRequestDtoWithItems();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setItems(ItemMapper.mapToItemDtoRequestInfo(items));
        itemRequestDto.setCreated(itemRequest.getCreated());
        return itemRequestDto;
    }
}
