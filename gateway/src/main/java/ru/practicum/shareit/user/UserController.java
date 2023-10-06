package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.ExistUser;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient client;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        return client.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        return client.getUserById(userId);
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Validated(NewUser.class) @RequestBody UserDto userDto) {
        return client.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @Validated(ExistUser.class) @RequestBody UserDto userDto) {
        return client.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable Long userId) {
        return client.deleteUserById(userId);
    }
}
