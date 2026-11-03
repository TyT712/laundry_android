package com.example.laundrymanager.ui;

import android.content.Intent;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.util.Formats;
import com.example.laundrymanager.util.Ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private OrderRepository orders;
    private TextView tvCurrentUser;
    private EditText edSearch;
    private ListView listOrders;
    private final List<Order> visibleOrders = new ArrayList<>();

    @Override
    protected int layoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void onReady() {
        orders = new OrderRepository(this);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        edSearch = findViewById(R.id.edSearch);
        listOrders = findViewById(R.id.listOrders);

        findViewById(R.id.btnOrder).setOnClickListener(v -> startActivity(new Intent(this, OrderFormActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnSearch).setOnClickListener(v -> reload());
        findViewById(R.id.btnViewOrders).setOnClickListener(v -> {
            edSearch.setText("");
            reload();
        });
        listOrders.setOnItemClickListener((parent, view, position, id) ->
                startActivity(OrderFormActivity.editIntent(this, visibleOrders.get(position).id())));

        setupTaskbar(NavTab.ORDERS);
    }

    @Override
    protected void onShow() {
        tvCurrentUser.setText(getString(R.string.main_greeting,
                session.fullname(), session.username(), getString(session.role().label())));
        reload();
    }

    private void logout() {
        session.end();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void reload() {
        String keyword = Ui.text(edSearch).toLowerCase(Locale.ROOT);
        visibleOrders.clear();
        for (Order order : orders.findAll()) {
            if (keyword.isEmpty() || searchableText(order).contains(keyword)) {
                visibleOrders.add(order);
            }
        }
        listOrders.setAdapter(Ui.listAdapter(this, visibleOrders, this::describe));
    }

    private String searchableText(Order order) {
        return (order.customer().name() + ' ' + order.customer().phone() + ' ' + order.service().name()
                + ' ' + getString(order.status().label()) + ' ' + getString(order.payment().label()))
                .toLowerCase(Locale.ROOT);
    }

    private String describe(Order order) {
        return getString(R.string.order_row,
                order.id(), order.customer().name(), order.service().name(), Formats.weight(order.weight()),
                Formats.money(this, order.total()),
                getString(order.status().label()), getString(order.payment().label()));
    }
}
