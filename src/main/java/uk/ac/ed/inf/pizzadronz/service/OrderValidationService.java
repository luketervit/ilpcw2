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
        return new OrderValidationResult(OrderValidationCode.NO_ERROR, "Order is valid");
    }

    // Validate if the order has no pizzas
    private OrderValidationResult validateEmptyOrder(Order order) {
        if (order.getPizzasInOrder() == null || order.getPizzasInOrder().isEmpty()) {
            return new OrderValidationResult(
                    OrderValidationCode.EMPTY_ORDER,
                    "The order contains no pizzas."
            );
        }
        return null; // Order is not empty
    }

    // Validate maximum pizza count
    private OrderValidationResult validateMaxPizzaCount(Order order) {
        if (order.getPizzasInOrder().size() > MAX_PIZZAS) {
            return new OrderValidationResult(
                    OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED,
                    "The order exceeds the maximum allowed number of pizzas (" + MAX_PIZZAS + ")."
            );
        }
        return null; // Pizza count is valid
    }

    // Validate pizzas (check prices, restaurant consistency, and existence)
    private OrderValidationResult validatePizzas(Order order, List<Restaurant> restaurants) {
        Restaurant firstPizzaRestaurant = null;

        for (Pizza pizza : order.getPizzasInOrder()) {
            // Check if the pizza exists in any restaurant menu
            Restaurant restaurant = restaurants.stream()
                    .filter(r -> r.getMenu().stream()
                            .anyMatch(menuPizza -> menuPizza.getName().equalsIgnoreCase(pizza.getName())))
                    .findFirst()
                    .orElse(null);

            if (restaurant == null) {
                return new OrderValidationResult(
                        OrderValidationCode.PIZZA_NOT_DEFINED,
                        "The pizza '" + pizza.getName() + "' is not defined in any restaurant's menu."
                );
            }

            // Check if pizza price matches the restaurant's menu
            Pizza menuPizza = restaurant.getMenu().stream()
                    .filter(menuItem -> menuItem.getName().equalsIgnoreCase(pizza.getName()))
                    .findFirst()
                    .orElse(null);

            if (menuPizza != null && pizza.getPriceInPence() != menuPizza.getPriceInPence()) {
                return new OrderValidationResult(
                        OrderValidationCode.PRICE_FOR_PIZZA_INVALID,
                        "The price for pizza '" + pizza.getName() + "' is invalid. Expected: " + menuPizza.getPriceInPence() + " pence, Found: " + pizza.getPriceInPence() + " pence."
                );
            }

            // Determine the restaurant of the first pizza
            if (firstPizzaRestaurant == null) {
                firstPizzaRestaurant = restaurant;
            }

            // Check if all pizzas are from the same restaurant
            if (!restaurant.equals(firstPizzaRestaurant)) {
                return new OrderValidationResult(
                        OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS,
                        "The pizzas in the order are from multiple restaurants."
                );
            }
        }

        return null; // All pizzas are valid
    }

    // Validate the total price of the order
    private OrderValidationResult validateOrderTotal(Order order) {
        // Calculate the expected total
        int calculatedTotal = order.getPizzasInOrder().stream()
                .mapToInt(pizza -> pizza.getPriceInPence())
                .sum() + FIXED_CHARGE;

        // Check if the total in the order matches the calculated total
        if (order.getPriceTotalInPence() != calculatedTotal) {
            return new OrderValidationResult(
                    OrderValidationCode.TOTAL_INCORRECT,
                    "Order total is incorrect. Expected: " + calculatedTotal + " pence, Found: " + order.getPriceTotalInPence() + " pence."
            );
        }

        return null; // Total is correct
    }

    // Validate if the restaurant is open on the order date
    private OrderValidationResult validateRestaurantOpen(Order order, List<Restaurant> restaurants) {
        Restaurant restaurant = order.determineRestaurant(restaurants);
        if (restaurant == null) {
            return new OrderValidationResult(
                    OrderValidationCode.RESTAURANT_CLOSED,
                    "The restaurant is closed on the order date."
            );
        }

        String orderDay = getOrderDayFromDate(order.getOrderDate());
        if (!restaurant.isOpenOn(order.getOrderDate())) {
            return new OrderValidationResult(
                    OrderValidationCode.RESTAURANT_CLOSED,
                    "Restaurant is closed on " + orderDay + "."
            );
        }

        return null; // Restaurant is open
    }

    // Validate credit card information (CVV, card number, expiry date)
    private OrderValidationResult validateCreditCardInformation(Order order) {
        if (!isValidCvv(order.getCreditCardInformation().getCvv())) {
            return new OrderValidationResult(OrderValidationCode.CVV_INVALID, "Invalid CVV");
        }
        if (!isValidCreditCardNumber(order.getCreditCardInformation().getCreditCardNumber())) {
            return new OrderValidationResult(OrderValidationCode.CARD_NUMBER_INVALID, "Invalid Credit Card Number");
        }
        if (!isValidCreditCardExpiry(order.getCreditCardInformation().getCreditCardExpiry())) {
            return new OrderValidationResult(OrderValidationCode.EXPIRY_DATE_INVALID, "Invalid Credit Card Expiry Date");
        }
        return null;
    }

    // Fetch restaurants from the API
    public List<Restaurant> fetchRestaurants() {
        try {
            String url = "https://ilp-rest-2024.azurewebsites.net/restaurants";
            Restaurant[] restaurants = restTemplate.getForObject(url, Restaurant[].class);
            return Arrays.asList(restaurants);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch restaurants from API: " + e.getMessage());
        }
    }

    // Helper method to get the day of the week from the orderDate string
    private String getOrderDayFromDate(String orderDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(orderDate, formatter);
        return date.getDayOfWeek().toString(); // Return the day as a string (e.g., MONDAY, SUNDAY)
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
