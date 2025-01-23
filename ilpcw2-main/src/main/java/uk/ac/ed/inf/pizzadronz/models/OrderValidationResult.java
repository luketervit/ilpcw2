package uk.ac.ed.inf.pizzadronz.models;

import uk.ac.ed.inf.pizzadronz.constant.OrderValidationCode;


public class OrderValidationResult {

    private OrderValidationCode validationCode;
    private String message;

    // Constructor
    public OrderValidationResult(OrderValidationCode validationCode, String message) {
        this.validationCode = validationCode;
        this.message = message;
    }

    // Getters and setters
    public OrderValidationCode getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(OrderValidationCode validationCode) {
        this.validationCode = validationCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}