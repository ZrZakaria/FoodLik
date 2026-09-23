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

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.Holder> {
    public interface Listener {
        void onMenuItemClick(MenuItem item);
    }

    private final Listener listener;
    private final List<MenuItem> items = new ArrayList<>();

    public MenuItemAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<MenuItem> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        MenuItem item = items.get(position);
        
        Glide.with(holder.image.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.atlas_burger)
                .error(R.drawable.atlas_burger)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);
                
        holder.image.setContentDescription(item.name);
        holder.name.setText(item.name);
        holder.description.setText(item.description);
        holder.price.setText(String.format(Locale.getDefault(), "%.0f MAD", item.price));
        View.OnClickListener addListener = v -> listener.onMenuItemClick(item);
        holder.itemView.setOnClickListener(addListener);
        holder.add.setOnClickListener(addListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView description;
        final TextView price;
        final TextView add;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.menuImage);
            name = itemView.findViewById(R.id.menuName);
            description = itemView.findViewById(R.id.menuDescription);
            price = itemView.findViewById(R.id.menuPrice);
            add = itemView.findViewById(R.id.addItemButton);
        }
    }
}
