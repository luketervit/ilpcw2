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

    public static void main(String[] args) {
        String ordersUrl = "https://ilp-rest-2024.azurewebsites.net/orders";
        RestTemplate restTemplate = new RestTemplate();
        OrderValidationService orderValidationService = new OrderValidationService(restTemplate);

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
                    // Run validation logic
                    OrderValidationResult result = orderValidationService.validateOrder(order);

                    // Compare the actual orderValidationCode with the result from the validation logic
                    boolean isMatch = order.getOrderValidationCode().equals(result.getValidationCode().toString());

                    if (isMatch) {
                        passedCount++;
                        System.out.printf("✔ Order %s: PASSED (Expected: %s, Actual: %s)%n",
                                order.getOrderNo(), order.getOrderValidationCode(), result.getValidationCode());
                    } else {
                        failedCount++;
                        System.out.printf("✘ Order %s: FAILED (Expected: %s, Actual: %s)%n",
                                order.getOrderNo(), order.getOrderValidationCode(), result.getValidationCode());
                    }
                }

                // Step 4: Print summary
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
