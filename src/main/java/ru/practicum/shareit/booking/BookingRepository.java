package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
            " where b.item.id = ?1" +
            " and b.status <> 'REJECTED'" +
            " and(b.start = " +
            "           (select max(b2.start) " +
            "                   from Booking b2 " +
            "                   where b2.item.id = ?1 and b2.start <= ?2)" +
            " or b.start = " +
            "           (select min(b3.start) " +
            "           from Booking b3 " +
            "           where b3.item.id = ?1 and b3.start > ?2))")
    List<Booking> findLastAndNearFutureBookingsByItemId(long itemId, LocalDateTime now, Sort sort);

    // Поиск всех бронирований заказчика
    Iterable<Booking> findAllByBookerIdOrderByStartDesc(long bookerId); //ALL

    @Query("select b " +
            "from Booking b " +
            "join b.booker as br " +
            "where br.id = ?1 " +
            "and b.start <= ?2" +
            "and b.end >= ?2 ")
    Iterable<Booking> findAllBookerCurrentBookings(long bookerId, LocalDateTime now, Sort sort); //Current

    Iterable<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now); //Past

    Iterable<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now); //Future

    Iterable<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status); //Status

    //Конец блока

    // Поиск бронирований для владельца
    Iterable<Booking> findAllByItemOwnerIdOrderByStartDesc(long ownerId); //ALL

    @Query("select b " +
            "from Booking b " +
            "join b.item as i " +
            "where i.owner.id = ?1 " +
            "and b.start <= ?2" +
            "and b.end >= ?2 ")
    Iterable<Booking> findAllOwnerCurrentBookings(long bookerId, LocalDateTime now, Sort sort); //Current

    Iterable<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now); //Past

    Iterable<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now); //Future

    Iterable<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status); //Future
    //Конец блока
}
