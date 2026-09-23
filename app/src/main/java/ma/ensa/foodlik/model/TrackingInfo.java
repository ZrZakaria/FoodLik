package ma.ensa.foodlik.model;

import java.util.List;

public class TrackingInfo {
    public int orderId;
    public String status;
    public String statusLabel;
    public int etaMinutes;
    public Place restaurant;
    public Place destination;
    public Driver driver;
    public List<StatusEvent> history;

    public static class Place {
        public String name;
        public String address;
        public double lat;
        public double lng;
    }

    public static class Driver {
        public String name;
        public String phone;
        public double lat;
        public double lng;
    }
}
