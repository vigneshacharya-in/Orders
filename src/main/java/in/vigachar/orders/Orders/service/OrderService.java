package in.vigachar.orders.Orders.service;

import in.vigachar.orders.Orders.entity.Customer;
import in.vigachar.orders.Orders.entity.Order;
import in.vigachar.orders.Orders.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final WebClient webClient;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker;

    @Autowired
    public OrderService(OrderRepository orderRepository, WebClient webClient, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.orderRepository = orderRepository;
        this.webClient = webClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("customers");
    }

    @CircuitBreaker(name = "customers", fallbackMethod = "fallbackCustomer")
    public Order getOrderById(Long orderId) throws OrderNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        Customer customer = webClient.method(HttpMethod.GET)
                .uri("http://localhost:8081/api/v1/customers/{customerId}", order.getCustomerId())
                .retrieve()
                .bodyToMono(Customer.class)
                .block();
        order.setCustomer(customer);

        // Get the current state of the circuit breaker
        io.github.resilience4j.circuitbreaker.CircuitBreaker.State state = circuitBreaker.getState();
        System.out.println("Circuit Breaker state: " + state);

        // Get the circuit breaker metrics
        io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        System.out.println("Circuit Breaker Metrics:");
        System.out.println("  - Successful calls: " + metrics.getNumberOfSuccessfulCalls());
        System.out.println("  - Failed calls: " + metrics.getNumberOfFailedCalls());
        System.out.println("  - Slow calls: " + metrics.getNumberOfSlowCalls());
        System.out.println("  - Not permitted calls: " + metrics.getNumberOfNotPermittedCalls());
        return order;
    }

    public Order fallbackCustomer(Long orderId, Exception ex) throws OrderNotFoundException {
        // Fallback logic when the circuit is open or an error occurs
        // You can return a default customer or an empty list of orders
        io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        System.out.println("Circuit Breaker Metrics:");
        System.out.println("  - Successful calls: " + metrics.getNumberOfSuccessfulCalls());
        System.out.println("  - Failed calls: " + metrics.getNumberOfFailedCalls());
        System.out.println("  - Slow calls: " + metrics.getNumberOfSlowCalls());
        System.out.println("  - Not permitted calls: " + metrics.getNumberOfNotPermittedCalls());
        
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    public class OrderNotFoundException extends Exception {
        public OrderNotFoundException(String s) {
            super(s);
        }
    }
}

