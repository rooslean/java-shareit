package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectAlreadyExistsException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapUserToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new ObjectNotFoundException("Пользователь", userId);
        }
        return UserMapper.mapUserToUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        isUserWithEmailExist(userDto.getEmail());

        User user = UserMapper.mapUserDtoToUser(userDto);
        userDto = UserMapper.mapUserToUserDto(userRepository.create(user));
        log.info("Пользователь с идентификатором {} и почтой {} был создан", user.getId(), user.getEmail());
        return userDto;
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        isValidForUpdate(userDto);
        isUserWithEmailExist(userDto.getEmail(), userId);
        userDto.setId(userId);
        User user = userRepository.getUserById(userDto.getId());
        if (userDto.getId() == null || user == null) {
            throw new ObjectNotFoundException("Пользователь", userDto.getId());
        }
        UserMapper.mapUserDtoToUserForUpdate(userDto, user);
        userDto = UserMapper.mapUserToUserDto(userRepository.save(user));
        log.info("Данные пользователя с идентификатором {} были обновлены", user.getId());
        return userDto;
    }

    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteUserById(userId);
    }
    

    private void isValidForUpdate(UserDto userDto) {
        if (userDto.getEmail() != null
                && userDto.getEmail().isEmpty()
                || userDto.getName() != null
                && userDto.getName().isEmpty()) {
            throw new ObjectNotValidException();
        }
    }

    private void isUserWithEmailExist(String email) {
        isUserWithEmailExist(email, -1L);
    }

    private void isUserWithEmailExist(String email, Long userId) {
        if (email != null) {
            User user = userRepository.getUserByEmail(email);
            if (user != null && !Objects.equals(user.getId(), userId)) {
                throw new ObjectAlreadyExistsException("email", user.getEmail());
            }
        }
    }
}
