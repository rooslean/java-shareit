package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @Test
    void testAddBookingWithInvalidData() {
        BookingService service = getBookingService();
        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        final ObjectNotValidException exception = Assertions.assertThrows(ObjectNotValidException.class,
                () -> service.add(newBookingDto, 1L));

        assertThat(exception.getMessage(), equalTo("Дата начала не может быть равна или позже даты конца"));

        Mockito.verifyNoInteractions(userRepository);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testAddBookingUserNotFound() {
        BookingService service = getBookingService();
        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.add(newBookingDto, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testAddBookingItemNotFound() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(null);

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.add(newBookingDto, 2L));

        assertThat(exception.getMessage(), equalTo("Объект c id - 1 не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testAddBookingOwnerCantBookOwnItem() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");
        User owner = new User(2L, "Stan", "stan@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", true);

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.add(newBookingDto, 2L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testAddBookingItemNotAvailable() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");
        User owner = new User(1L, "Mike", "mike@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", false);

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.add(newBookingDto, 2L));

        assertThat(exception.getMessage(), equalTo("Предмет недоступен для бронирования"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testApproveBookingNotFound() {
        BookingService service = getBookingService();

        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(null);

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.approveBooking(1L, true, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                        .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Бронирование не найдено"));

    }

    @Test
    void testApproveBookingOwnerEqualBooker() {
        BookingService service = getBookingService();
        User booker = new User(1L, "Mike", "mike@test.ru");
        User owner = new User(1L, "Mike", "mike@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(booking);

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.approveBooking(1L, true, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Объект c id - 1 не найден"));

    }

    @Test
    void testApproveBookingStatusNotWaiting() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");
        User owner = new User(1L, "Mike", "mike@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(booking);

        final ObjectNotValidException exception = Assertions.assertThrows(ObjectNotValidException.class,
                () -> service.approveBooking(1L, true, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Невозможно сменить статус"));

    }
    ///
    @Test
    void testFindAllByBookerIdAndBookingStateUserNotFound() {
        BookingService service = getBookingService();
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.findAllByBookerIdAndBookingState(1L, BookingState.ALL,-1, 0));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
    }
    @Test
    void testFindAllByBookerIdAndBookingStateWrongPagination() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.findAllByBookerIdAndBookingState(2L, BookingState.ALL,-1, 0));

        assertThat(exception.getMessage(), equalTo("Неверно выбрана пагинация"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateAll() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(bookingRepository.findAllByBookerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByBookerIdAndBookingState(2L, BookingState.ALL,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateCurrent() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(bookingRepository.findAllBookerCurrentBookings(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByBookerIdAndBookingState(2L, BookingState.CURRENT,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllBookerCurrentBookings(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStatePast() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByBookerIdAndBookingState(2L, BookingState.PAST,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateFuture() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByBookerIdAndBookingState(2L, BookingState.FUTURE,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateWaiting() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(2L,
                        BookingStatus.WAITING, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByBookerIdAndBookingState(2L, BookingState.WAITING,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(2L,
                BookingStatus.WAITING, PageRequest.of(0, 5));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateRejected() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");
        User owner = new User(1L, "Mike", "mike@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(2L,
                        BookingStatus.REJECTED, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        service.findAllByBookerIdAndBookingState(2L, BookingState.REJECTED,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(2L,
                        BookingStatus.REJECTED, PageRequest.of(0, 5));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }
///

    @Test
    void testFindAllByOwnerIdAndBookingStateUserNotFound() {
        BookingService service = getBookingService();
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.findAllByOwnerIdAndBookingState(1L, BookingState.ALL,-1, 0));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
    }
    @Test
    void testFindAllByOwnerIdAndBookingStateWrongPagination() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Mike", "mike@test.ru");

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.findAllByOwnerIdAndBookingState(1L, BookingState.ALL,-1, 0));

        assertThat(exception.getMessage(), equalTo("Неверно выбрана пагинация"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateAll() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Mike", "mike@test.ru");

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.ALL,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateCurrent() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Mike", "mike@test.ru");

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllOwnerCurrentBookings(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.CURRENT,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllOwnerCurrentBookings(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStatePast() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Mike", "mike@test.ru");

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.PAST,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateFuture() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Mike", "mike@test.ru");

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.FUTURE,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateWaiting() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Mike", "mike@test.ru");
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(1L,
                        BookingStatus.WAITING, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.WAITING,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(1L,
                        BookingStatus.WAITING, PageRequest.of(0, 5));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateRejected() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Stan", "stan@test.ru");
        User owner = new User(1L, "Mike", "mike@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(1L,
                        BookingStatus.REJECTED, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.REJECTED,0, 5);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(1L,
                        BookingStatus.REJECTED, PageRequest.of(0, 5));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }
    private BookingService getBookingService() {
        return new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }
}
