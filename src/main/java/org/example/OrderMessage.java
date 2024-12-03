package org.example;

// Model class for the order message
public class OrderMessage {
    private int orderId;
    private int customerId;
    private String[] items;

    public OrderMessage(int orderId, int customerId, String[] items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
    }

    // Getters and setters 
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }
}
