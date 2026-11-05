package com.example.laundrymanager.ui;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.OrderStatus;
import com.example.laundrymanager.model.PaymentStatus;
import com.example.laundrymanager.ui.adapter.OrderAdapter;
import com.example.laundrymanager.util.Ui;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private enum FilterMode {
        ALL, RECEIVED, PROCESSING, DONE, DELIVERED, UNPAID
    }

    private OrderRepository orders;
    private TextView tvCurrentUser;
    private EditText edSearch;
    private ImageButton btnClearSearch;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private View emptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    private FilterMode currentFilter = FilterMode.ALL;
    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();

    @Override
    protected int layoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void onReady() {
        orders = new OrderRepository(this);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        edSearch = findViewById(R.id.edSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        rvOrders = findViewById(R.id.rvOrders);
        emptyState = findViewById(R.id.emptyStateOrders);
        tvEmptyTitle = emptyState.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = emptyState.findViewById(R.id.tvEmptySubtitle);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(order ->
                startActivity(OrderFormActivity.editIntent(this, order.id())));
        rvOrders.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabNewOrder);
        fab.setOnClickListener(v -> startActivity(new Intent(this, OrderFormActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        btnClearSearch.setOnClickListener(v -> edSearch.setText(""));

        Ui.onTextChanged(edSearch, () -> {
            String q = Ui.text(edSearch);
            btnClearSearch.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);
            applyFilter();
        });

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipReceived) {
                currentFilter = FilterMode.RECEIVED;
            } else if (checkedId == R.id.chipProcessing) {
                currentFilter = FilterMode.PROCESSING;
            } else if (checkedId == R.id.chipDone) {
                currentFilter = FilterMode.DONE;
            } else if (checkedId == R.id.chipDelivered) {
                currentFilter = FilterMode.DELIVERED;
            } else if (checkedId == R.id.chipUnpaid) {
                currentFilter = FilterMode.UNPAID;
            } else {
                currentFilter = FilterMode.ALL;
            }
            applyFilter();
        });

        setupTaskbar(NavTab.ORDERS);
    }

    @Override
    protected void onShow() {
        tvCurrentUser.setText(getString(R.string.main_greeting,
                session.fullname(), session.username(), getString(session.role().label())));
        loadOrders();
    }

    private void logout() {
        session.end();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadOrders() {
        allOrders.clear();
        allOrders.addAll(orders.findAll());
        applyFilter();
    }

    private void applyFilter() {
        String keyword = Ui.text(edSearch).toLowerCase(Locale.ROOT);
        filteredOrders.clear();

        for (Order order : allOrders) {
            if (!matchesFilter(order)) {
                continue;
            }
            if (!keyword.isEmpty() && !searchableText(order).contains(keyword)) {
                continue;
            }
            filteredOrders.add(order);
        }

        adapter.setItems(filteredOrders);

        if (filteredOrders.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            if (!keyword.isEmpty()) {
                tvEmptyTitle.setText(R.string.empty_search_title);
                tvEmptySubtitle.setText(R.string.empty_search_desc);
            } else {
                tvEmptyTitle.setText(R.string.empty_orders_title);
                tvEmptySubtitle.setText(R.string.empty_orders_desc);
            }
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    private boolean matchesFilter(Order order) {
        switch (currentFilter) {
            case RECEIVED:
                return order.status() == OrderStatus.RECEIVED;
            case PROCESSING:
                return order.status() == OrderStatus.WASHING || order.status() == OrderStatus.DRYING;
            case DONE:
                return order.status() == OrderStatus.DONE;
            case DELIVERED:
                return order.status() == OrderStatus.DELIVERED;
            case UNPAID:
                return order.payment() == PaymentStatus.UNPAID;
            case ALL:
            default:
                return true;
        }
    }

    private String searchableText(Order order) {
        return (order.id() + " " + order.customer().name() + " " + order.customer().phone() + " "
                + order.service().name() + " "
                + getString(order.status().label()) + " "
                + getString(order.payment().label()))
                .toLowerCase(Locale.ROOT);
    }
}
