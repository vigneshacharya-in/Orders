package in.vigachar.orders.Orders.util;

import in.vigachar.orders.Orders.entity.Customer;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.ConnectException;

@Service
public class RestClient {

    private final WebClient webClient;

    public RestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Customer get(Long customerId) throws ConnectException {
        try {
            return webClient.method(HttpMethod.GET)
                    .uri("http://localhost:8081/api/v1/customers/{customerId}", customerId)
                    .retrieve()
                    .bodyToMono(Customer.class)
                    .block();
        } catch (Exception e) {
//            e.printStackTrace();
            throw new ConnectException("Customer API is down");
        }
    }
}
