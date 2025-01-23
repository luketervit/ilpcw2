import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderValidationCheckerTest {

    @Test
    void testValidateOrdersFromAPI() {
        String ordersUrl = "https://ilp-rest-2024.azurewebsites.net/orders";

        // Use a real RestTemplate to fetch orders from the API
        RestTemplate restTemplate = new RestTemplate();

        // Initialize OrderValidationService with the real RestTemplate
        OrderValidationService orderValidationService = new OrderValidationService(restTemplate);

        try {
            // Fetch orders from the API
            Order[] orders = restTemplate.getForObject(ordersUrl, Order[].class);

            // Assert that orders were fetched successfully
            assertNotNull(orders, "Orders fetched from API should not be null");
            System.out.println("Fetched " + orders.length + " orders from the API.");

            // Validate each order
            for (Order order : orders) {
                OrderValidationResult result = orderValidationService.validateOrder(order);

                // Print validation results for each order
                System.out.printf("Order No: %s | Validation Code: %s | Order Status: %s%n",
                        order.getOrderNo(), result.getOrderValidationCode(), result.getOrderStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching or validating orders from the API", e);
        }
    }
}
