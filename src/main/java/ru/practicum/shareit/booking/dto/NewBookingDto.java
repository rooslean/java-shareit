package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewBookingDto {
    @NotNull
    Long itemId;
    @NotNull
    @Future
    LocalDateTime start;
    @NotNull
    @Future
    LocalDateTime end;
}