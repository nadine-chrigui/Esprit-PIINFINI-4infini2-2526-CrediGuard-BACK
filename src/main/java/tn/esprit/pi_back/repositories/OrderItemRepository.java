package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.OrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.enums.OrderStatus;
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    @Query("""
    select oi.product.id, oi.product.name, coalesce(sum(oi.quantity), 0), coalesce(sum(oi.lineTotal), 0)
    from OrderItem oi
    where oi.order.status in :statuses
      and oi.product.active = true
    group by oi.product.id, oi.product.name
    order by sum(oi.lineTotal) desc
""")
    List<Object[]> findTopProductsByRevenue(@Param("statuses") List<OrderStatus> statuses);
    @Query("""
    select oi.product.category.id, oi.product.category.name, coalesce(sum(oi.lineTotal), 0)
    from OrderItem oi
    where oi.order.status in :statuses
      and oi.product.active = true
    group by oi.product.category.id, oi.product.category.name
    order by sum(oi.lineTotal) desc
""")
    List<Object[]> findRevenueByCategory(@Param("statuses") List<OrderStatus> statuses);
}