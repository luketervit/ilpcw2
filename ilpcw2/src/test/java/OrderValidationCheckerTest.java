import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderValidationCheckerTest {

    private static final String ORDERS_URL = "https://ilp-rest-2024.azurewebsites.net/orders";
    private static RestTemplate restTemplate;
    private static OrderValidationService orderValidationService;
    private static Order[] orders;

    @BeforeAll
    static void setup() {
        // Initialize RestTemplate and OrderValidationService
        restTemplate = new RestTemplate();
        orderValidationService = new OrderValidationService(restTemplate);

        // Fetch orders from the API
        try {
            orders = restTemplate.getForObject(ORDERS_URL, Order[].class);
            assertNotNull(orders, "Orders fetched from API should not be null");
            System.out.println("Fetched " + orders.length + " orders from the API.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching orders from the API", e);
        }
    }

    static Stream<Order> provideOrders() {
        // Provide a stream of orders for the parameterized test
        return Arrays.stream(orders);
    }

    @ParameterizedTest
    @MethodSource("provideOrders")
    @DisplayName("Test Order Validation for Each Order")
    void testValidateOrder(Order order) {
        // Validate the order using the service
        OrderValidationResult result = orderValidationService.validateOrder(order);

        // Assert that the validation result matches the order's expected status
        System.out.printf("Order No: %s | Expected Status: %s | Actual Status: %s%n",
                order.getOrderNo(), order.getOrderStatus(), result.getOrderStatus());

        assertEquals(
                order.getOrderStatus(),
                result.getOrderStatus(),
                "Validation failed for Order No: " + order.getOrderNo()
        );
    }
}
