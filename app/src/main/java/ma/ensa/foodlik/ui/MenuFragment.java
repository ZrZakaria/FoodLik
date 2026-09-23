package ma.ensa.foodlik.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.adapter.MenuItemAdapter;
import ma.ensa.foodlik.data.LocalDbHelper;
import ma.ensa.foodlik.model.MenuItem;

public class MenuFragment extends Fragment {
    public interface Callback {
        void onOpenCart();
    }

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private Callback callback;
    private MenuItemAdapter adapter;
    private LocalDbHelper db;
    private View itemOfDayCard;
    private ImageView itemOfDayImage;
    private TextView itemOfDayName;
    private TextView itemOfDayDescription;
    private TextView menuSectionTitle;
    private LinearLayout categoryChips;
    private Button itemOfDayButton;
    private Button openCartButton;
    private final List<MenuItem> allMenuItems = new ArrayList<>();
    private String selectedCategory = "";

    public static MenuFragment newInstance(int restaurantId, String name) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, restaurantId);
        args.putString(ARG_NAME, name);
        MenuFragment fragment = new MenuFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = new LocalDbHelper(requireContext());
        int restaurantId = requireArguments().getInt(ARG_ID);
        TextView title = view.findViewById(R.id.menuTitle);
        title.setText(requireArguments().getString(ARG_NAME, "Menu"));
        itemOfDayCard = view.findViewById(R.id.itemOfDayCard);
        itemOfDayImage = view.findViewById(R.id.itemOfDayImage);
        itemOfDayName = view.findViewById(R.id.itemOfDayName);
        itemOfDayDescription = view.findViewById(R.id.itemOfDayDescription);
        menuSectionTitle = view.findViewById(R.id.menuSectionTitle);
        categoryChips = view.findViewById(R.id.menuCategoryChips);
        itemOfDayButton = view.findViewById(R.id.itemOfDayButton);
        itemOfDayCard.setVisibility(View.GONE);
        RecyclerView recyclerView = view.findViewById(R.id.menuRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MenuItemAdapter(this::addToCart);
        recyclerView.setAdapter(adapter);
        openCartButton = view.findViewById(R.id.openCartButton);
        openCartButton.setOnClickListener(v -> callback.onOpenCart());
        updateCartButton();
        showMenu(db.getMenu(restaurantId), restaurantId);
    }

    private void showMenu(List<MenuItem> items, int restaurantId) {
        allMenuItems.clear();
        for (MenuItem item : items) {
            if (item.restaurantId == 0) item.restaurantId = restaurantId;
            allMenuItems.add(item);
        }
        setupCategoryChips();
        renderFilteredMenu();
    }

    private void setupCategoryChips() {
        categoryChips.removeAllViews();
        addCategoryChip(getString(R.string.category_all), "");
        Set<String> categories = new LinkedHashSet<>();
        for (MenuItem item : allMenuItems) {
            if (item.category != null && !item.category.trim().isEmpty()) categories.add(item.category);
        }
        for (String category : categories) addCategoryChip(category, category);
    }

    private void addCategoryChip(String label, String value) {
        TextView chip = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        params.setMarginEnd(dp(8));
        chip.setLayoutParams(params);
        chip.setMinWidth(dp(72));
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setText(label);
        chip.setTextSize(14);
        chip.setSingleLine(true);
        chip.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
        chip.setTextColor(getResources().getColor(value.equals(selectedCategory) ? R.color.foodlik_on_primary_container : R.color.foodlik_text));
        chip.setBackgroundResource(value.equals(selectedCategory) ? R.drawable.foodlik_chip_selected : R.drawable.foodlik_chip);
        chip.setOnClickListener(v -> {
            selectedCategory = value;
            setupCategoryChips();
            renderFilteredMenu();
        });
        categoryChips.addView(chip);
    }

    private void renderFilteredMenu() {
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : allMenuItems) {
            if (selectedCategory.isEmpty() || sameCategory(selectedCategory, item.category)) filtered.add(item);
        }
        menuSectionTitle.setText(selectedCategory.isEmpty() ? getString(R.string.all_items) : selectedCategory);
        if (filtered.isEmpty()) {
            itemOfDayCard.setVisibility(View.GONE);
            adapter.submit(filtered);
            return;
        }
        MenuItem featured = filtered.get(0);
        itemOfDayCard.setVisibility(View.VISIBLE);
        
        Glide.with(this)
                .load(featured.imageUrl)
                .placeholder(R.drawable.atlas_burger)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemOfDayImage);

        itemOfDayImage.setContentDescription(featured.name);
        itemOfDayName.setText(featured.name);
        itemOfDayDescription.setText(String.format(Locale.getDefault(), "%s - %.0f MAD", featured.description, featured.price));
        itemOfDayButton.setOnClickListener(v -> {
            addToCart(featured);
        });
        adapter.submit(new ArrayList<>(filtered.subList(1, filtered.size())));
    }

    private void addToCart(MenuItem item) {
        int count = db.addToCart(item);
        updateCartButton(count);
        Toast.makeText(requireContext(), item.name + " added to cart", Toast.LENGTH_SHORT).show();
    }

    private void updateCartButton() {
        updateCartButton(db == null ? 0 : db.getCartCount());
    }

    private void updateCartButton(int count) {
        if (openCartButton == null) return;
        if (count > 0) {
            openCartButton.setText(getString(R.string.nav_cart) + " (" + count + ")");
        } else {
            openCartButton.setText(R.string.nav_cart);
        }
    }

    private boolean sameCategory(String selected, String category) {
        return normalizeCategory(selected).equals(normalizeCategory(category));
    }

    private String normalizeCategory(String category) {
        return category == null ? "" : category.trim().toLowerCase(Locale.US);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
