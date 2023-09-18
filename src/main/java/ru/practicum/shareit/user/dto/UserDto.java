package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    /*
     * А эти аннотации мешали при обновлении объекта
     * Передавалось только имя и из-за этого бэд реквест падал, так как не было мейла
     * Вот и решил эти закомментировать, оставить только аннотацию мейла,
     * А для остального в сервисах методы с валидацией сделать
     * */
    Long id;
    //        @NotBlank
    String name;
    @Email
//    @NotBlank
    String email;
}