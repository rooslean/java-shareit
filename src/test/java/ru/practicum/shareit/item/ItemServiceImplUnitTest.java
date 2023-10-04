package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoRightsForUpdateException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.item.comments.Comment;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;

    @Test
    void testGetItemByIdOkByUser() {
        ItemService service = makeItemService();

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Steve", "steve@test.com"),
                        null, "Пила", "Пилит", true));
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(new ArrayList<>());

        ItemDto itemDto = service.getItemById(1L, 2L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Пила"));
        assertThat(itemDto.getDescription(), equalTo("Пилит"));
        assertThat(itemDto.getAvailable(), equalTo(true));

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdOkByOwner() {
        ItemService service = makeItemService();

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Steve", "steve@test.com"),
                        null, "Пила", "Пилит", true));
        Mockito
                .when(bookingRepository.findLastAndNearFutureBookingsByItemIn(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class), Mockito.any(Sort.class)))
                .thenReturn(new ArrayList<>());
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(new ArrayList<>());

        ItemDtoWithBookings itemDto = (ItemDtoWithBookings) service.getItemById(1L, 1L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Пила"));
        assertThat(itemDto.getDescription(), equalTo("Пилит"));
        assertThat(itemDto.getAvailable(), equalTo(true));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findLastAndNearFutureBookingsByItemIn(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class), Mockito.any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdWithWrongItemId() {
        ItemService service = makeItemService();

        Mockito
                .when(itemRepository.getItemById(Mockito.anyLong()))
                .thenReturn(null);


        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.getItemById(99L, 2L));

        assertThat(exception.getMessage(), equalTo("Предмет c id - 99 не найден"));
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(99L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
    }

    @Test
    void testAddItemOk() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");
        Item newItem = new Item(null, owner, null, "Пила", "Пилит", true);
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.save(newItem))
                .thenReturn(new Item(1L, owner, null, "Пила", "Пилит", true));

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();

        ItemDto itemDtoAdd = service.addItem(1L, itemDto);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(newItem);

        assertThat(itemDtoAdd.getId(), equalTo(1L));
        assertThat(itemDtoAdd.getAvailable(), equalTo(true));
        assertThat(itemDtoAdd.getOwnerId(), equalTo(1L));
    }

    @Test
    void testAddItemInvalidData() {
        ItemService service = makeItemService();
        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .build();

        final ObjectNotValidException exception = Assertions.assertThrows(ObjectNotValidException.class,
                () -> service.addItem(1L, itemDto));

        Mockito.verifyNoInteractions(userRepository);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Введены невалидные данные"));
    }

    @Test
    void testAddItemUserNotFound() {
        ItemService service = makeItemService();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.addItem(1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
    }

    @Test
    void testAddItemRequestNotFound() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .requestId(1L)
                .build();

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.addItem(1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Объект не найден"));
    }

    @Test
    void testUpdateItemOwnerNotFound() {
        ItemService service = makeItemService();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.updateItem(1L, 1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
    }

    @Test
    void testUpdateItemNotFound() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(null);

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.updateItem(1L, 1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Предмет c id - 1 не найден"));
    }

    @Test
    void testUpdateItemNoRightsForUpdate() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");
        User user = new User(2L, "Peter", "peter@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();

        final NoRightsForUpdateException exception = Assertions.assertThrows(NoRightsForUpdateException.class,
                () -> service.updateItem(1L, 2L, itemDto));


        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("У вас нет прав изменять этот объект"));
    }

    @Test
    void testUpdateItemNewOwnerNotFound() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .ownerId(2L)
                .build();

        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.updateItem(1L, 1L, itemDto));


        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
    }

    @Test
    void testUpdateItemOk() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        Item updatedItem = new Item(1L, owner, null, "Пила", "Пилит просто супер", true);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(itemRepository.save(updatedItem))
                .thenReturn(updatedItem);
        ItemDto itemDto = ItemDto.builder()
                .description("Пилит просто супер")
                .build();


        ItemDto updatedItemDto = service.updateItem(1L, 1L, itemDto);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(updatedItem);

        assertThat(updatedItemDto.getDescription(), equalTo("Пилит просто супер"));

    }

    @Test
    void findItemsByOwnerIdWrongPagination() {
        ItemService service = makeItemService();

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.findItemsByOwnerId(1L, -1, 0));

        assertThat(exception.getMessage(), equalTo("Неверно выбрана пагинация"));

        Mockito.verifyNoInteractions(itemRepository);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
    }

    @Test
    void searchItemsByPhraseWrongPagination() {
        ItemService service = makeItemService();

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.searchItemsByPhrase("Пилит", -1, 0));

        assertThat(exception.getMessage(), equalTo("Неверно выбрана пагинация"));

        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void searchItemsByPhraseEmptyPhrase() {
        ItemService service = makeItemService();


        List<ItemDto> items = service.searchItemsByPhrase("", 0, 1);
        assertThat(items, hasSize(0));

        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void searchItemsByPhraseOk() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Ken", "ken@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        Mockito.when(itemRepository.findByNameOrDescription(anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        List<ItemDto> items = service.searchItemsByPhrase("Пилит", 0, 1);
        assertThat(items, hasSize(1));
        assertThat(items, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("ownerId", equalTo(item.getOwner().getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable()))
        )));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findByNameOrDescription(anyString(), any(PageRequest.class));
    }

    @Test
    void addCommentItemNotFound() {
        ItemService service = makeItemService();

        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(null);

        CommentDto commentDto = CommentDto.builder()
                .text("И правда отлично пилит")
                .build();
        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.addComment(1L, 1L, commentDto));

        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
        Mockito.verifyNoInteractions(userRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Объект не найден"));
    }

    @Test
    void addCommentUserNotFound() {
        ItemService service = makeItemService();

        User owner = new User(1L, "Ken", "ken@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(userRepository.findById(3L))
                .thenReturn(Optional.empty());
        CommentDto commentDto = CommentDto.builder()
                .text("И правда отлично пилит")
                .build();
        final ObjectNotFoundException exception = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> service.addComment(1L, 3L, commentDto));

        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(3L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Объект не найден"));
    }

    @Test
    void addCommentBookingNotFound() {
        ItemService service = makeItemService();

        User owner = new User(1L, "Ken", "ken@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        User commentator = new User(2L, "Джон Крамер", "john@saw.ru");

        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(commentator));
        Mockito.when(bookingRepository.findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        CommentDto commentDto = CommentDto.builder()
                .text("И правда отлично пилит")
                .build();

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.addComment(1L, 2L, commentDto));

        Mockito.verifyNoInteractions(commentRepository);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Нельзя оставить комментарий без бронирования"));
    }

    @Test
    void addCommentBookingOk() {
        ItemService service = makeItemService();

        User owner = new User(1L, "Ken", "ken@test.ru");
        User booker = new User(2L, "Barbie", "barbie@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        User commentator = new User(2L, "Джон Крамер", "john@saw.ru");
        Booking booking = new Booking(1L, booker, item, LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3), BookingStatus.APPROVED);
        Comment comment = new Comment(1L, item, booker, "Супер", LocalDateTime.now());

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(commentator));
        Mockito
                .when(bookingRepository.findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito
                .when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        CommentDto commentDto = CommentDto.builder()
                .text("И правда отлично пилит")
                .build();


        service.addComment(1L, 2L, commentDto);
        Mockito.verify(commentRepository, Mockito.times(1))
                .save(any(Comment.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    private ItemService makeItemService() {
        return new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, requestRepository);
    }
}
