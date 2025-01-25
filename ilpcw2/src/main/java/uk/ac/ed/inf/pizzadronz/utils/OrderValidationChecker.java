package uk.ac.ed.inf.pizzadronz.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;

import java.util.List;

public class OrderValidationChecker {

    public void validateOrders(String ordersUrl, RestTemplate restTemplate, OrderValidationService orderValidationService) {
        try {
            // Step 1: Fetch orders data
            ResponseEntity<String> response = restTemplate.getForEntity(ordersUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Step 2: Parse JSON response into a list of orders
                ObjectMapper objectMapper = new ObjectMapper();
                List<Order> orders = objectMapper.readValue(response.getBody(), new TypeReference<List<Order>>() {});

                int passedCount = 0;
                int failedCount = 0;

                System.out.println("Validating all orders...");

                // Step 3: Evaluate each order
                for (Order order : orders) {
                    OrderValidationResult result = orderValidationService.validateOrder(order);

                    boolean isMatch = order.getOrderValidationCode().equals(result.getOrderValidationCode());

                    if (isMatch) {
                        passedCount++;
                        System.out.printf("✔ Order %s: PASSED (Expected: %s, Actual: %s)%n",
                                order.getOrderNo(), order.getOrderValidationCode(), result.getOrderValidationCode());
                    } else {
                        failedCount++;
                        System.out.printf("✘ Order %s: FAILED (Expected: %s, Actual: %s)%n",
                                order.getOrderNo(), order.getOrderValidationCode(), result.getOrderValidationCode());
                    }
                }

                System.out.printf("Validation Summary: %d/%d orders passed, %d failed.%n",
                        passedCount, orders.size(), failedCount);
            } else {
                System.err.println("Failed to fetch orders or received an empty response.");
            }
        } catch (Exception e) {
            System.err.println("An error occurred while validating orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
