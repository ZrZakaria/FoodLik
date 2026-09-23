package ma.ensa.foodlik;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ma.ensa.foodlik.ui.CartFragment;
import ma.ensa.foodlik.ui.HistoryFragment;
import ma.ensa.foodlik.ui.MenuFragment;
import ma.ensa.foodlik.ui.RestaurantsFragment;
import ma.ensa.foodlik.ui.SettingsFragment;
import ma.ensa.foodlik.ui.TrackingFragment;

public class MainActivity extends AppCompatActivity implements RestaurantsFragment.Callback, TrackingFragment.Callback, MenuFragment.Callback, CartFragment.Callback, HistoryFragment.Callback {
    public static final String EXTRA_OPEN_HISTORY = "ma.ensa.foodlik.OPEN_HISTORY";

    private TextView connectivityBanner;
    private BottomNavigationView bottomNavigation;
    private boolean programmaticNavSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectivityBanner = findViewById(R.id.connectivityBanner);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (programmaticNavSelection) return true;
            enableBottomNavigationSelection();
            if (id == R.id.navRestaurants) show(new RestaurantsFragment());
            else if (id == R.id.navCart) show(new CartFragment());
            else if (id == R.id.navTracking) show(TrackingFragment.newInstance(0));
            else if (id == R.id.navHistory) show(new HistoryFragment());
            else if (id == R.id.navSettings) show(new SettingsFragment());
            return true;
        });
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.navRestaurants);
            show(new RestaurantsFragment());
        }
        handleLaunchIntent(getIntent());
        requestNotificationPermission();
        connectivityBanner.setVisibility(View.GONE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleLaunchIntent(intent);
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void showTracking(int orderId) {
        selectNavItem(R.id.navTracking);
        show(TrackingFragment.newInstance(orderId));
    }

    @Override
    public void onRestaurantSelected(int id, String name) {
        clearBottomNavigationSelection();
        show(MenuFragment.newInstance(id, name));
    }

    @Override
    public void onOpenCart() {
        selectNavItem(R.id.navCart);
        show(new CartFragment());
    }

    @Override
    public void onOrderCreated(int orderId) {
        showHistory();
    }

    @Override
    public void onOrderSelected(int orderId) {
        selectNavItem(R.id.navHistory);
    }

    @Override
    public void onBrowseRequested() {
        bottomNavigation.setSelectedItemId(R.id.navRestaurants);
    }

    private void handleLaunchIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_OPEN_HISTORY, false)) {
            showHistory();
        }
    }

    private void showHistory() {
        selectNavItem(R.id.navHistory);
        show(new HistoryFragment());
    }

    private void selectNavItem(int itemId) {
        if (bottomNavigation == null) return;
        enableBottomNavigationSelection();
        if (bottomNavigation.getSelectedItemId() == itemId) return;
        programmaticNavSelection = true;
        try {
            bottomNavigation.setSelectedItemId(itemId);
        } finally {
            programmaticNavSelection = false;
        }
    }

    private void clearBottomNavigationSelection() {
        if (bottomNavigation == null) return;
        Menu menu = bottomNavigation.getMenu();
        menu.setGroupCheckable(0, false, true);
        for (int index = 0; index < menu.size(); index++) {
            menu.getItem(index).setChecked(false);
        }
    }

    private void enableBottomNavigationSelection() {
        if (bottomNavigation != null) {
            bottomNavigation.getMenu().setGroupCheckable(0, true, true);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 44);
        }
    }

}
