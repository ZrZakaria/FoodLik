package ma.ensa.foodlik.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.adapter.CartAdapter;
import ma.ensa.foodlik.data.LocalDbHelper;
import ma.ensa.foodlik.data.PreferencesManager;
import ma.ensa.foodlik.model.CartItem;
import ma.ensa.foodlik.model.Order;
import ma.ensa.foodlik.util.NotificationHelper;
import ma.ensa.foodlik.widget.OrderStatusWidgetProvider;

public class CartFragment extends Fragment {
    public interface Callback {
        void onOrderCreated(int orderId);
    }

    private Callback callback;
    private LocalDbHelper db;
    private PreferencesManager prefs;
    private CartAdapter adapter;
    private TextView totalView;
    private TextView emptyMessageView;
    private Button checkoutButton;
    private View cartFooter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = new LocalDbHelper(requireContext());
        prefs = new PreferencesManager(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.cartList);
        totalView = view.findViewById(R.id.cartTotal);
        emptyMessageView = view.findViewById(R.id.emptyCartMessage);
        checkoutButton = view.findViewById(R.id.checkoutButton);
        cartFooter = view.findViewById(R.id.cartFooter);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(requireContext(), new CartAdapter.Listener() {
            @Override
            public void onIncrement(CartItem item) {
                db.incrementCartItem(item.menuItemId);
                refresh();
            }

            @Override
            public void onDecrement(CartItem item) {
                db.decrementCartItem(item.menuItemId);
                refresh();
            }

            @Override
            public void onRemove(CartItem item) {
                db.removeCartItem(item.menuItemId);
                refresh();
            }
        });
        recyclerView.setAdapter(adapter);
        checkoutButton.setOnClickListener(v -> checkout());
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (db != null) refresh();
    }

    private void refresh() {
        List<CartItem> items = db.getCart();
        adapter.submit(items);
        
        if (items.isEmpty()) {
            emptyMessageView.setVisibility(View.VISIBLE);
            cartFooter.setVisibility(View.GONE);
        } else {
            emptyMessageView.setVisibility(View.GONE);
            cartFooter.setVisibility(View.VISIBLE);
            double total = 0;
            for (CartItem item : items) total += item.total();
            totalView.setText(String.format(Locale.getDefault(), "Total: %.0f MAD", total));
        }
        checkoutButton.setEnabled(!items.isEmpty());
    }

    private void checkout() {
        List<CartItem> items = db.getCart();
        if (items.isEmpty()) return;
        Order order = db.createOrder(items, prefs);
        if (order == null) {
            Toast.makeText(requireContext(), "Checkout failed", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.setLastOrder(order.id, order.statusLabel);
        NotificationHelper.notifyOrder(requireContext(), order.id, "FoodLik", order.statusLabel);
        OrderStatusWidgetProvider.updateAll(requireContext());
        callback.onOrderCreated(order.id);
    }
}
