package ma.ensa.foodlik.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.ensa.foodlik.R;
import ma.ensa.foodlik.adapter.OrderAdapter;
import ma.ensa.foodlik.data.LocalDbHelper;
import ma.ensa.foodlik.model.Order;

public class HistoryFragment extends Fragment {
    public interface Callback {
        void onOrderSelected(int orderId);
        void onBrowseRequested();
    }

    private Callback callback;
    private OrderAdapter adapter;
    private LocalDbHelper db;
    private LinearLayout emptyState;
    private TextView sectionLabel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = new LocalDbHelper(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.historyRecycler);
        emptyState = view.findViewById(R.id.emptyHistoryState);
        sectionLabel = view.findViewById(R.id.historySectionLabel);
        Button browseButton = view.findViewById(R.id.browseFoodButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(order -> callback.onOrderSelected(order.id));
        recyclerView.setAdapter(adapter);

        browseButton.setOnClickListener(v -> callback.onBrowseRequested());

        refresh();
    }

    private void refresh() {
        List<Order> orders = db.getCachedOrders();
        adapter.submit(orders);
        
        if (orders.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            sectionLabel.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            sectionLabel.setVisibility(View.VISIBLE);
        }
    }
}
