package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    @NotBlank(groups = NewUser.class)
    String name;
    @Email(groups = {NewUser.class, ExistUser.class})
    @NotBlank(groups = NewUser.class)
    String email;
}