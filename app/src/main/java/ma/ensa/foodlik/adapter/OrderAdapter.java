package ma.ensa.foodlik.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.Holder> {
    public interface Listener {
        void onOrderClick(Order order);
    }

    private final Listener listener;
    private final List<Order> orders = new ArrayList<>();

    public OrderAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Order> next) {
        orders.clear();
        orders.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Order order = orders.get(position);
        holder.title.setText(String.format(Locale.getDefault(), "#%d - %s", order.id, order.restaurantName));
        holder.meta.setText(String.format(Locale.getDefault(), "%.0f MAD - %s", order.total, order.createdAt == null ? "" : order.createdAt));
        holder.status.setText(order.statusLabel == null ? order.status : order.statusLabel);
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView meta;
        final TextView status;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.orderTitle);
            meta = itemView.findViewById(R.id.orderMeta);
            status = itemView.findViewById(R.id.orderStatus);
        }
    }
}
