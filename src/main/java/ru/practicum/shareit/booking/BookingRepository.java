package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends CrudRepository<Booking, Long> {
    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "join b.booker as br " +
            "where b.id = ?1 and " +
            "(br.id = ?2 or i.owner.id = ?2)")
    Booking findByOwnerIdOrBookerId(long bookingId, long userId);

    List<Booking> findByItemIdAndEndAfter(long itemId, LocalDateTime now);

    List<Booking> findByItemIdAndBookerIdAndStatusNotAndEndBefore(long itemId, long bookerId, BookingStatus status, LocalDateTime now);

    //Находим последнее и ближайшее будущее бронирования
    @Query("select b " +
            "from Booking b" +
            " where b.item.id in ?1" +
            " and b.status <> 'REJECTED'" +
            " and(b.start = " +
            "           (select max(b2.start) " +
            "                   from Booking b2 " +
            "                   where b2.item.id in ?1 and b2.start <= ?2)" +
            " or b.start = " +
            "           (select min(b3.start) " +
            "           from Booking b3 " +
            "           where b3.item.id in ?1 and b3.start > ?2))")
    List<Booking> findLastAndNearFutureBookingsByItemIn(Collection<Long> itemIds, LocalDateTime now, Sort sort);

    // Поиск всех бронирований заказчика
    Page<Booking> findAllByBookerIdOrderByStartDesc(long bookerId, Pageable page); //ALL

    @Query("select b " +
            "from Booking b " +
            "join b.booker as br " +
            "where br.id = ?1 " +
            "and b.start <= ?2" +
            "and b.end >= ?2 ")
    Page<Booking> findAllBookerCurrentBookings(long bookerId, LocalDateTime now, Pageable page); //Current

    Page<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now, Pageable page); //Past

    Page<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now, Pageable page); //Future

    Page<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status, Pageable page); //Status

    //Конец блока

    // Поиск бронирований для владельца
    Page<Booking> findAllByItemOwnerIdOrderByStartDesc(long ownerId, Pageable page); //ALL

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "where i.owner.id = ?1 " +
            "and b.start <= ?2" +
            "and b.end >= ?2 ")
    Page<Booking> findAllOwnerCurrentBookings(long bookerId, LocalDateTime now, Pageable page); //Current

    Page<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now, Pageable page); //Past

    Page<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now, Pageable page); //Future

    Page<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status, Pageable page); //Future
    //Конец блока
}
