import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.models.Restaurant;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import java.util.List;

import static org.mockito.Mockito.*;

class OrderValidationCheckerTest {

    @Test
    void testValidateOrders() throws Exception {
        // Mock API response for orders
        String mockOrderResponse = "[{\"orderNo\": \"1\", \"orderValidationCode\": \"VALID\"}]";
        String mockRestaurantResponse = "[{\"name\": \"PizzaPlace\", \"menu\": [{\"name\": \"Margherita\", \"priceInPence\": 1000}]}]";
        String ordersUrl = "https://ilp-rest-2024.azurewebsites.net/orders";
        String restaurantsUrl = "https://ilp-rest-2024.azurewebsites.net/restaurants";

        // Mock RestTemplate
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(eq(ordersUrl), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockOrderResponse));
        when(restTemplate.getForObject(eq(restaurantsUrl), eq(Restaurant[].class)))
                .thenReturn(new ObjectMapper().readValue(mockRestaurantResponse, Restaurant[].class));

        // Initialize OrderValidationService with the mocked RestTemplate
        OrderValidationService orderValidationService = new OrderValidationService(restTemplate);

        // Mock Order Validation
        ObjectMapper objectMapper = new ObjectMapper();
        List<Order> orders = objectMapper.readValue(mockOrderResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
        Order order = orders.get(0);

        // Use the actual service to validate the order
        OrderValidationResult validationResult = orderValidationService.validateOrder(order);

        // Assertions (Validate the expected result)
        assert validationResult != null;
        //assert "VALID".equals(validationResult.getOrderNo());
        assert "NO_ERROR".equals(validationResult.getOrderValidationCode());
    }
}
