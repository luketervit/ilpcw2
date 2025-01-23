package uk.ac.ed.inf.pizzadronz.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constant.OrderValidationCode;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.models.Pizza;
import uk.ac.ed.inf.pizzadronz.models.Restaurant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderValidationService {

    private static final int MAX_PIZZAS = 4;
    private static final int FIXED_CHARGE = 100; // Fixed charge in pence

    private final RestTemplate restTemplate;

    public OrderValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OrderValidationResult validateOrder(Order order) {
        // Validate if the order is empty (no pizzas)
        OrderValidationResult result = validateEmptyOrder(order);
        if (result != null) {
            return result;
        }

        // Fetch restaurants from the API
        List<Restaurant> restaurants = fetchRestaurants();

        // Validate maximum pizza count
        result = validateMaxPizzaCount(order);
        if (result != null) {
            return result;
        }

        // Validate pizzas in the order (check prices, restaurant consistency, and existence)
        result = validatePizzas(order, restaurants);
        if (result != null) {
            return result;
        }

        // Validate the total price of the order
        result = validateOrderTotal(order);
        if (result != null) {
            return result;
        }

        // Validate if the restaurant is open on the order date
        result = validateRestaurantOpen(order, restaurants);
        if (result != null) {
            return result;
        }

        // Validate credit card information
        result = validateCreditCardInformation(order);
        if (result != null) {
            return result;
        }

        // If no issues were found, return NO_ERROR
        return new OrderValidationResult("VALID", OrderValidationCode.NO_ERROR.name());
    }

    private OrderValidationResult validateEmptyOrder(Order order) {
        if (order.getPizzasInOrder() == null || order.getPizzasInOrder().isEmpty()) {
            return new OrderValidationResult("INVALID", OrderValidationCode.EMPTY_ORDER.name());
        }
        return null; // Order is not empty
    }

    private OrderValidationResult validateMaxPizzaCount(Order order) {
        if (order.getPizzasInOrder().size() > MAX_PIZZAS) {
            return new OrderValidationResult("INVALID", OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED.name());
        }
        return null; // Pizza count is valid
    }

    private OrderValidationResult validatePizzas(Order order, List<Restaurant> restaurants) {
        Restaurant firstPizzaRestaurant = null;

        for (Pizza pizza : order.getPizzasInOrder()) {
            Restaurant restaurant = restaurants.stream()
                    .filter(r -> r.getMenu().stream()
                            .anyMatch(menuPizza -> menuPizza.getName().equalsIgnoreCase(pizza.getName())))
                    .findFirst()
                    .orElse(null);

            if (restaurant == null) {
                return new OrderValidationResult("INVALID", OrderValidationCode.PIZZA_NOT_DEFINED.name());
            }

            Pizza menuPizza = restaurant.getMenu().stream()
                    .filter(menuItem -> menuItem.getName().equalsIgnoreCase(pizza.getName()))
                    .findFirst()
                    .orElse(null);

            if (menuPizza != null && pizza.getPriceInPence() != menuPizza.getPriceInPence()) {
                return new OrderValidationResult("INVALID", OrderValidationCode.PRICE_FOR_PIZZA_INVALID.name());
            }

            if (firstPizzaRestaurant == null) {
                firstPizzaRestaurant = restaurant;
            }

            if (!restaurant.equals(firstPizzaRestaurant)) {
                return new OrderValidationResult("INVALID", OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS.name());
            }
        }

        return null; // All pizzas are valid
    }

    private OrderValidationResult validateOrderTotal(Order order) {
        int calculatedTotal = order.getPizzasInOrder().stream()
                .mapToInt(Pizza::getPriceInPence)
                .sum() + FIXED_CHARGE;

        if (order.getPriceTotalInPence() != calculatedTotal) {
            return new OrderValidationResult("INVALID", OrderValidationCode.TOTAL_INCORRECT.name());
        }

        return null; // Total is correct
    }

    private OrderValidationResult validateRestaurantOpen(Order order, List<Restaurant> restaurants) {
        Restaurant restaurant = order.determineRestaurant(restaurants);
        if (restaurant == null) {
            return new OrderValidationResult("INVALID", OrderValidationCode.RESTAURANT_CLOSED.name());
        }

        if (!restaurant.isOpenOn(order.getOrderDate())) {
            return new OrderValidationResult("INVALID", OrderValidationCode.RESTAURANT_CLOSED.name());
        }

        return null; // Restaurant is open
    }

    private OrderValidationResult validateCreditCardInformation(Order order) {
        if (!isValidCvv(order.getCreditCardInformation().getCvv())) {
            return new OrderValidationResult("INVALID", OrderValidationCode.CVV_INVALID.name());
        }
        if (!isValidCreditCardNumber(order.getCreditCardInformation().getCreditCardNumber())) {
            return new OrderValidationResult("INVALID", OrderValidationCode.CARD_NUMBER_INVALID.name());
        }
        if (!isValidCreditCardExpiry(order.getCreditCardInformation().getCreditCardExpiry())) {
            return new OrderValidationResult("INVALID", OrderValidationCode.EXPIRY_DATE_INVALID.name());
        }
        return null;
    }

    public List<Restaurant> fetchRestaurants() {
        try {
            String url = "https://ilp-rest-2024.azurewebsites.net/restaurants";
            Restaurant[] restaurants = restTemplate.getForObject(url, Restaurant[].class);
            return Arrays.asList(restaurants);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch restaurants from API: " + e.getMessage());
        }
    }

    private boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("\\d{3}");
    }

    private boolean isValidCreditCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }

    private boolean isValidCreditCardExpiry(String expiry) {
        if (expiry == null || !expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;
        LocalDate expiryDate = LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth());
        return !expiryDate.isBefore(LocalDate.now());
    }
}
