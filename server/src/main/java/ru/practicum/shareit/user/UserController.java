package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.ExistUser;
import ru.practicum.shareit.user.dto.NewUser;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        return userService.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
