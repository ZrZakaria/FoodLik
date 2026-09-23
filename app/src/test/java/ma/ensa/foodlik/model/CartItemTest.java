package ma.ensa.foodlik.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CartItemTest {
    @Test
    public void totalMultipliesPriceByQuantity() {
        CartItem item = new CartItem();
        item.price = 42.5;
        item.quantity = 3;

        assertEquals(127.5, item.total(), 0.001);
    }
}
