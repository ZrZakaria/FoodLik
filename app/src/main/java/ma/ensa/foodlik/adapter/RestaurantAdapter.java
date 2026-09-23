package ma.ensa.foodlik.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.model.MenuItem;
import ma.ensa.foodlik.model.Restaurant;

public class RestaurantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_RESTAURANT = 0;
    private static final int TYPE_DISH = 1;

    public interface Listener {
        void onRestaurantClick(Restaurant restaurant);
        void onDishClick(MenuItem item);
    }

    private final Listener listener;
    private final List<Object> items = new ArrayList<>();

    public RestaurantAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<?> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Restaurant) return TYPE_RESTAURANT;
        return TYPE_DISH;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_RESTAURANT) {
            return new RestaurantHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false));
        } else {
            return new DishHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof RestaurantHolder) {
            Restaurant restaurant = (Restaurant) item;
            RestaurantHolder h = (RestaurantHolder) holder;
            
            Glide.with(h.image.getContext())
                    .load(restaurant.imageUrl)
                    .placeholder(R.drawable.atlas_burger)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(h.image);

            h.name.setText(restaurant.name);
            if (restaurant.distanceKm >= 0) {
                h.meta.setText(String.format(Locale.getDefault(), "%s - %.1f/5 - %.1f km - %d min",
                        restaurant.cuisine, restaurant.rating, restaurant.distanceKm, restaurant.etaMin));
            } else {
                h.meta.setText(String.format(Locale.getDefault(), "%s - %.1f/5 - %d min",
                        restaurant.cuisine, restaurant.rating, restaurant.etaMin));
            }
            h.address.setText(restaurant.address);
            h.itemView.setOnClickListener(v -> listener.onRestaurantClick(restaurant));
        } else if (holder instanceof DishHolder) {
            MenuItem dish = (MenuItem) item;
            DishHolder h = (DishHolder) holder;
            
            Glide.with(h.image.getContext())
                    .load(dish.imageUrl)
                    .placeholder(R.drawable.atlas_burger)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(h.image);

            h.name.setText(dish.name);
            h.description.setText(dish.description);
            h.price.setText(String.format(Locale.getDefault(), "%.0f MAD", dish.price));
            
            h.itemView.setOnClickListener(v -> listener.onDishClick(dish));
            h.addButton.setOnClickListener(v -> listener.onDishClick(dish));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RestaurantHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView meta;
        final TextView address;

        RestaurantHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.restaurantImage);
            name = itemView.findViewById(R.id.restaurantName);
            meta = itemView.findViewById(R.id.restaurantMeta);
            address = itemView.findViewById(R.id.restaurantAddress);
        }
    }

    static class DishHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView description;
        final TextView price;
        final TextView addButton;

        DishHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.menuImage);
            name = itemView.findViewById(R.id.menuName);
            description = itemView.findViewById(R.id.menuDescription);
            price = itemView.findViewById(R.id.menuPrice);
            addButton = itemView.findViewById(R.id.addItemButton);
        }
    }
}
