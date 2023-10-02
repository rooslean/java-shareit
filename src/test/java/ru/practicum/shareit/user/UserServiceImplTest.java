package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    UserRepository mockUserRepository;

    @Test
    void testUpdateUserWithValidData() {
        UserService userService = new UserServiceImpl(mockUserRepository);
        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "Svatoslav", "slava@test.com")));
        Mockito
                .when(mockUserRepository.save(new User(1L, "Slava", "slava@test.com")))
                .thenReturn(new User(1L, "Slava", "slava@test.com"));

        UserDto userDto = UserDto.builder()
                .name("Slava")
                .email("slava@test.com")
                .build();

        UserDto updatedUserDto = userService.updateUser(1L, userDto);

        assertThat(updatedUserDto.getId(), equalTo(1L));
        assertThat(updatedUserDto.getName(), equalTo("Slava"));
        assertThat(updatedUserDto.getEmail(), equalTo("slava@test.com"));
        Mockito.verify(mockUserRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(mockUserRepository, Mockito.times(1))
                .save(new User(1L, "Slava", "slava@test.com"));
    }

    @Test
    void testUpdateUserWithInvalidName() {
        UserService userService = new UserServiceImpl(mockUserRepository);

        UserDto userDto = UserDto.builder()
                .name("")
                .email("slava@test.com")
                .build();

        final ObjectNotValidException exception = Assertions.assertThrows(ObjectNotValidException.class,
                () -> userService.updateUser(1L, userDto));

        assertThat(exception.getMessage(), equalTo("Введены невалидные данные"));
        Mockito.verifyNoInteractions(mockUserRepository);
    }

    @Test
    void testUpdateUserWithInvalidEmail() {
        UserService userService = new UserServiceImpl(mockUserRepository);

        UserDto userDto = UserDto.builder()
                .name("Slava")
                .email("")
                .build();

        final ObjectNotValidException exception = Assertions.assertThrows(ObjectNotValidException.class,
                () -> userService.updateUser(1L, userDto));

        assertThat(exception.getMessage(), equalTo("Введены невалидные данные"));
        Mockito.verifyNoInteractions(mockUserRepository);
    }

    @Test
    void testGetUserByIdIfUserExist() {
        UserService userService = new UserServiceImpl(mockUserRepository);
        Mockito
                .when(mockUserRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "Svatoslav", "slava@test.com")));

        UserDto userDto = userService.getUserById(1L);

        assertThat(userDto.getId(), equalTo(1L));
        Mockito.verify(mockUserRepository, Mockito.times(1))
                .findById(1L);
    }

    @Test
    void testGetUserByIdIfUserDoesNotExist() {
        UserService userService = new UserServiceImpl(mockUserRepository);
        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> userService.getUserById(1L));
        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
        Mockito.verify(mockUserRepository, Mockito.times(1))
                .findById(Mockito.anyLong());

    }

}
