package in.vigachar.orders.Orders.service;

import in.vigachar.orders.Orders.entity.Customer;
import in.vigachar.orders.Orders.entity.Order;
import in.vigachar.orders.Orders.repository.OrderRepository;
import in.vigachar.orders.Orders.util.RestClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.ConnectException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RestClient restClient;

    @Autowired
    public OrderService(OrderRepository orderRepository, CircuitBreakerRegistry circuitBreakerRegistry, RestClient restClient) {
        this.orderRepository = orderRepository;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "customers", fallbackMethod = "fallbackCustomer")
    public Order getOrderById(Long orderId) throws OrderNotFoundException, ConnectException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        Customer customer = restClient.get(order.getCustomerId());
        order.setCustomer(customer);

        printCircuitBreakerMetrics("customers");

        return order;
    }

    public Order fallbackCustomer(Long orderId, ConnectException ex) throws OrderNotFoundException {
        // Fallback logic when the circuit is open or an error occurs
        // You can return a default customer or an empty list of orders
        System.out.println("Exception: " + ex.getClass().toString() + ", Message: " + ex.getMessage());

        printCircuitBreakerMetrics("customers");

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    public void printCircuitBreakerMetrics(String circuitBreakerName) {
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        System.out.println("Circuit Breaker state: " + circuitBreaker.getState());

        io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        System.out.println("Circuit Breaker Metrics:");
        System.out.println("  - Successful calls: " + metrics.getNumberOfSuccessfulCalls());
        System.out.println("  - Failed calls: " + metrics.getNumberOfFailedCalls());
        System.out.println("  - Slow calls: " + metrics.getNumberOfSlowCalls());
        System.out.println("  - Not permitted calls: " + metrics.getNumberOfNotPermittedCalls());
    }

    public class OrderNotFoundException extends Exception {
        public OrderNotFoundException(String s) {
            super(s);
        }
    }
}

