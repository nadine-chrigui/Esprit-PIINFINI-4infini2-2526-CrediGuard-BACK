package tn.esprit.pi_back.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    long countByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Order> findByStatusAndCreatedAtBetween(
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );


    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.status in :statuses
    """)
    Double sumRevenueByStatuses(@Param("statuses") List<OrderStatus> statuses);

    @Query("""
        select count(o)
        from Order o
        where o.status in :statuses
    """)
    Long countByStatuses(@Param("statuses") List<OrderStatus> statuses);

    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.status in :statuses
          and o.createdAt between :startDate and :endDate
    """)
    Double sumRevenueByStatusesAndDateRange(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("""
    select year(o.createdAt), month(o.createdAt), coalesce(sum(o.totalAmount), 0)
    from Order o
    where o.status in :statuses
    group by year(o.createdAt), month(o.createdAt)
    order by year(o.createdAt), month(o.createdAt)
""")
    List<Object[]> sumRevenueGroupedByMonth(@Param("statuses") List<OrderStatus> statuses);
}
