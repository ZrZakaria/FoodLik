package ma.ensa.foodlik.ui;

import android.Manifest;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.data.LocalDbHelper;
import ma.ensa.foodlik.data.PreferencesManager;
import ma.ensa.foodlik.model.Restaurant;
import ma.ensa.foodlik.util.RestaurantDistanceUtils;

public class TrackingFragment extends Fragment implements OnMapReadyCallback {
    public interface Callback {
        void onRestaurantSelected(int id, String name);
    }

    private static final LatLng DEMO_CENTER = new LatLng(34.0420566, -6.794375);
    private static final float DEMO_DEFAULT_ZOOM = 14.0f;
    private static final int LOCATION_PERMISSION_REQUEST = 42;

    private Callback callback;
    private GoogleMap map;
    private LocalDbHelper db;
    private PreferencesManager preferences;
    private LocationManager locationManager;
    private List<Restaurant> restaurants;
    private TextView subtitleView;
    private TextView mapUnavailableView;
    private TextView restaurantNameView;
    private TextView restaurantMetaView;
    private TextView restaurantAddressView;
    private TextView restaurantPhoneView;
    private Button callRestaurantButton;
    private Button smsRestaurantButton;
    private Button openRestaurantMenuButton;
    private View restaurantSheet;
    private View mapContainer;
    private Restaurant selectedRestaurant;
    private LatLng userLocation;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateUserLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

    public static TrackingFragment newInstance(int orderId) {
        return new TrackingFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = new LocalDbHelper(requireContext());
        preferences = new PreferencesManager(requireContext());
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        restaurants = db.getRestaurants();
        mapContainer = view.findViewById(R.id.mapContainer);
        subtitleView = view.findViewById(R.id.mapSubtitle);
        mapUnavailableView = view.findViewById(R.id.mapUnavailableText);
        restaurantSheet = view.findViewById(R.id.restaurantMapSheet);
        restaurantNameView = view.findViewById(R.id.mapRestaurantName);
        restaurantMetaView = view.findViewById(R.id.mapRestaurantMeta);
        restaurantAddressView = view.findViewById(R.id.mapRestaurantAddress);
        restaurantPhoneView = view.findViewById(R.id.mapRestaurantPhone);
        callRestaurantButton = view.findViewById(R.id.callRestaurantButton);
        smsRestaurantButton = view.findViewById(R.id.smsRestaurantButton);
        openRestaurantMenuButton = view.findViewById(R.id.openRestaurantMenuButton);
        ImageButton closeRestaurantSheet = view.findViewById(R.id.closeRestaurantSheet);
        closeRestaurantSheet.setOnClickListener(v -> hideRestaurantSheet());
        callRestaurantButton.setOnClickListener(v -> callSelectedRestaurant());
        smsRestaurantButton.setOnClickListener(v -> messageSelectedRestaurant());
        openRestaurantMenuButton.setOnClickListener(v -> {
            if (selectedRestaurant != null && callback != null) {
                callback.onRestaurantSelected(selectedRestaurant.id, selectedRestaurant.name);
            }
        });
        subtitleView.setText(getString(R.string.map_restaurants_count, restaurants.size()));

        int playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext());
        if (playServicesStatus != ConnectionResult.SUCCESS) {
            showMapUnavailable();
            return;
        }

        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapContainer);
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.mapContainer, mapFragment)
                        .commitNow();
            }
            mapFragment.getMapAsync(this);
        } catch (RuntimeException e) {
            showMapUnavailable();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        try {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(true);
            map.setOnMapClickListener(latLng -> hideRestaurantSheet());
            map.setOnMarkerClickListener(this::showRestaurantSheet);
            showRestaurants();
            requestPhoneLocation();
        } catch (RuntimeException e) {
            showMapUnavailable();
        }
    }

    private void showRestaurants() {
        if (map == null || restaurants == null || restaurants.isEmpty()) return;
        map.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        if (userLocation != null) {
            boundsBuilder.include(userLocation);
            map.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title(getString(R.string.map_your_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        for (Restaurant restaurant : restaurants) {
            LatLng position = new LatLng(restaurant.lat, restaurant.lng);
            boundsBuilder.include(position);
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(restaurant.name)
                    .snippet(metaText(restaurant)));
            if (marker != null) marker.setTag(restaurant);
        }
        moveCameraToVisibleArea(boundsBuilder);
    }

    private boolean showRestaurantSheet(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (!(tag instanceof Restaurant)) return false;
        selectedRestaurant = (Restaurant) tag;
        restaurantNameView.setText(selectedRestaurant.name);
        restaurantMetaView.setText(metaText(selectedRestaurant));
        restaurantAddressView.setText(selectedRestaurant.address);
        restaurantPhoneView.setText(getString(R.string.restaurant_phone_format, selectedRestaurant.phone));
        restaurantSheet.setVisibility(View.VISIBLE);
        return true;
    }

    private void hideRestaurantSheet() {
        selectedRestaurant = null;
        if (restaurantSheet != null) restaurantSheet.setVisibility(View.GONE);
    }

    private void callSelectedRestaurant() {
        if (selectedRestaurant == null) return;
        openIntent(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + selectedRestaurant.phone)), R.string.no_phone_app);
    }

    private void messageSelectedRestaurant() {
        if (selectedRestaurant == null) return;
        openIntent(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + selectedRestaurant.phone))
                .putExtra("sms_body", getString(R.string.restaurant_sms_body, selectedRestaurant.name)), R.string.no_sms_app);
    }

    private void openIntent(Intent intent, int errorStringRes) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), errorStringRes, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPhoneLocation() {
        if (!hasLocationPermission()) {
            subtitleView.setText(R.string.map_waiting_for_location);
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST);
            return;
        }
        enableUserLocationLayer();
        Location lastLocation = bestLastKnownLocation();
        if (lastLocation != null) updateUserLocation(lastLocation);
        requestFreshLocation();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void enableUserLocationLayer() {
        if (map == null || !hasLocationPermission()) return;
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) return;
        if (hasLocationPermission()) {
            requestPhoneLocation();
        } else if (subtitleView != null) {
            subtitleView.setText(R.string.map_location_permission_denied);
        }
    }

    private Location bestLastKnownLocation() {
        if (locationManager == null || !hasLocationPermission()) return null;
        Location best = null;
        String[] providers = new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };
        for (String provider : providers) {
            try {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate == null) continue;
                if (best == null || candidate.getAccuracy() < best.getAccuracy()
                        || candidate.getTime() > best.getTime()) {
                    best = candidate;
                }
            } catch (SecurityException | IllegalArgumentException ignored) {
            }
        }
        return best;
    }

    private void requestFreshLocation() {
        if (locationManager == null || !hasLocationPermission()) return;
        String provider = null;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            }
            if (provider == null) return;
            locationManager.requestSingleUpdate(provider, locationListener, Looper.getMainLooper());
        } catch (SecurityException | IllegalArgumentException ignored) {
        }
    }

    private void updateUserLocation(@NonNull Location location) {
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        preferences.setDeliveryLocation(userLocation.latitude, userLocation.longitude);
        restaurants = RestaurantDistanceUtils.sortedFrom(db.getRestaurants(), userLocation.latitude, userLocation.longitude);
        subtitleView.setText(getString(R.string.map_restaurants_near_user, restaurants.size()));
        hideRestaurantSheet();
        showRestaurants();
    }

    private String metaText(Restaurant restaurant) {
        if (restaurant.distanceKm >= 0) {
            return String.format(Locale.getDefault(), "%s - %.1f/5 - %.1f km - %d min",
                    restaurant.cuisine, restaurant.rating, restaurant.distanceKm, restaurant.etaMin);
        }
        return String.format(Locale.getDefault(), "%s - %.1f/5 - %d min",
                restaurant.cuisine, restaurant.rating, restaurant.etaMin);
    }

    private void moveCameraToVisibleArea(LatLngBounds.Builder boundsBuilder) {
        if (map == null) return;
        try {
            if (mapContainer == null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEMO_CENTER, DEMO_DEFAULT_ZOOM));
                return;
            }
            mapContainer.post(() -> {
                try {
                    int width = mapContainer.getWidth();
                    int height = mapContainer.getHeight();
                    if (width <= 0 || height <= 0) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEMO_CENTER, DEMO_DEFAULT_ZOOM));
                        return;
                    }
                    int padding = Math.max(96, Math.min(width, height) / 7);
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), width, height, padding));
                } catch (RuntimeException e) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEMO_CENTER, DEMO_DEFAULT_ZOOM));
                }
            });
        } catch (RuntimeException e) {
            showMapUnavailable();
        }
    }

    private void showMapUnavailable() {
        if (mapUnavailableView != null) {
            mapUnavailableView.setVisibility(View.VISIBLE);
        }
        if (subtitleView != null) {
            subtitleView.setText(R.string.map_unavailable);
        }
    }
}
