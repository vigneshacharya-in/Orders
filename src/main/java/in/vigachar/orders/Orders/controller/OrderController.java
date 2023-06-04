package in.vigachar.orders.Orders.controller;

import in.vigachar.orders.Orders.entity.Order;
import in.vigachar.orders.Orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.ConnectException;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public Order getOrderDetails(@PathVariable Long orderId) throws OrderService.OrderNotFoundException, ConnectException {
        return orderService.getOrderById(orderId);
    }
}

