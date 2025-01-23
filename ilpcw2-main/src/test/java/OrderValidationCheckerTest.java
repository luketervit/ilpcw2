import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;
import uk.ac.ed.inf.pizzadronz.utils.OrderValidationChecker;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderValidationCheckerTest {

    @Test
    void testValidateOrders() throws Exception {
        String mockResponse = "[{\"orderNo\": \"1\", \"orderValidationCode\": \"VALID\"}]";

        // Mock RestTemplate
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        // Mock OrderValidationService
        OrderValidationService orderValidationService = mock(OrderValidationService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Order> orders = objectMapper.readValue(mockResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
        Order order = orders.get(0);

        // Use Mockito argument matchers to match any Order object
        when(orderValidationService.validateOrder(any(Order.class)))
                .thenReturn(new OrderValidationResult(order.getOrderNo(), order.getOrderValidationCode()));

        // Execute validation
        OrderValidationChecker checker = new OrderValidationChecker();
        checker.validateOrders("https://mock.url/orders", restTemplate, orderValidationService);

        // Verify calls
        verify(orderValidationService, times(1)).validateOrder(any(Order.class));
    }
}
