package uk.ac.ed.inf.pizzadronz.models;

import uk.ac.ed.inf.pizzadronz.constant.OrderValidationCode;

public class OrderValidationResult {

    private String orderStatus;
    private String orderValidationCode;

    public OrderValidationResult(String orderStatus, String orderValidationCode) {
        this.orderStatus = orderStatus;
        this.orderValidationCode = orderValidationCode;
    }

    public OrderValidationResult() {

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
}
