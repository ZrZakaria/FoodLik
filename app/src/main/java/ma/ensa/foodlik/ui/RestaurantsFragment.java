package ma.ensa.foodlik.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.adapter.RestaurantAdapter;
import ma.ensa.foodlik.data.LocalDbHelper;
import ma.ensa.foodlik.data.PreferencesManager;
import ma.ensa.foodlik.model.MenuItem;
import ma.ensa.foodlik.model.Restaurant;
import ma.ensa.foodlik.util.RestaurantDistanceUtils;

public class RestaurantsFragment extends Fragment implements RestaurantAdapter.Listener {
    public interface Callback {
        void onRestaurantSelected(int id, String name);
    }

    private Callback callback;
    private RestaurantAdapter adapter;
    private LocalDbHelper db;
    private PreferencesManager preferences;
    private EditText searchEditText;
    private LinearLayout dishCategoryChips;
    private LinearLayout restaurantChips;
    private TextView sectionTitle;

    private boolean suppressSearchChange;
    private String selectedCuisine = "";
    private String selectedDishCategory = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = new LocalDbHelper(requireContext());
        preferences = new PreferencesManager(requireContext());
        searchEditText = view.findViewById(R.id.searchEditText);
        dishCategoryChips = view.findViewById(R.id.dishCategoryChips);
        // We reused cuisineChips ID for Restaurant Brands in the layout before, 
        // let's rename or use it as restaurantChips
        restaurantChips = view.findViewById(R.id.cuisineChips); 
        sectionTitle = view.findViewById(R.id.sectionTitle);
        RecyclerView recyclerView = view.findViewById(R.id.restaurantsRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RestaurantAdapter(this);
        recyclerView.setAdapter(adapter);

        setupSearch();
        setupFilters();
        loadInitialData();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressSearchChange) return;
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    selectedCuisine = "";
                    selectedDishCategory = "";
                    updateFilterChips();
                    performSearch(query);
                } else {
                    applyFilters();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        updateFilterChips();
    }

    private void updateFilterChips() {
        // 1. Dish Categories
        dishCategoryChips.removeAllViews();
        addFilterChip(dishCategoryChips, getString(R.string.category_all), "", true);
        for (String cat : db.getDishCategories()) {
            addFilterChip(dishCategoryChips, cat, cat, true);
        }

        // 2. Restaurant cuisine filters
        restaurantChips.removeAllViews();
        addFilterChip(restaurantChips, getString(R.string.category_all), "", false);
        for (String cuisine : getCuisineFilters()) {
            addFilterChip(restaurantChips, cuisine, cuisine, false);
        }
    }

    private void addFilterChip(LinearLayout container, String label, String value, boolean isDishCategory) {
        TextView chip = new TextView(requireContext());
        float density = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        params.setMarginEnd((int) (8 * density));
        chip.setLayoutParams(params);
        chip.setMinWidth(dp(72));
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setText(label);
        chip.setTextSize(14);
        chip.setSingleLine(true);
        chip.setPadding(dp(16), 0, dp(16), 0);

        boolean isSelected = isFilterSelected(value, isDishCategory);
        chip.setSelected(isSelected);
        chip.setTextColor(getResources().getColor(isSelected ? R.color.foodlik_on_primary_container : R.color.foodlik_text));
        chip.setBackgroundResource(isSelected ? R.drawable.foodlik_chip_selected : R.drawable.foodlik_chip);

        chip.setOnClickListener(v -> {
            if (isDishCategory) {
                selectedDishCategory = value;
                selectedCuisine = "";
            } else {
                selectedCuisine = value;
                selectedDishCategory = "";
            }

            clearSearchText();
            updateFilterChips();
            applyFilters();
        });
        
        container.addView(chip);
    }

    private void performSearch(String query) {
        List<Object> results = db.searchEverything(query);
        adapter.submit(results);
        sectionTitle.setText(results.isEmpty() ? R.string.search_no_results : R.string.search_results);
    }

    private void applyFilters() {
        if (!selectedDishCategory.isEmpty()) {
            List<Restaurant> restaurants = db.getRestaurantsByDishCategory(selectedDishCategory);
            adapter.submit(sortFromSavedLocation(restaurants));
            sectionTitle.setText(getString(R.string.category_restaurants_format, selectedDishCategory));
        } else if (!selectedCuisine.isEmpty()) {
            List<Restaurant> restaurants = db.getRestaurantsByCategory(selectedCuisine);
            adapter.submit(sortFromSavedLocation(restaurants));
            sectionTitle.setText(getString(R.string.category_restaurants_format, selectedCuisine));
        } else {
            loadInitialData();
        }
    }

    private void loadInitialData() {
        adapter.submit(sortFromSavedLocation(db.getRestaurants()));
        sectionTitle.setText(R.string.top_restaurants);
    }

    private List<Restaurant> sortFromSavedLocation(List<Restaurant> restaurants) {
        return RestaurantDistanceUtils.sortedFrom(restaurants, preferences.getDeliveryLat(), preferences.getDeliveryLng());
    }

    private List<String> getCuisineFilters() {
        Set<String> cuisines = new LinkedHashSet<>();
        for (Restaurant restaurant : db.getRestaurants()) {
            if (restaurant.cuisine != null && !restaurant.cuisine.trim().isEmpty()) {
                cuisines.add(restaurant.cuisine);
            }
        }
        return new ArrayList<>(cuisines);
    }

    private boolean isFilterSelected(String value, boolean isDishCategory) {
        if (value.isEmpty()) {
            return selectedDishCategory.isEmpty() && selectedCuisine.isEmpty();
        }
        return isDishCategory ? value.equals(selectedDishCategory) : value.equals(selectedCuisine);
    }

    private void clearSearchText() {
        if (searchEditText == null || searchEditText.getText().length() == 0) return;
        suppressSearchChange = true;
        try {
            searchEditText.setText("");
        } finally {
            suppressSearchChange = false;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        callback.onRestaurantSelected(restaurant.id, restaurant.name);
    }

    @Override
    public void onDishClick(MenuItem item) {
        // When clicking a dish from search results, add to cart or open restaurant?
        // Let's add to cart directly as it's more convenient from search
        db.addToCart(item);
        Toast.makeText(requireContext(), item.name + " added to cart", Toast.LENGTH_SHORT).show();
    }
}
