package ru.practicum.shareit.item.comments;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    String authorName;
    @NotBlank
    String text;
    LocalDateTime created;
}
