package ma.ensa.foodlik.model;

public class CartItem {
    public int menuItemId;
    public int restaurantId;
    public String name;
    public int quantity;
    public double price;

    public double total() {
        return price * quantity;
    }
}
