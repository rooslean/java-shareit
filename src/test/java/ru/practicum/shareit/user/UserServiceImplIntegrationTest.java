package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
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
public class UserServiceImplIntegrationTest {
    private final EntityManager em;
    private final UserService service;

    @Test
    void testGetAllUsers() {
        UserDto userDtoOne = UserDto.builder()
                .name("Roland")
                .email("roland@test.ru")
                .build();
        UserDto userDtoTwo = UserDto.builder()
                .name("Voland")
                .email("voland@test.ru")
                .build();
        UserDto userDtoThree = UserDto.builder()
                .name("Poland")
                .email("poland@test.ru")
                .build();
        List<UserDto> sourceUsers = List.of(
                userDtoOne,
                userDtoTwo,
                userDtoThree
        );

        for (UserDto userDto : sourceUsers) {
            User user = UserMapper.mapUserDtoToUser(userDto);
            em.persist(user);
        }
        em.flush();

        List<UserDto> targetUsers = service.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (UserDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }

    }

}
