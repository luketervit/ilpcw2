package uk.ac.ed.inf.pizzadronz.models;

import java.util.List;

public class Order {
    private String orderNo;
    private String orderDate;
    private String orderStatus;
    private String orderValidationCode;
    private int priceTotalInPence;
    private List<Pizza> pizzasInOrder;
    private CreditCardInformation creditCardInformation;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderValidationCode() {
        return orderValidationCode;
    }

    public void setOrderValidationCode(String orderValidationCode) {
        this.orderValidationCode = orderValidationCode;
    }

    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }

    public void setPriceTotalInPence(int priceTotalInPence) {
        this.priceTotalInPence = priceTotalInPence;
    }

    public List<Pizza> getPizzasInOrder() {
        return pizzasInOrder;
    }

    public void setPizzasInOrder(List<Pizza> pizzasInOrder) {
        this.pizzasInOrder = pizzasInOrder;
    }

    public CreditCardInformation getCreditCardInformation() {
        return creditCardInformation;
    }

    public void setCreditCardInformation(CreditCardInformation creditCardInformation) {
        this.creditCardInformation = creditCardInformation;
    }

    /**
     * Dynamically determines the restaurant based on the pizzas in the order.
     *
     * @param restaurants The list of restaurants fetched from the API.
     * @return The matching Restaurant object or null if no match is found.
     */
    public Restaurant determineRestaurant(List<Restaurant> restaurants) {
        if (pizzasInOrder == null || pizzasInOrder.isEmpty()) {
            return null;
        }

        // Use the first pizza in the order to determine the restaurant
        for (Pizza pizza : pizzasInOrder) {
            for (Restaurant restaurant : restaurants) {
                if (restaurant.getMenu().stream()
                        .anyMatch(menuItem -> menuItem.getName().equalsIgnoreCase(pizza.getName()))) {
                    return restaurant;
                }
            }
        }
        return null;
    }
}
