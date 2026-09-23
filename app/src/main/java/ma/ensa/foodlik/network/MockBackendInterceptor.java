package ma.ensa.foodlik.network;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class MockBackendInterceptor implements Interceptor {
    public static final String MOCK_BASE_URL = "https://foodlik.local/";

    private static final String PREFS = "foodlik_mock_backend";
    private static final String KEY_ORDERS = "orders_json";
    private static final String KEY_NEXT_ORDER_ID = "next_order_id";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String[][] STATUSES = {
            {"PLACED", "Order placed"},
            {"CONFIRMED", "Restaurant confirmed"},
            {"PREPARING", "Preparing your meal"},
            {"OUT_FOR_DELIVERY", "Driver on the way"},
            {"DELIVERED", "Delivered"}
    };

    private static final Object[][] RESTAURANTS = {
            {1, "McDonald's", "Burgers", "Demo point north, Rabat", "+212600100101", 4.7, 24, 34.0442, -6.7941},
            {2, "Sushis For Groups", "Sushi", "Demo point west, Rabat", "+212600100102", 4.5, 32, 34.0417, -6.7972},
            {3, "Dar Tajine", "Moroccan", "Demo point east, Rabat", "+212600100103", 4.8, 28, 34.0436, -6.7916},
            {4, "Pizza Hut", "Pizza", "Demo point south, Rabat", "+212600100104", 4.4, 22, 34.0398, -6.7949}
    };

    private static final Object[][] MENU = {
            {101, 1, "Menu Gourmand McCrispy", "McCrispy meal inspired by McDonald's Morocco delivery menus", "Menus", 97.0},
            {102, 1, "Menu Duo Big Mac", "Big Mac duo menu inspired by Glovo Morocco listings", "Menus", 97.0},
            {103, 1, "Menu Big Mac + Chicken McNuggets 6 pcs", "Big Mac menu with six Chicken McNuggets", "Menus", 82.0},
            {201, 2, "DealBox 22", "California, Fry Ebi Fry, Dragon Eyes and Maki Ebi Fry assortment", "Boxes", 119.0},
            {202, 2, "DealBox 40", "Forty-piece sushi box with California Ebi Fry, Dragon Eyes and maki", "Boxes", 215.0},
            {203, 2, "California Ebi Fry", "Crispy shrimp California roll", "Sushi", 45.0},
            {301, 3, "Couscous with Chicken and 7 Vegetables", "Traditional couscous inspired by Moroccan restaurant menus", "Moroccan", 75.0},
            {302, 3, "Couscous with Meat and 7 Vegetables", "Classic meat couscous with seasonal vegetables", "Moroccan", 85.0},
            {303, 3, "Flan Caramel with Dried Fruits", "Dessert inspired by Dar Tajine menu listings", "Desserts", 30.0},
            {401, 4, "Pizza Super Supreme", "Pizza Hut style supreme pizza with mixed toppings", "Pizza", 73.0},
            {402, 4, "Pizza Pepperoni Lovers", "Pepperoni Lovers pizza inspired by Pizza Hut Morocco delivery listings", "Pizza", 78.0},
            {403, 4, "Fruit de Mer Sauce", "Seafood pizza option inspired by Pizza Hut Morocco menu listings", "Pizza", 82.0}
    };

    private final SharedPreferences prefs;

    public MockBackendInterceptor(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String path = chain.request().url().encodedPath();
        String method = chain.request().method();
        try {
            if ("GET".equals(method) && "/health".equals(path)) {
                return json(chain, 200, new JSONObject()
                        .put("ok", true)
                        .put("service", "FoodLik Android mock backend")
                        .put("timestamp", now()).toString());
            }
            if ("GET".equals(method) && "/restaurants".equals(path)) {
                return json(chain, 200, restaurantsJson().toString());
            }
            if ("GET".equals(method) && path.matches("/restaurants/\\d+")) {
                JSONObject restaurant = restaurantById(idAt(path, 1));
                return restaurant == null ? error(chain, 404, "Restaurant not found") : json(chain, 200, restaurant.toString());
            }
            if ("GET".equals(method) && path.matches("/restaurants/\\d+/menu")) {
                return json(chain, 200, menuForRestaurant(idAt(path, 1)).toString());
            }
            if ("GET".equals(method) && "/orders".equals(path)) {
                return json(chain, 200, ordersJson().toString());
            }
            if ("POST".equals(method) && "/orders".equals(path)) {
                return createOrder(chain);
            }
            if ("GET".equals(method) && path.matches("/orders/\\d+")) {
                JSONObject order = orderById(idAt(path, 1));
                return order == null ? error(chain, 404, "Order not found") : json(chain, 200, order.toString());
            }
            if ("GET".equals(method) && path.matches("/orders/\\d+/tracking")) {
                return tracking(chain, idAt(path, 1));
            }
            if ("POST".equals(method) && path.matches("/orders/\\d+/status/advance")) {
                return advanceStatus(chain, idAt(path, 1));
            }
            if ("POST".equals(method) && path.matches("/orders/\\d+/photo")) {
                return uploadPhoto(chain, idAt(path, 1));
            }
            return error(chain, 404, "Mock endpoint not found");
        } catch (JSONException e) {
            return error(chain, 500, "Mock backend error");
        }
    }

    private Response createOrder(Chain chain) throws IOException, JSONException {
        JSONObject body = new JSONObject(requestBody(chain));
        JSONArray items = body.optJSONArray("items");
        if (items == null || items.length() == 0) return error(chain, 400, "Cart is empty");

        int restaurantId = body.optInt("restaurantId");
        JSONObject restaurant = restaurantById(restaurantId);
        if (restaurant == null) return error(chain, 404, "Restaurant not found");

        JSONArray resolvedItems = new JSONArray();
        double total = 0;
        for (int index = 0; index < items.length(); index++) {
            JSONObject item = items.getJSONObject(index);
            int menuItemId = item.optInt("menuItemId");
            int quantity = Math.max(1, item.optInt("quantity", 1));
            JSONObject menuItem = menuItemById(menuItemId, restaurantId);
            if (menuItem == null) return error(chain, 400, "Invalid menu item " + menuItemId);
            total += menuItem.getDouble("price") * quantity;
            resolvedItems.put(new JSONObject()
                    .put("menuItemId", menuItemId)
                    .put("name", menuItem.getString("name"))
                    .put("quantity", quantity)
                    .put("price", menuItem.getDouble("price")));
        }

        int orderId = prefs.getInt(KEY_NEXT_ORDER_ID, 1);
        String createdAt = now();
        JSONObject order = baseOrder(orderId, restaurant, body, total, createdAt)
                .put("items", resolvedItems)
                .put("history", new JSONArray().put(statusEvent(0, createdAt)));
        JSONArray orders = ordersJson();
        orders.put(order);
        prefs.edit()
                .putString(KEY_ORDERS, orders.toString())
                .putInt(KEY_NEXT_ORDER_ID, orderId + 1)
                .apply();
        return json(chain, 201, order.toString());
    }

    private Response tracking(Chain chain, int orderId) throws JSONException {
        JSONObject order = orderById(orderId);
        if (order == null) return error(chain, 404, "Order not found");
        int statusIndex = order.optInt("statusIndex", 0);
        double progress = Math.min(statusIndex, STATUSES.length - 1) / (double) (STATUSES.length - 1);
        double restaurantLat = order.getDouble("restaurantLat");
        double restaurantLng = order.getDouble("restaurantLng");
        double destinationLat = order.getDouble("deliveryLat");
        double destinationLng = order.getDouble("deliveryLng");
        JSONObject result = new JSONObject()
                .put("orderId", orderId)
                .put("status", order.getString("status"))
                .put("statusLabel", order.getString("statusLabel"))
                .put("etaMinutes", Math.max(0, Math.round((1 - progress) * 24)))
                .put("restaurant", new JSONObject()
                        .put("name", order.getString("restaurantName"))
                        .put("lat", restaurantLat)
                        .put("lng", restaurantLng))
                .put("destination", new JSONObject()
                        .put("address", order.getString("deliveryAddress"))
                        .put("lat", destinationLat)
                        .put("lng", destinationLng))
                .put("driver", new JSONObject()
                        .put("name", order.getString("driverName"))
                        .put("phone", order.getString("driverPhone"))
                        .put("lat", round(restaurantLat + (destinationLat - restaurantLat) * progress))
                        .put("lng", round(restaurantLng + (destinationLng - restaurantLng) * progress)))
                .put("history", order.getJSONArray("history"));
        return json(chain, 200, result.toString());
    }

    private Response advanceStatus(Chain chain, int orderId) throws JSONException {
        JSONArray orders = ordersJson();
        for (int index = 0; index < orders.length(); index++) {
            JSONObject order = orders.getJSONObject(index);
            if (order.getInt("id") == orderId) {
                int nextIndex = Math.min(order.optInt("statusIndex", 0) + 1, STATUSES.length - 1);
                String updatedAt = now();
                order.put("statusIndex", nextIndex)
                        .put("status", STATUSES[nextIndex][0])
                        .put("statusLabel", STATUSES[nextIndex][1])
                        .put("updatedAt", updatedAt);
                JSONArray history = order.getJSONArray("history");
                JSONObject last = history.length() == 0 ? null : history.getJSONObject(history.length() - 1);
                if (last == null || !STATUSES[nextIndex][0].equals(last.optString("status"))) {
                    history.put(statusEvent(nextIndex, updatedAt));
                }
                saveOrders(orders);
                return json(chain, 200, order.toString());
            }
        }
        return error(chain, 404, "Order not found");
    }

    private Response uploadPhoto(Chain chain, int orderId) throws JSONException {
        JSONArray orders = ordersJson();
        for (int index = 0; index < orders.length(); index++) {
            JSONObject order = orders.getJSONObject(index);
            if (order.getInt("id") == orderId) {
                String photoPath = "/android-mock/delivery-" + orderId + ".jpg";
                order.put("photoPath", photoPath).put("updatedAt", now());
                saveOrders(orders);
                return json(chain, 200, new JSONObject()
                        .put("orderId", orderId)
                        .put("photoPath", photoPath)
                        .toString());
            }
        }
        return error(chain, 404, "Order not found");
    }

    private JSONObject baseOrder(int id, JSONObject restaurant, JSONObject body, double total, String createdAt) throws JSONException {
        int driverId = restaurant.getInt("id") % 2 == 0 ? 2 : 1;
        String driverName = driverId == 2 ? "Sara" : "Yassine";
        String driverPhone = driverId == 2 ? "+212600200202" : "+212600200201";
        return new JSONObject()
                .put("id", id)
                .put("customerId", body.optString("customerId", "demo"))
                .put("customerName", "Delivery contact " + body.optString("customerPhone", "+212600000000"))
                .put("customerPhone", body.optString("customerPhone", "+212600000000"))
                .put("restaurantId", restaurant.getInt("id"))
                .put("restaurantName", restaurant.getString("name"))
                .put("restaurantAddress", restaurant.getString("address"))
                .put("restaurantPhone", restaurant.getString("phone"))
                .put("restaurantLat", restaurant.getDouble("lat"))
                .put("restaurantLng", restaurant.getDouble("lng"))
                .put("driverName", driverName)
                .put("driverPhone", driverPhone)
                .put("deliveryAddress", body.optString("deliveryAddress"))
                .put("deliveryLat", body.optDouble("deliveryLat", 34.0420566))
                .put("deliveryLng", body.optDouble("deliveryLng", -6.794375))
                .put("status", STATUSES[0][0])
                .put("statusLabel", STATUSES[0][1])
                .put("statusIndex", 0)
                .put("total", Math.round(total * 100) / 100.0)
                .put("photoPath", JSONObject.NULL)
                .put("createdAt", createdAt)
                .put("updatedAt", createdAt);
    }

    private JSONArray restaurantsJson() throws JSONException {
        JSONArray array = new JSONArray();
        for (Object[] row : RESTAURANTS) array.put(restaurantJson(row));
        return array;
    }

    private JSONObject restaurantById(int id) throws JSONException {
        for (Object[] row : RESTAURANTS) {
            if ((int) row[0] == id) return restaurantJson(row);
        }
        return null;
    }

    private JSONObject restaurantJson(Object[] row) throws JSONException {
        return new JSONObject()
                .put("id", row[0])
                .put("name", row[1])
                .put("cuisine", row[2])
                .put("address", row[3])
                .put("phone", row[4])
                .put("rating", row[5])
                .put("etaMin", row[6])
                .put("lat", row[7])
                .put("lng", row[8]);
    }

    private JSONArray menuForRestaurant(int restaurantId) throws JSONException {
        JSONArray array = new JSONArray();
        for (Object[] row : MENU) {
            if ((int) row[1] == restaurantId) array.put(menuJson(row));
        }
        return array;
    }

    private JSONObject menuItemById(int menuItemId, int restaurantId) throws JSONException {
        for (Object[] row : MENU) {
            if ((int) row[0] == menuItemId && (int) row[1] == restaurantId) return menuJson(row);
        }
        return null;
    }

    private JSONObject menuJson(Object[] row) throws JSONException {
        return new JSONObject()
                .put("id", row[0])
                .put("restaurantId", row[1])
                .put("name", row[2])
                .put("description", row[3])
                .put("category", row[4])
                .put("price", row[5])
                .put("available", true);
    }

    private JSONArray ordersJson() throws JSONException {
        return new JSONArray(prefs.getString(KEY_ORDERS, "[]"));
    }

    private JSONObject orderById(int orderId) throws JSONException {
        JSONArray orders = ordersJson();
        for (int index = 0; index < orders.length(); index++) {
            JSONObject order = orders.getJSONObject(index);
            if (order.getInt("id") == orderId) return order;
        }
        return null;
    }

    private void saveOrders(JSONArray orders) {
        prefs.edit().putString(KEY_ORDERS, orders.toString()).apply();
    }

    private JSONObject statusEvent(int index, String createdAt) throws JSONException {
        return new JSONObject()
                .put("status", STATUSES[index][0])
                .put("label", STATUSES[index][1])
                .put("createdAt", createdAt);
    }

    private String requestBody(Chain chain) throws IOException {
        if (chain.request().body() == null) return "{}";
        Buffer buffer = new Buffer();
        chain.request().body().writeTo(buffer);
        return buffer.readUtf8();
    }

    private int idAt(String path, int segmentIndex) {
        return Integer.parseInt(path.substring(1).split("/")[segmentIndex]);
    }

    private String now() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private double round(double value) {
        return Math.round(value * 1000000.0) / 1000000.0;
    }

    private Response error(Chain chain, int code, String message) {
        return json(chain, code, "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
    }

    private Response json(Chain chain, int code, String body) {
        return new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code >= 200 && code < 300 ? "OK" : "Error")
                .body(ResponseBody.create(body, JSON))
                .build();
    }
}
