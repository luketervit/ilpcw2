//package uk.ac.ed.inf.PizzaDronz.services;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DynamicTest;
//import org.junit.jupiter.api.TestFactory;
//import org.mockito.Mockito;
//import org.springframework.web.client.RestTemplate;
//import uk.ac.ed.inf.PizzaDronz.constants.OrderValidationCode;
//import uk.ac.ed.inf.PizzaDronz.constants.OrderStatus;
//import uk.ac.ed.inf.PizzaDronz.models.Order;
//import uk.ac.ed.inf.PizzaDronz.models.OrderValidationResult;
//import uk.ac.ed.inf.PizzaDronz.models.Restaurant;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//
//class OrderServiceTest {
//
//    private OrderService orderService;
//    private List<Order> testOrders;
//    private Restaurant[] testRestaurants;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//
//        // Load test restaurants from JSON file
//        InputStream restaurantsStream = getClass().getResourceAsStream("/test-restaurants.json");
//        testRestaurants = mapper.readValue(restaurantsStream, Restaurant[].class);
//
//        // Create a mock RestTemplate
//        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
//        Mockito.when(mockRestTemplate.getForObject(anyString(), eq(Restaurant[].class)))
//                .thenReturn(testRestaurants);
//
//        // Initialize RestaurantService with the mocked RestTemplate
//        RestaurantService restaurantService = new RestaurantService(mockRestTemplate);
//
//        // Initialize OrderService with the mocked RestaurantService
//        orderService = new OrderService(restaurantService);
//
//        // Load test orders from JSON file
//        InputStream ordersStream = getClass().getResourceAsStream("/test-orders.json");
//        testOrders = mapper.readValue(ordersStream, new TypeReference<List<Order>>() {});
//    }
//
//    @TestFactory
//    Stream<DynamicTest> testOrderValidation() {
//        return testOrders.stream().map(order ->
//                DynamicTest.dynamicTest("Testing order: " + order.getOrderNo(), () -> {
//                    // Validate the order
//                    OrderValidationResult result = orderService.validateOrder(order);
//
//                    // Convert expected values to enums
//                    OrderValidationCode expectedCode = OrderValidationCode.valueOf(order.getOrderValidationCode());
//                    OrderStatus expectedStatus = OrderStatus.valueOf(order.getOrderStatus());
//
//                    // Assert validation code
//                    assertEquals(expectedCode, result.getValidationCode(),
//                            String.format("Order %s: Expected validation code %s but got %s",
//                                    order.getOrderNo(), expectedCode, result.getValidationCode()));
//
//                    // Assert order status
//                    assertEquals(expectedStatus, result.getOrderStatus(),
//                            String.format("Order %s: Expected status %s but got %s",
//                                    order.getOrderNo(), expectedStatus, result.getOrderStatus()));
//                })
//        );
//    }
//}
