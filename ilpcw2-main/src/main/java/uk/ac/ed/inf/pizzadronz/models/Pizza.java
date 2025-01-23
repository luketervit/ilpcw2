package uk.ac.ed.inf.pizzadronz.models;

public class Pizza {
    private String name;
    private int priceInPence;

    // Default constructor for JSON deserialization
    public Pizza() {
    }

    // Constructor for convenience
    public Pizza(String name, int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }

    public void setPriceInPence(int priceInPence) {
        this.priceInPence = priceInPence;
    }

    // Validation method for pizza
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && priceInPence > 0;
    }

    // Override toString() for easier debugging
    @Override
    public String toString() {
        return "Pizza{" +
                "name='" + name + '\'' +
                ", priceInPence=" + priceInPence +
                '}';
    }

    // Equals and hashCode for comparison purposes
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Pizza pizza = (Pizza) obj;

        if (priceInPence != pizza.priceInPence) return false;
        return name != null ? name.equals(pizza.name) : pizza.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + priceInPence;
        return result;
    }
}
