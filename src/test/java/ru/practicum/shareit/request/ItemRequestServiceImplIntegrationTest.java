package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemRequestService requestService;

    @Test
    void testFindAll() {
        UserDto userDtoOne = UserDto.builder()
                .name("Roland")
                .email("roland@test.ru")
                .build();

        UserDto userDtoTwo = UserDto.builder()
                .name("Voland")
                .email("voland@test.ru")
                .build();
        UserDto createdUserOne = userService.createUser(userDtoOne);
        UserDto createdUserTwo = userService.createUser(userDtoTwo);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Мне надо штуку, чтобы пилить можно было")
                .build();
        ItemRequestDto addedItemRequest = requestService.addRequest(itemRequestDto, createdUserOne.getId());

        List<ItemRequestDto> itemRequests = requestService.findAll(createdUserTwo.getId(), 0, 5);

        assertThat(itemRequests, hasSize(1));
        assertThat(itemRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(addedItemRequest.getDescription()))
        )));
    }

    @Test
    void testFindAllWithoutPages() {
        UserDto userDtoOne = UserDto.builder()
                .name("Roland")
                .email("roland@test.ru")
                .build();

        UserDto userDtoTwo = UserDto.builder()
                .name("Voland")
                .email("voland@test.ru")
                .build();
        UserDto createdUserOne = userService.createUser(userDtoOne);
        userService.createUser(userDtoTwo);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Мне надо штуку, чтобы пилить можно было")
                .build();
        ItemRequestDto addedItemRequest = requestService.addRequest(itemRequestDto, createdUserOne.getId());

        List<ItemRequestDto> itemRequests = requestService.findAll(createdUserOne.getId());

        assertThat(itemRequests, hasSize(1));
        assertThat(itemRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(addedItemRequest.getDescription()))
        )));
    }
}
