package ma.ensa.foodlik.model;

import java.util.List;

public class Order {
    public int id;
    public String customerId;
    public String customerName;
    public String customerPhone;
    public int restaurantId;
    public String restaurantName;
    public String restaurantAddress;
    public String restaurantPhone;
    public double restaurantLat;
    public double restaurantLng;
    public String driverName;
    public String driverPhone;
    public String deliveryAddress;
    public double deliveryLat;
    public double deliveryLng;
    public String status;
    public String statusLabel;
    public int statusIndex;
    public double total;
    public String photoPath;
    public String createdAt;
    public String updatedAt;
    public List<OrderItem> items;
    public List<StatusEvent> history;
}
