package uk.ac.ed.inf.pizzadronz.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import java.util.List;

public class OrderValidationChecker {
    public static void main(String[] args) {
        String ordersUrl = "https://ilp-rest-2024.azurewebsites.net/orders";
        RestTemplate restTemplate = new RestTemplate();
        OrderValidationService service = new OrderValidationService(restTemplate);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(ordersUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                List<Order> orders = mapper.readValue(response.getBody(), new TypeReference<>() {});

                int passed = 0, failed = 0;
                System.out.println("Validating all orders...");
                for (Order order : orders) {
                    OrderValidationResult result = service.validateOrder(order);
                    boolean isMatch = order.getOrderValidationCode().equals(result.getValidationCode().toString());

                    if (isMatch) passed++;
                    else failed++;

                    System.out.printf(isMatch ? "✔ Order %s: PASSED%n" : "✘ Order %s: FAILED%n", order.getOrderNo());
                }
                System.out.printf("Summary: %d/%d passed, %d failed.%n", passed, orders.size(), failed);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}