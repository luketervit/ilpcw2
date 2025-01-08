package uk.ac.ed.inf.pizzadronz.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constant.SystemConstants;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.models.Restaurant;

import java.util.List;

@Service
public class CalcDeliveryPath {

    private final OrderValidationService orderValidationService;
    private final RestTemplate restTemplate;

    public CalcDeliveryPath(OrderValidationService orderValidationService, RestTemplate restTemplate) {
        this.orderValidationService = orderValidationService;
        this.restTemplate = restTemplate;
    }

    public RestaurantCoordinates getRestaurantCoordinates(Order order) {
        // Fetch all restaurants from the API
        List<Restaurant> restaurants = fetchRestaurants();

        // Call the validatePizzas method from OrderValidationService
        OrderValidationResult validationResult = orderValidationService.validatePizzas(order, restaurants);

        if (validationResult != null) {
            // If there's an issue with the pizzas, throw an exception with the validation message
            throw new IllegalArgumentException(validationResult.getMessage());
        }

        // If validation passed, find the restaurant for the first pizza
        Restaurant restaurant = restaurants.stream()
                .filter(r -> r.getMenu().stream()
                        .anyMatch(pizza -> pizza.getName().equalsIgnoreCase(order.getPizzasInOrder().get(0).getName())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Restaurant for the first pizza not found."));

        // Return only the restaurant name and its coordinates
        return new RestaurantCoordinates(restaurant.getName(), restaurant.getLocation().getLng(), restaurant.getLocation().getLat());
    }

    private List<Restaurant> fetchRestaurants() {
        try {
            String url = SystemConstants.RESTAURANTS_API_URL;
            Restaurant[] restaurants = restTemplate.getForObject(url, Restaurant[].class);
            if (restaurants == null || restaurants.length == 0) {
                throw new RuntimeException("No restaurants found from API.");
            }
            return List.of(restaurants);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch restaurants: " + e.getMessage());
        }
    }

    public static class RestaurantCoordinates {
        private final String name;
        private final double lng;
        private final double lat;

        public RestaurantCoordinates(String name, double lng, double lat) {
            this.name = name;
            this.lng = lng;
            this.lat = lat;
        }

        public String getName() {
            return name;
        }

        public double getLng() {
            return lng;
        }

        public double getLat() {
            return lat;
        }

        @Override
        public String toString() {
            return "RestaurantCoordinates{" +
                    "name='" + name + '\'' +
                    ", lng=" + lng +
                    ", lat=" + lat +
                    '}';
        }
    }
}
