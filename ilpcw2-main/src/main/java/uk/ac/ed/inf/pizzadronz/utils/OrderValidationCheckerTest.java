package uk.ac.ed.inf.pizzadronz.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderValidationCheckerTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderValidationService orderValidationService;

    @Test
    public void testAllOrdersValidation() {
        // Define the URL for fetching orders
        String ordersUrl = "https://ilp-rest-2024.azurewebsites.net/orders";

        // Fetch orders from the REST API
        ResponseEntity<Order[]> response = restTemplate.getForEntity(ordersUrl, Order[].class);

        // Assert that the HTTP response is successful
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed to fetch orders");

        // Process orders if the response body is not null
        if (response.getBody() != null) {
            int passedCount = 0;
            int failedCount = 0;

            for (Order order : response.getBody()) {
                // Validate the order using the service
                OrderValidationResult result = orderValidationService.validateOrder(order);

                // Compare expected vs. actual validation code
                boolean isMatch = order.getOrderValidationCode().equals(result.getValidationCode().toString());

                // Assert the match for CI reporting
                assertTrue(isMatch, "Order " + order.getOrderNo() +
                        " failed: Expected=" + order.getOrderValidationCode() +
                        ", Actual=" + result.getValidationCode());

                // Track pass/fail counts (for logging only)
                if (isMatch) {
                    passedCount++;
                } else {
                    failedCount++;
                }
            }

            // Log summary (visible in CI logs)
            System.out.printf("Validation Summary: %d/%d orders passed, %d failed.%n",
                    passedCount, response.getBody().length, failedCount);
        } else {
            fail("No orders returned from the API");
        }
    }
}