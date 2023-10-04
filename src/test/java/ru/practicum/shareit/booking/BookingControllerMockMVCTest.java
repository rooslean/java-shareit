package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerMockMVCTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().plusDays(2).format(formatter));
    private final LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().plusDays(3).format(formatter));

    private final UserDto booker =
            new UserDto(1L, "StarLord", "star@lord.com");
    private final ItemDto itemDto = new ItemDto(1L, 2L, null,
            "Бластер", "Делает пиу пиу", true);
    private final NewBookingDto newBookingDto = NewBookingDto.builder()
            .itemId(1L)
            .start(start)
            .end(end)
            .build();
    private final BookingDto bookingDto = new BookingDto(1L, booker, itemDto, start,
            end, BookingStatus.WAITING);

    private final BookingDto approvedBookingDto = new BookingDto(1L, booker, itemDto, start,
            end, BookingStatus.APPROVED);

    @Test
    void testAddBooking() throws Exception {
        when(bookingService.add(any(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(newBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(formatter)), String.class))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(formatter)), String.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name()), String.class));
    }

    @Test
    void testApproveBooking() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(approvedBookingDto);

        mvc.perform(patch("/bookings/1?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(approvedBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(approvedBookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(approvedBookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.start", is(approvedBookingDto.getStart().format(formatter)), String.class))
                .andExpect(jsonPath("$.end", is(approvedBookingDto.getEnd().format(formatter)), String.class))
                .andExpect(jsonPath("$.status", is(approvedBookingDto.getStatus().name()), String.class));
    }

        @Test
        void testGetBookingInfo() throws Exception {
                when(bookingService.findByOwnerIdOrBookerId(anyLong(), anyLong()))
                        .thenReturn(bookingDto);

                mvc.perform(get("/bookings/1")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", "1")
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                        .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                        .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                        .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(formatter)), String.class))
                        .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(formatter)), String.class))
                        .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name()), String.class));
        }

    @Test
    void testGetBookerBookings() throws Exception {
        when(bookingService.findAllByBookerIdAndBookingState(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().format(formatter)), String.class))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().format(formatter)), String.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().name()), String.class));
    }

    @Test
    void testGetOwnerBookings() throws Exception {
        when(bookingService.findAllByOwnerIdAndBookingState(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().format(formatter)), String.class))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().format(formatter)), String.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().name()), String.class));
    }
}
