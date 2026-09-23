package ma.ensa.foodlik.model;

import java.util.ArrayList;
import java.util.List;

public class CreateOrderRequest {
    public int restaurantId;
    public String customerId;
    public String customerPhone;
    public String deliveryAddress;
    public double deliveryLat;
    public double deliveryLng;
    public List<Item> items = new ArrayList<>();

    public static class Item {
        public int menuItemId;
        public int quantity;

        public Item(int menuItemId, int quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }
    }
}
