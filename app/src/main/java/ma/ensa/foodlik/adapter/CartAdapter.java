package ma.ensa.foodlik.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.ensa.foodlik.model.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {
    public interface Listener {
        void onIncrement(CartItem item);
        void onDecrement(CartItem item);
        void onRemove(CartItem item);
    }

    private final Context context;
    private final Listener listener;
    private final List<CartItem> items = new ArrayList<>();

    public CartAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submit(List<CartItem> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(ma.ensa.foodlik.R.layout.item_cart, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CartItem item = items.get(position);
        holder.name.setText(item.name);
        holder.price.setText(String.format(Locale.getDefault(), "%.0f MAD", item.total()));
        holder.quantity.setText(String.valueOf(item.quantity));
        holder.decrement.setEnabled(item.quantity > 1);
        holder.increment.setOnClickListener(v -> listener.onIncrement(item));
        holder.decrement.setOnClickListener(v -> listener.onDecrement(item));
        holder.remove.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView price;
        final TextView quantity;
        final ImageButton decrement;
        final ImageButton increment;
        final ImageButton remove;

        Holder(View view) {
            super(view);
            name = view.findViewById(ma.ensa.foodlik.R.id.cartItemName);
            price = view.findViewById(ma.ensa.foodlik.R.id.cartItemPrice);
            quantity = view.findViewById(ma.ensa.foodlik.R.id.cartItemQuantity);
            decrement = view.findViewById(ma.ensa.foodlik.R.id.decrementCartItem);
            increment = view.findViewById(ma.ensa.foodlik.R.id.incrementCartItem);
            remove = view.findViewById(ma.ensa.foodlik.R.id.removeCartItem);
        }
    }
}
