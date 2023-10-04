package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplUnitTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    private ItemRequestService service;

    @BeforeEach
    public void makeItemRequestService() {
        this.service = new ItemRequestServiceImpl(requestRepository, userRepository, itemRepository);
    }
    @Test
    void testAddRequestWrongUser() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Надо вещь такую, чтобы пилить хорошо")
                .build();

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.addRequest(itemRequestDto, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAllWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.findAll(1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAllWithPaginationWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.findAll(1L, 0, 5));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAllWithPaginationWrongPagination() {
        User user = new User(1L, "Stas", "stas@test.com");
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.findAll(1L, -1, 5));

        assertThat(exception.getMessage(), equalTo("Неверно выбрана пагинация"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByIdWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.findById(1L, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByIdItemRequestNotFound() {
        User user = new User(1L, "Stas", "stas@test.com");
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(requestRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.findById(1L, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);
    }
}
