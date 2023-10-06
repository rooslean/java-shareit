package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
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
public class ItemServiceImplIntegrationTest {
    private final ItemService service;
    private final UserService userService;

    @Test
    void testFindItemsByOwnerId() {
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

        ItemDto itemDtoOne = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();
        ItemDto itemDtoTwo = ItemDto.builder()
                .name("Дрель")
                .description("Сверлит")
                .available(true)
                .build();

        ItemDto addedItemOne = service.addItem(createdUserOne.getId(), itemDtoOne);
        service.addItem(createdUserTwo.getId(), itemDtoTwo);

        List<ItemDto> items = service.findItemsByOwnerId(createdUserOne.getId(), 0, 5);

        assertThat(items, hasSize(1));
        assertThat(items, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("ownerId", equalTo(addedItemOne.getOwnerId())),
                hasProperty("name", equalTo(addedItemOne.getName())),
                hasProperty("description", equalTo(addedItemOne.getDescription())),
                hasProperty("available", equalTo(addedItemOne.getAvailable()))
        )));
    }
}
