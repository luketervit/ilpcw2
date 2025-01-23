package uk.ac.ed.inf.pizzadronz.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import java.util.List;

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
        Order order = objectMapper.readValue(mockResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class)).get(0);
        when(orderValidationService.validateOrder(order))
                .thenReturn(new OrderValidationResult(order.getOrderValidationCode()));

        // Execute validation
        OrderValidationChecker checker = new OrderValidationChecker();
        checker.validateOrders("https://mock.url/orders", restTemplate, orderValidationService);

        // Verify calls
        verify(orderValidationService, times(1)).validateOrder(order);
    }
}
