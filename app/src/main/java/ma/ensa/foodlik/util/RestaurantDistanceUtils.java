package ma.ensa.foodlik.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ma.ensa.foodlik.model.Restaurant;

public final class RestaurantDistanceUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_CITY_SPEED_KMH = 24.0;

    private RestaurantDistanceUtils() {
    }

    public static List<Restaurant> sortedFrom(List<Restaurant> restaurants, double originLat, double originLng) {
        List<Restaurant> sorted = new ArrayList<>(restaurants);
        for (Restaurant restaurant : sorted) {
            restaurant.distanceKm = distanceKm(originLat, originLng, restaurant.lat, restaurant.lng);
            restaurant.etaMin = etaMinutes(restaurant.distanceKm);
        }
        Collections.sort(sorted, Comparator.comparingDouble(restaurant -> restaurant.distanceKm));
        return sorted;
    }

    public static double distanceKm(double fromLat, double fromLng, double toLat, double toLng) {
        double latDelta = Math.toRadians(toLat - fromLat);
        double lngDelta = Math.toRadians(toLng - fromLng);
        double startLat = Math.toRadians(fromLat);
        double endLat = Math.toRadians(toLat);
        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lngDelta / 2) * Math.sin(lngDelta / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private static int etaMinutes(double distanceKm) {
        int minutes = (int) Math.round((distanceKm / AVERAGE_CITY_SPEED_KMH) * 60.0);
        return Math.max(5, minutes);
    }
}
