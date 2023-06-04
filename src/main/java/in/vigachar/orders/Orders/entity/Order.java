package in.vigachar.orders.Orders.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status;

    @Transient
    private Customer customer;
}

