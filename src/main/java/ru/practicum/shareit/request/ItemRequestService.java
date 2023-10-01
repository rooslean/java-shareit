package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestDto itemRequestDto, Long userId);
    List<ItemRequestDto> findAll(Long userId);
    List<ItemRequestDto> findAll(Long userId, int from, int size);
    ItemRequestDto findById(Long userId, Long requestId);
}
