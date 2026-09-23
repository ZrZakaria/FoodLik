package ma.ensa.foodlik.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ma.ensa.foodlik.model.CartItem;
import ma.ensa.foodlik.model.MenuItem;
import ma.ensa.foodlik.model.Order;
import ma.ensa.foodlik.model.OrderItem;
import ma.ensa.foodlik.model.Restaurant;
import ma.ensa.foodlik.model.StatusEvent;
import ma.ensa.foodlik.model.TrackingInfo;

public class LocalDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "foodlik_local.db";
    private static final int DB_VERSION = 13;

    private static final String[][] STATUSES = {
            {"PLACED", "Order placed"},
            {"CONFIRMED", "Restaurant confirmed"},
            {"PREPARING", "Preparing your meal"},
            {"OUT_FOR_DELIVERY", "Driver on the way"},
            {"DELIVERED", "Delivered"}
    };

    private static final Object[][] RESTAURANTS = {
            {1, "McDonald's", "Burgers", "Demo point north, Rabat", "+212600100101", 4.7, 24, 34.0442, -6.7941, "https://images.unsplash.com/photo-1552566626-52f8b828add9?w=500&q=80"},
            {2, "Sushis For Groups", "Sushi", "Demo point west, Rabat", "+212600100102", 4.5, 32, 34.0417, -6.7972, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&q=80"},
            {3, "Dar Tajine", "Moroccan", "Demo point east, Rabat", "+212600100103", 4.8, 28, 34.0436, -6.7916, "https://images.unsplash.com/photo-1541518763669-27fef04b14ea?w=500&q=80"},
            {4, "Pizza Hut", "Pizza", "Demo point south, Rabat", "+212600100104", 4.4, 22, 34.0398, -6.7949, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&q=80"},
            {5, "Casa Pasta", "Italian", "Demo point northwest, Rabat", "+212600100105", 4.6, 26, 34.0451, -6.7964, "https://images.unsplash.com/photo-1473093226795-af9932fe5856?w=500&q=80"},
            {6, "Atlas Burger", "Fast food", "Demo point northeast, Rabat", "+212600100106", 4.3, 18, 34.0450, -6.7922, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80"},
            {7, "Le Beldi", "Traditional", "Demo point southwest, Rabat", "+212600100107", 4.9, 35, 34.0387, -6.7970, "https://images.unsplash.com/photo-1589113182023-7173ba883838?w=500&q=80"},
            {8, "Taco Fiesta", "Tacos", "Demo point southeast, Rabat", "+212600100108", 4.2, 20, 34.0394, -6.7918, "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=500&q=80"},
            {9, "Green Bowl", "Healthy", "Demo point avenue A, Rabat", "+212600100109", 4.6, 21, 34.0462, -6.7948, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&q=80"},
            {10, "Chicken House", "Chicken", "Demo point avenue B, Rabat", "+212600100110", 4.4, 25, 34.0424, -6.7898, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=500&q=80"},
            {11, "Sweet Corner", "Desserts", "Demo point avenue C, Rabat", "+212600100111", 4.7, 16, 34.0408, -6.7927, "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=500&q=80"},
            {12, "Ocean Fish", "Seafood", "Demo point avenue D, Rabat", "+212600100112", 4.5, 30, 34.0470, -6.7978, "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=500&q=80"},
            {13, "Bakery & Co", "Bakery", "Demo point avenue E, Rabat", "+212600100113", 4.6, 15, 34.0379, -6.7934, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&q=80"},
            {14, "Steakhouse 71", "Grill", "Demo point avenue F, Rabat", "+212600100114", 4.7, 35, 34.0431, -6.7992, "https://images.unsplash.com/photo-1544025162-d76694265947?w=500&q=80"},
            {15, "Asian Street", "Asian", "Demo point avenue G, Rabat", "+212600100115", 4.4, 28, 34.0481, -6.7931, "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=500&q=80"},
            {16, "Vegan Life", "Healthy", "Demo point avenue H, Rabat", "+212600100116", 4.5, 22, 34.0411, -6.8004, "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=500&q=80"},
            {17, "Lebanese Corner", "Lebanese", "Demo point avenue I, Rabat", "+212600100117", 4.8, 25, 34.0368, -6.7960, "https://images.unsplash.com/photo-1527324688102-04982dce4962?w=500&q=80"},
            {18, "Waffle Land", "Desserts", "Demo point avenue J, Rabat", "+212600100118", 4.3, 20, 34.0445, -6.7905, "https://images.unsplash.com/photo-1573821663912-56990145564c?w=500&q=80"}
    };

    private static final Object[][] MENU = {
            // McDonald's (1)
            {101, 1, "Menu Gourmand McCrispy", "McCrispy meal with fries and drink", "Burgers", 97.0, "https://images.unsplash.com/photo-1513185158878-8d8c196b7fb7?w=500&q=80"},
            {102, 1, "Menu Duo Big Mac", "Two Big Mac menus for sharing", "Burgers", 97.0, "https://images.unsplash.com/photo-1561758033-d89a9ad46330?w=500&q=80"},
            {103, 1, "Menu Big Mac + Nuggets", "Big Mac menu with 6 Chicken McNuggets", "Burgers", 82.0, "https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=500&q=80"},
            {104, 1, "Double Cheese Burger", "Classic double patty cheeseburger", "Burgers", 38.0, "https://images.unsplash.com/photo-1550547660-d9450f859349?w=500&q=80"},
            {105, 1, "McFlurry Oreo", "Vanilla ice cream with Oreo crumbs", "Desserts", 32.0, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500&q=80"},
            // Sushis For Groups (2)
            {201, 2, "DealBox 22", "22 piece assortment of California and Maki", "Sushi", 119.0, "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=500&q=80"},
            {202, 2, "DealBox 40", "Large 40 piece sushi assortment", "Sushi", 215.0, "https://images.unsplash.com/photo-1553621042-f6e147245754?w=500&q=80"},
            {203, 2, "California Ebi Fry", "Shrimp tempura California roll", "Sushi", 45.0, "https://images.unsplash.com/photo-1617196034183-421b4917c92d?w=500&q=80"},
            // Dar Tajine (3)
            {301, 3, "Couscous Chicken & 7 Veg", "Traditional Moroccan couscous", "Moroccan", 75.0, "https://images.unsplash.com/photo-1541518763669-27fef04b14ea?w=500&q=80"},
            {304, 3, "Tajine Chicken Lemon", "Chicken with preserved lemons and olives", "Moroccan", 79.0, "https://images.unsplash.com/photo-1589113182023-7173ba883838?w=500&q=80"},
            {307, 3, "Pastilla Chicken", "Sweet and savory almond chicken pie", "Moroccan", 95.0, "https://images.unsplash.com/photo-1604329760661-e71dc83f8f26?w=500&q=80"},
            // Pizza Hut (4)
            {401, 4, "Pizza Super Supreme", "Beef, pepperoni, mushrooms, peppers", "Pizza", 73.0, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&q=80"},
            {405, 4, "Pizza Margherita", "Tomato sauce and mozzarella", "Pizza", 59.0, "https://images.unsplash.com/photo-1604068549290-dea0e4a305ca?w=500&q=80"},
            // Casa Pasta (5)
            {501, 5, "Penne Alfredo Poulet", "Pasta with creamy chicken sauce", "Pasta", 68.0, "https://images.unsplash.com/photo-1645112481338-356247c4dd4d?w=500&q=80"},
            {503, 5, "Lasagne Maison", "Traditional layered beef lasagna", "Pasta", 79.0, "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=500&q=80"},
            // Atlas Burger (6)
            {601, 6, "Atlas Smash Burger", "Double beef patty with secret sauce", "Burgers", 64.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80"},
            {603, 6, "Loaded Fries", "Fries with cheese and jalapeños", "Burgers", 34.0, "https://images.unsplash.com/photo-1585109649139-366815a0d713?w=500&q=80"},
            // Le Beldi (7)
            {701, 7, "Rfissa Poulet", "Msemen with chicken and lentils", "Moroccan", 88.0, "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?w=500&q=80"},
            {703, 7, "Tanjia Marrakchia", "Beef cooked in a clay pot", "Moroccan", 105.0, "https://images.unsplash.com/photo-1511690656952-34342bb7c2f2?w=500&q=80"},
            // Taco Fiesta (8)
            {801, 8, "Tacos Mixte", "French taco with chicken and meat", "Tacos", 55.0, "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=500&q=80"},
            {804, 8, "Nachos Fromage", "Crispy nachos with cheese dip", "Tacos", 39.0, "https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?w=500&q=80"},
            // Green Bowl (9)
            {901, 9, "Bowl Quinoa Poulet", "Healthy quinoa and chicken bowl", "Healthy", 69.0, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&q=80"},
            {902, 9, "Salade Caesar", "Chicken, lettuce and parmesan", "Healthy", 58.0, "https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=500&q=80"},
            // Chicken House (10)
            {1001, 10, "Poulet Roti Demi", "Roasted half chicken with fries", "Chicken", 64.0, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=500&q=80"},
            {1003, 10, "Chicken Tenders", "Crispy fried chicken strips", "Chicken", 55.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500&q=80"},
            // Sweet Corner (11)
            {1101, 11, "Crepe Nutella", "Warm crepe with hazelnut cocoa", "Desserts", 36.0, "https://images.unsplash.com/photo-1519676867240-f031ee043477?w=500&q=80"},
            {1103, 11, "Cheesecake Fraise", "New York style strawberry cake", "Desserts", 39.0, "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500&q=80"},
            // Ocean Fish (12)
            {1201, 12, "Paella Fruits de Mer", "Seafood rice specialty", "Seafood", 98.0, "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=500&q=80"},
            {1203, 12, "Crevettes Pil Pil", "Shrimp in garlic chili oil", "Seafood", 76.0, "https://images.unsplash.com/photo-1559742811-822873691df8?w=500&q=80"},
            // Bakery & Co (13)
            {1301, 13, "Pain au Chocolat", "Buttery French chocolate pastry", "Bakery", 12.0, "https://images.unsplash.com/photo-1530610476181-d83430b64dcd?w=500&q=80"},
            {1305, 13, "Tarte au Citron", "Tangy lemon meringue tart", "Desserts", 25.0, "https://images.unsplash.com/photo-1519915028121-7d3463d20b13?w=500&q=80"},
            // Steakhouse 71 (14)
            {1401, 14, "Ribeye Steak", "Grilled ribeye with herb butter", "Steak", 145.0, "https://images.unsplash.com/photo-1544025162-d76694265947?w=500&q=80"},
            {1404, 14, "Steak Burger", "Gourmet burger with thick steak patty", "Burgers", 78.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80"},
            // Asian Street (15)
            {1501, 15, "Pad Thai Poulet", "Thai rice noodles with chicken", "Asian", 65.0, "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=500&q=80"},
            {1505, 15, "Mango Sticky Rice", "Sweet mango with coconut rice", "Desserts", 38.0, "https://images.unsplash.com/photo-1621210185326-1669326d2bbe?w=500&q=80"},
            // Vegan Life (16)
            {1601, 16, "Vegan Burger", "Plant-based patty with vegan mayo", "Healthy", 62.0, "https://images.unsplash.com/photo-1525059696034-4967a8e1dca2?w=500&q=80"},
            {1602, 16, "Buddha Bowl", "Chickpeas, kale, avocado and tahini", "Healthy", 68.0, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&q=80"},
            // Lebanese Corner (17)
            {1701, 17, "Mezza Platter", "Hummus, babaganoush and falafel", "Moroccan", 75.0, "https://images.unsplash.com/photo-1527324688102-04982dce4962?w=500&q=80"},
            {1704, 17, "Beef Shawarma Wrap", "Spiced beef wrap with tahini", "Moroccan", 48.0, "https://images.unsplash.com/photo-1561651823-34feb02250e4?w=500&q=80"},
            // Waffle Land (18)
            {1801, 18, "Waffle Classic Syrup", "Warm waffle with maple syrup", "Desserts", 28.0, "https://images.unsplash.com/photo-1573821663912-56990145564c?w=500&q=80"},
            {1805, 18, "Milkshake Oreo", "Thick Oreo cookies milkshake", "Drinks", 35.0, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500&q=80"}
    };

    public LocalDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        seedCatalog(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS order_items");
        db.execSQL("DROP TABLE IF EXISTS order_status_events");
        db.execSQL("DROP TABLE IF EXISTS orders_cache");
        db.execSQL("DROP TABLE IF EXISTS menu_items");
        db.execSQL("DROP TABLE IF EXISTS restaurants");
        db.execSQL("DROP TABLE IF EXISTS cart");
        onCreate(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE restaurants(id INTEGER PRIMARY KEY, name TEXT, cuisine TEXT, address TEXT, phone TEXT, rating REAL, eta_min INTEGER, lat REAL, lng REAL, image_url TEXT)");
        db.execSQL("CREATE TABLE menu_items(id INTEGER PRIMARY KEY, restaurant_id INTEGER, name TEXT, description TEXT, category TEXT, price REAL, available INTEGER, image_url TEXT)");
        db.execSQL("CREATE TABLE cart(menu_item_id INTEGER PRIMARY KEY, restaurant_id INTEGER, name TEXT, quantity INTEGER, price REAL)");
        db.execSQL("CREATE TABLE orders_cache(id INTEGER PRIMARY KEY AUTOINCREMENT, customer_id TEXT, customer_name TEXT, customer_phone TEXT, restaurant_id INTEGER, restaurant_name TEXT, restaurant_address TEXT, restaurant_phone TEXT, restaurant_lat REAL, restaurant_lng REAL, driver_name TEXT, driver_phone TEXT, delivery_address TEXT, delivery_lat REAL, delivery_lng REAL, status TEXT, status_label TEXT, status_index INTEGER, total REAL, photo_path TEXT, created_at TEXT, updated_at TEXT)");
        db.execSQL("CREATE TABLE order_items(id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, menu_item_id INTEGER, name TEXT, quantity INTEGER, price REAL)");
        db.execSQL("CREATE TABLE order_status_events(id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, status TEXT, label TEXT, created_at TEXT)");
    }

    private void seedCatalog(SQLiteDatabase db) {
        for (Object[] row : RESTAURANTS) {
            ContentValues values = new ContentValues();
            values.put("id", (Integer) row[0]);
            values.put("name", (String) row[1]);
            values.put("cuisine", (String) row[2]);
            values.put("address", (String) row[3]);
            values.put("phone", (String) row[4]);
            values.put("rating", (Double) row[5]);
            values.put("eta_min", (Integer) row[6]);
            values.put("lat", (Double) row[7]);
            values.put("lng", (Double) row[8]);
            values.put("image_url", (String) row[9]);
            db.insertWithOnConflict("restaurants", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        for (Object[] row : MENU) {
            ContentValues values = new ContentValues();
            values.put("id", (Integer) row[0]);
            values.put("restaurant_id", (Integer) row[1]);
            values.put("name", (String) row[2]);
            values.put("description", (String) row[3]);
            values.put("category", (String) row[4]);
            values.put("price", (Double) row[5]);
            values.put("available", 1);
            values.put("image_url", (String) row[6]);
            db.insertWithOnConflict("menu_items", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public List<Restaurant> getRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, name, cuisine, address, phone, rating, eta_min, lat, lng, image_url FROM restaurants ORDER BY id", null);
        try {
            while (cursor.moveToNext()) restaurants.add(readRestaurant(cursor));
        } finally {
            cursor.close();
        }
        return restaurants;
    }

    public List<Restaurant> getRestaurantsByCategory(String category) {
        if (category == null || category.isEmpty()) return getRestaurants();
        List<Restaurant> restaurants = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, name, cuisine, address, phone, rating, eta_min, lat, lng, image_url FROM restaurants WHERE cuisine = ? ORDER BY id", new String[]{category});
        try {
            while (cursor.moveToNext()) restaurants.add(readRestaurant(cursor));
        } finally {
            cursor.close();
        }
        return restaurants;
    }

    public List<Restaurant> getRestaurantsByDishCategory(String category) {
        if (category == null || category.isEmpty()) return getRestaurants();
        List<Restaurant> restaurants = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT DISTINCT r.id, r.name, r.cuisine, r.address, r.phone, r.rating, r.eta_min, r.lat, r.lng, r.image_url " +
                        "FROM restaurants r " +
                        "JOIN menu_items m ON r.id = m.restaurant_id " +
                        "WHERE m.category = ? " +
                        "ORDER BY r.id",
                new String[]{category});
        try {
            while (cursor.moveToNext()) restaurants.add(readRestaurant(cursor));
        } finally {
            cursor.close();
        }
        return restaurants;
    }

    public List<Object> searchEverything(String query) {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(getRestaurants());
        String wildQuery = "%" + query.trim() + "%";
        List<Object> results = new ArrayList<>();
        
        Cursor rCursor = getReadableDatabase().rawQuery(
                "SELECT id, name, cuisine, address, phone, rating, eta_min, lat, lng, image_url " +
                        "FROM restaurants WHERE name LIKE ? OR cuisine LIKE ? ORDER BY name",
                new String[]{wildQuery, wildQuery});
        try {
            while (rCursor.moveToNext()) results.add(readRestaurant(rCursor));
        } finally {
            rCursor.close();
        }

        Cursor mCursor = getReadableDatabase().rawQuery(
                "SELECT id, restaurant_id, name, description, category, price, available, image_url " +
                        "FROM menu_items WHERE name LIKE ? OR description LIKE ? OR category LIKE ? ORDER BY name",
                new String[]{wildQuery, wildQuery, wildQuery});
        try {
            while (mCursor.moveToNext()) results.add(readMenuItem(mCursor));
        } finally {
            mCursor.close();
        }
        
        return results;
    }

    public List<String> getDishCategories() {
        List<String> categories = new ArrayList<>();
        String[] popular = {"Burgers", "Sushi", "Pizza", "Tacos", "Pasta", "Moroccan", "Desserts", "Healthy", "Seafood", "Chicken", "Asian", "Bakery"};
        for (String p : popular) categories.add(p);
        return categories;
    }

    public Restaurant getRestaurant(int id) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, name, cuisine, address, phone, rating, eta_min, lat, lng, image_url FROM restaurants WHERE id = ?", new String[]{String.valueOf(id)});
        try {
            return cursor.moveToFirst() ? readRestaurant(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public List<MenuItem> getMenu(int restaurantId) {
        List<MenuItem> items = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, restaurant_id, name, description, category, price, available, image_url FROM menu_items WHERE restaurant_id = ? ORDER BY id", new String[]{String.valueOf(restaurantId)});
        try {
            while (cursor.moveToNext()) items.add(readMenuItem(cursor));
        } finally {
            cursor.close();
        }
        return items;
    }

    public int addToCart(MenuItem item) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT quantity FROM cart WHERE menu_item_id = ?", new String[]{String.valueOf(item.id)});
        try {
            ContentValues values = new ContentValues();
            values.put("restaurant_id", item.restaurantId);
            values.put("name", item.name);
            values.put("price", item.price);
            if (cursor.moveToFirst()) {
                values.put("quantity", cursor.getInt(0) + 1);
                db.update("cart", values, "menu_item_id = ?", new String[]{String.valueOf(item.id)});
            } else {
                values.put("menu_item_id", item.id);
                values.put("quantity", 1);
                db.insert("cart", null, values);
            }
        } finally {
            cursor.close();
        }
        return getCartCount();
    }

    public List<CartItem> getCart() {
        List<CartItem> items = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT menu_item_id, restaurant_id, name, quantity, price FROM cart ORDER BY name", null);
        try {
            while (cursor.moveToNext()) {
                CartItem item = new CartItem();
                item.menuItemId = cursor.getInt(0);
                item.restaurantId = cursor.getInt(1);
                item.name = cursor.getString(2);
                item.quantity = cursor.getInt(3);
                item.price = cursor.getDouble(4);
                items.add(item);
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public void clearCart() {
        getWritableDatabase().delete("cart", null, null);
    }

    public void incrementCartItem(int menuItemId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE cart SET quantity = quantity + 1 WHERE menu_item_id = ?", new Object[]{menuItemId});
    }

    public void decrementCartItem(int menuItemId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE cart SET quantity = quantity - 1 WHERE menu_item_id = ? AND quantity > 1", new Object[]{menuItemId});
    }

    public void removeCartItem(int menuItemId) {
        getWritableDatabase().delete("cart", "menu_item_id = ?", new String[]{String.valueOf(menuItemId)});
    }

    public int getCartCount() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(quantity), 0) FROM cart", null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public Order createOrder(List<CartItem> cart, PreferencesManager prefs) {
        if (cart.isEmpty()) return null;
        Restaurant restaurant = getRestaurant(cart.get(0).restaurantId);
        if (restaurant == null) return null;
        String createdAt = now();
        double total = 0;
        for (CartItem item : cart) total += item.total();

        int driverId = restaurant.id % 2 == 0 ? 2 : 1;
        ContentValues values = new ContentValues();
        values.put("customer_id", "demo");
        values.put("customer_name", "Delivery contact " + prefs.getCustomerPhone());
        values.put("customer_phone", prefs.getCustomerPhone());
        values.put("restaurant_id", restaurant.id);
        values.put("restaurant_name", restaurant.name);
        values.put("restaurant_address", restaurant.address);
        values.put("restaurant_phone", restaurant.phone);
        values.put("restaurant_lat", restaurant.lat);
        values.put("restaurant_lng", restaurant.lng);
        values.put("driver_name", driverId == 2 ? "Sara" : "Yassine");
        values.put("driver_phone", driverId == 2 ? "+212600200202" : "+212600200201");
        values.put("delivery_address", prefs.getDeliveryAddress());
        values.put("delivery_lat", prefs.getDeliveryLat());
        values.put("delivery_lng", prefs.getDeliveryLng());
        values.put("status", STATUSES[0][0]);
        values.put("status_label", STATUSES[0][1]);
        values.put("status_index", 0);
        values.put("total", Math.round(total * 100) / 100.0);
        values.put("created_at", createdAt);
        values.put("updated_at", createdAt);

        SQLiteDatabase db = getWritableDatabase();
        long orderId = db.insert("orders_cache", null, values);
        for (CartItem item : cart) saveOrderItem(db, (int) orderId, item);
        saveStatusEvent(db, (int) orderId, 0, createdAt);
        clearCart();
        return getOrder((int) orderId);
    }

    public void saveOrder(Order order) {
        if (order == null) return;
        ContentValues values = orderValues(order);
        getWritableDatabase().insertWithOnConflict("orders_cache", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Order getOrder(int id) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, customer_id, customer_name, customer_phone, restaurant_id, restaurant_name, restaurant_address, restaurant_phone, restaurant_lat, restaurant_lng, driver_name, driver_phone, delivery_address, delivery_lat, delivery_lng, status, status_label, status_index, total, photo_path, created_at, updated_at FROM orders_cache WHERE id = ?", new String[]{String.valueOf(id)});
        try {
            return cursor.moveToFirst() ? readOrder(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public List<Order> getCachedOrders() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, customer_id, customer_name, customer_phone, restaurant_id, restaurant_name, restaurant_address, restaurant_phone, restaurant_lat, restaurant_lng, driver_name, driver_phone, delivery_address, delivery_lat, delivery_lng, status, status_label, status_index, total, photo_path, created_at, updated_at FROM orders_cache ORDER BY id DESC", null);
        try {
            while (cursor.moveToNext()) orders.add(readOrder(cursor));
        } finally {
            cursor.close();
        }
        return orders;
    }

    public Order advanceOrderStatus(int orderId) {
        Order order = getOrder(orderId);
        if (order == null) return null;
        int nextIndex = Math.min(order.statusIndex + 1, STATUSES.length - 1);
        String updatedAt = now();
        ContentValues values = new ContentValues();
        values.put("status", STATUSES[nextIndex][0]);
        values.put("status_label", STATUSES[nextIndex][1]);
        values.put("status_index", nextIndex);
        values.put("updated_at", updatedAt);
        SQLiteDatabase db = getWritableDatabase();
        db.update("orders_cache", values, "id = ?", new String[]{String.valueOf(orderId)});
        saveStatusEvent(db, orderId, nextIndex, updatedAt);
        return getOrder(orderId);
    }

    public void savePhotoPath(int orderId, String photoPath) {
        ContentValues values = new ContentValues();
        values.put("photo_path", photoPath);
        values.put("updated_at", now());
        getWritableDatabase().update("orders_cache", values, "id = ?", new String[]{String.valueOf(orderId)});
    }

    public TrackingInfo getTracking(int orderId) {
        Order order = getOrder(orderId);
        if (order == null) return null;
        TrackingInfo info = new TrackingInfo();
        info.orderId = order.id;
        info.status = order.status;
        info.statusLabel = order.statusLabel;
        double progress = Math.min(order.statusIndex, STATUSES.length - 1) / (double) (STATUSES.length - 1);
        info.etaMinutes = Math.max(0, (int) Math.round((1 - progress) * 24));
        info.restaurant = new TrackingInfo.Place();
        info.restaurant.name = order.restaurantName;
        info.restaurant.address = order.restaurantAddress;
        info.restaurant.lat = order.restaurantLat;
        info.restaurant.lng = order.restaurantLng;
        info.destination = new TrackingInfo.Place();
        info.destination.address = order.deliveryAddress;
        info.destination.lat = order.deliveryLat;
        info.destination.lng = order.deliveryLng;
        info.driver = new TrackingInfo.Driver();
        info.driver.name = order.driverName;
        info.driver.phone = order.driverPhone;
        info.driver.lat = round(order.restaurantLat + (order.deliveryLat - order.restaurantLat) * progress);
        info.driver.lng = round(order.restaurantLng + (order.deliveryLng - order.restaurantLng) * progress);
        info.history = getStatusEvents(orderId);
        return info;
    }

    private Restaurant readRestaurant(Cursor cursor) {
        Restaurant restaurant = new Restaurant();
        restaurant.id = cursor.getInt(0);
        restaurant.name = cursor.getString(1);
        restaurant.cuisine = cursor.getString(2);
        restaurant.address = cursor.getString(3);
        restaurant.phone = cursor.getString(4);
        restaurant.rating = cursor.getDouble(5);
        restaurant.etaMin = cursor.getInt(6);
        restaurant.lat = cursor.getDouble(7);
        restaurant.lng = cursor.getDouble(8);
        restaurant.imageUrl = cursor.getString(9);
        return restaurant;
    }

    private MenuItem readMenuItem(Cursor cursor) {
        MenuItem item = new MenuItem();
        item.id = cursor.getInt(0);
        item.restaurantId = cursor.getInt(1);
        item.name = cursor.getString(2);
        item.description = cursor.getString(3);
        item.category = cursor.getString(4);
        item.price = cursor.getDouble(5);
        item.available = cursor.getInt(6) == 1;
        item.imageUrl = cursor.getString(7);
        return item;
    }

    private Order readOrder(Cursor cursor) {
        Order order = new Order();
        order.id = cursor.getInt(0);
        order.customerId = cursor.getString(1);
        order.customerName = cursor.getString(2);
        order.customerPhone = cursor.getString(3);
        order.restaurantId = cursor.getInt(4);
        order.restaurantName = cursor.getString(5);
        order.restaurantAddress = cursor.getString(6);
        order.restaurantPhone = cursor.getString(7);
        order.restaurantLat = cursor.getDouble(8);
        order.restaurantLng = cursor.getDouble(9);
        order.driverName = cursor.getString(10);
        order.driverPhone = cursor.getString(11);
        order.deliveryAddress = cursor.getString(12);
        order.deliveryLat = cursor.getDouble(13);
        order.deliveryLng = cursor.getDouble(14);
        order.status = cursor.getString(15);
        order.statusLabel = cursor.getString(16);
        order.statusIndex = cursor.getInt(17);
        order.total = cursor.getDouble(18);
        order.photoPath = cursor.getString(19);
        order.createdAt = cursor.getString(20);
        order.updatedAt = cursor.getString(21);
        order.items = getOrderItems(order.id);
        order.history = getStatusEvents(order.id);
        return order;
    }

    private ContentValues orderValues(Order order) {
        ContentValues values = new ContentValues();
        values.put("id", order.id);
        values.put("customer_id", order.customerId);
        values.put("customer_name", order.customerName);
        values.put("customer_phone", order.customerPhone);
        values.put("restaurant_id", order.restaurantId);
        values.put("restaurant_name", order.restaurantName);
        values.put("restaurant_address", order.restaurantAddress);
        values.put("restaurant_phone", order.restaurantPhone);
        values.put("restaurant_lat", order.restaurantLat);
        values.put("restaurant_lng", order.restaurantLng);
        values.put("driver_name", order.driverName);
        values.put("driver_phone", order.driverPhone);
        values.put("delivery_address", order.deliveryAddress);
        values.put("delivery_lat", order.deliveryLat);
        values.put("delivery_lng", order.deliveryLng);
        values.put("status", order.status);
        values.put("status_label", order.statusLabel);
        values.put("status_index", order.statusIndex);
        values.put("total", order.total);
        values.put("photo_path", order.photoPath);
        values.put("created_at", order.createdAt);
        values.put("updated_at", order.updatedAt);
        return values;
    }

    private void saveOrderItem(SQLiteDatabase db, int orderId, CartItem cartItem) {
        ContentValues values = new ContentValues();
        values.put("order_id", orderId);
        values.put("menu_item_id", cartItem.menuItemId);
        values.put("name", cartItem.name);
        values.put("quantity", cartItem.quantity);
        values.put("price", cartItem.price);
        db.insert("order_items", null, values);
    }

    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT menu_item_id, name, quantity, price FROM order_items WHERE order_id = ? ORDER BY id", new String[]{String.valueOf(orderId)});
        try {
            while (cursor.moveToNext()) {
                OrderItem item = new OrderItem();
                item.menuItemId = cursor.getInt(0);
                item.name = cursor.getString(1);
                item.quantity = cursor.getInt(2);
                item.price = cursor.getDouble(3);
                items.add(item);
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    private void saveStatusEvent(SQLiteDatabase db, int orderId, int statusIndex, String createdAt) {
        Cursor cursor = db.rawQuery("SELECT status FROM order_status_events WHERE order_id = ? ORDER BY id DESC LIMIT 1", new String[]{String.valueOf(orderId)});
        try {
            if (cursor.moveToFirst() && STATUSES[statusIndex][0].equals(cursor.getString(0))) return;
        } finally {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put("order_id", orderId);
        values.put("status", STATUSES[statusIndex][0]);
        values.put("label", STATUSES[statusIndex][1]);
        values.put("created_at", createdAt);
        db.insert("order_status_events", null, values);
    }

    private List<StatusEvent> getStatusEvents(int orderId) {
        List<StatusEvent> events = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT status, label, created_at FROM order_status_events WHERE order_id = ? ORDER BY id", new String[]{String.valueOf(orderId)});
        try {
            while (cursor.moveToNext()) {
                StatusEvent event = new StatusEvent();
                event.status = cursor.getString(0);
                event.label = cursor.getString(1);
                event.createdAt = cursor.getString(2);
                events.add(event);
            }
        } finally {
            cursor.close();
        }
        return events;
    }

    private String now() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private double round(double value) {
        return Math.round(value * 1000000.0) / 1000000.0;
    }
}
