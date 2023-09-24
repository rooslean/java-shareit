package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id")
    User booker;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    Item item;
    @Column(name = "start_date")
    LocalDateTime start;
    @Column(name = "end_date")
    LocalDateTime end;
    @Enumerated(EnumType.STRING)
    BookingStatus status;
}
