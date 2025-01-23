// src/test/java/uk/ac/ed/inf/pizzadronz/utils/OrderValidationCheckerTest.java
package uk.ac.ed.inf.pizzadronz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
public class OrderValidationCheckerTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderValidationService orderValidationService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testOrderValidation() {
        // Mock API response
        String mockResponse = """
            [
              {
                "orderNo": "1234",
                "orderValidationCode": "Valid",
                "pizzas": ["Margherita"],
                "orderDate": "2024-03-15",
                "priceTotalInPence": 1500,
                "creditCardInformation": {
                    "creditCardNumber": "1234567890123456",
                    "creditCardExpiry": "12/25",
                    "cvv": "123"
                }
              }
            ]
            """;

        mockServer.expect(requestTo("https://ilp-rest-2024.azurewebsites.net/orders"))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Test validation
        Order[] orders = restTemplate.getForObject(
                "https://ilp-rest-2024.azurewebsites.net/orders",
                Order[].class
        );

        assert orders != null;
        for (Order order : orders) {
            OrderValidationResult result = orderValidationService.validateOrder(order);
            assertEquals(
                    order.getOrderValidationCode(),
                    result.getValidationCode().toString(),
                    "Validation failed for order: " + order.getOrderNo()
            );
        }
    }
}