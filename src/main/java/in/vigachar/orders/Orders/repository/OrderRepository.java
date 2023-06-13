package in.vigachar.orders.Orders.repository;

import in.vigachar.orders.Orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Custom query methods, if needed
}
