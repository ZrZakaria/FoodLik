package ma.ensa.foodlik.data;

import android.content.Context;
import android.content.SharedPreferences;

import ma.ensa.foodlik.BuildConfig;

public class PreferencesManager {
    private static final String FILE = "foodlik_prefs";
    private static final String USERS_FILE = "foodlik_users";
    
    private static final String KEY_API_URL = "api_url";
    private static final String KEY_CUSTOMER_PHONE = "customer_phone";
    private static final String KEY_DELIVERY_ADDRESS = "delivery_address";
    private static final String KEY_DELIVERY_LAT = "delivery_lat";
    private static final String KEY_DELIVERY_LNG = "delivery_lng";
    private static final String KEY_LAST_ORDER_ID = "last_order_id";
    private static final String KEY_LAST_ORDER_STATUS = "last_order_status";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_GUEST = "is_guest";

    public static final String DEFAULT_CUSTOMER_PHONE = "+212600000000";
    public static final String DEFAULT_DELIVERY_ADDRESS = "Demo delivery point, Rabat (34.0420566, -6.794375)";
    public static final double DEFAULT_DELIVERY_LAT = 34.0420566;
    public static final double DEFAULT_DELIVERY_LNG = -6.794375;

    private final SharedPreferences prefs;
    private final SharedPreferences userPrefs;

    public PreferencesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
        userPrefs = context.getApplicationContext().getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE);
    }

    public String getApiBaseUrl() {
        return BuildConfig.DEFAULT_API_BASE_URL;
    }

    public void setApiBaseUrl(String value) {
        prefs.edit().remove(KEY_API_URL).apply();
    }

    public String getCustomerPhone() {
        return prefs.getString(KEY_CUSTOMER_PHONE, DEFAULT_CUSTOMER_PHONE);
    }

    public void setCustomerPhone(String value) {
        prefs.edit().putString(KEY_CUSTOMER_PHONE, value).apply();
    }

    public String getDeliveryAddress() {
        return prefs.getString(KEY_DELIVERY_ADDRESS, DEFAULT_DELIVERY_ADDRESS);
    }

    public void setDeliveryAddress(String value) {
        prefs.edit().putString(KEY_DELIVERY_ADDRESS, value).apply();
    }

    public double getDeliveryLat() {
        return Double.longBitsToDouble(prefs.getLong(KEY_DELIVERY_LAT, Double.doubleToLongBits(DEFAULT_DELIVERY_LAT)));
    }

    public double getDeliveryLng() {
        return Double.longBitsToDouble(prefs.getLong(KEY_DELIVERY_LNG, Double.doubleToLongBits(DEFAULT_DELIVERY_LNG)));
    }

    public void setDeliveryLocation(double lat, double lng) {
        prefs.edit()
                .putLong(KEY_DELIVERY_LAT, Double.doubleToLongBits(lat))
                .putLong(KEY_DELIVERY_LNG, Double.doubleToLongBits(lng))
                .apply();
    }

    public int getLastOrderId() {
        return prefs.getInt(KEY_LAST_ORDER_ID, 0);
    }

    public void setLastOrder(int id, String status) {
        prefs.edit()
                .putInt(KEY_LAST_ORDER_ID, id)
                .putString(KEY_LAST_ORDER_STATUS, status)
                .apply();
    }

    public String getLastOrderStatus() {
        return prefs.getString(KEY_LAST_ORDER_STATUS, "No active order");
    }

    public void setUserSession(String email, boolean loggedIn) {
        prefs.edit()
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                .putBoolean(KEY_IS_GUEST, false)
                .apply();
    }

    public void setGuestSession() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putBoolean(KEY_IS_GUEST, true)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void logout() {
        prefs.edit()
                .remove(KEY_USER_EMAIL)
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_IS_GUEST)
                .apply();
    }

    // New methods for User Registration and Authentication
    public boolean registerUser(String name, String email, String password) {
        if (userPrefs.contains(email)) {
            return false; // User already exists
        }
        userPrefs.edit()
                .putString(email, password)
                .putString(email + "_name", name)
                .apply();
        return true;
    }

    public boolean authenticate(String email, String password) {
        String savedPassword = userPrefs.getString(email, null);
        return savedPassword != null && savedPassword.equals(password);
    }
}
