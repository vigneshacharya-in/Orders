package in.vigachar.orders.Orders.util;

import in.vigachar.orders.Orders.entity.Customer;
import in.vigachar.orders.Orders.entity.Order;
import in.vigachar.orders.Orders.repository.OrderRepository;
import in.vigachar.orders.Orders.service.OrderService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class AsyncProcessHandler {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RestClient restClient;
    private final OrderRepository orderRepository;
    public static final String R4J_STATE_METRIC = "resilience.circuitbreaker.state";
    private static final String circuitBreakerName = "customers";
    private static final String OPEN_STATE = "OPEN";
    private static final String CLOSED_STATE = "CLOSED";
    private static final String HALF_OPEN_STATE = "HALF_OPEN";
//    private final MetricsEndpoint metricsEndPoint;

    @Async
    public void checkCircuitBreakerState(Long orderId) throws InterruptedException {
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        boolean isOpen = true;
        while (isOpen) {
            printCircuitBreakerMetrics(circuitBreakerName);
            if (OPEN_STATE.equalsIgnoreCase(circuitBreaker.getState().toString())) {
                System.out.println("Circuit is open!");
            } else if (HALF_OPEN_STATE.equalsIgnoreCase(circuitBreaker.getState().toString())) {
                System.out.println("Circuit is half open!");
                try {
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new OrderService.OrderNotFoundException("Order not found with ID: " + orderId));
                    Customer customer = restClient.get(order.getCustomerId());
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getClass().toString() + ", Message: " + ex.getMessage());
                }
                TimeUnit.SECONDS.sleep(5);
            } else if (CLOSED_STATE.equalsIgnoreCase(circuitBreaker.getState().toString())) {
                System.out.println("Circuit is closed!");
                isOpen = false;
            }
        }
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
}
