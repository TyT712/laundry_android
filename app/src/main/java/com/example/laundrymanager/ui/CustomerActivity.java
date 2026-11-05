package com.example.laundrymanager.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.CustomerRepository;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.ui.adapter.CustomerAdapter;
import com.example.laundrymanager.util.Formats;
import com.example.laundrymanager.util.Ui;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CustomerActivity extends BaseActivity {
    public static final String EXTRA_QUICK_ADD = "quick_add";
    public static final String EXTRA_NEW_CUSTOMER_ID = "new_customer_id";

    public static Intent quickAddIntent(Context context) {
        return new Intent(context, CustomerActivity.class).putExtra(EXTRA_QUICK_ADD, true);
    }

    private CustomerRepository customers;
    private OrderRepository orders;
    private EditText edName;
    private EditText edPhone;
    private EditText edAddress;
    private EditText edSearch;
    private ImageButton btnClearSearch;
    private Button btnSave;
    private Button btnCancel;
    private TextView tvFormHeader;
    private TextView tvCustomerCount;

    private RecyclerView rvCustomers;
    private CustomerAdapter adapter;
    private View emptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    private List<Customer> items = new ArrayList<>();
    private Customer editing;
    private boolean isQuickAdd;

    @Override
    protected int layoutRes() {
        return R.layout.activity_customer;
    }

    @Override
    protected void onReady() {
        customers = new CustomerRepository(this);
        orders = new OrderRepository(this);

        edName = findViewById(R.id.edCustomerName);
        edPhone = findViewById(R.id.edCustomerPhone);
        edAddress = findViewById(R.id.edCustomerAddress);
        edSearch = findViewById(R.id.edSearchCustomer);
        btnClearSearch = findViewById(R.id.btnClearCustomerSearch);
        btnSave = findViewById(R.id.btnSaveCustomer);
        btnCancel = findViewById(R.id.btnClearCustomer);
        tvFormHeader = findViewById(R.id.tvFormHeader);
        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        rvCustomers = findViewById(R.id.rvCustomers);
        emptyState = findViewById(R.id.emptyStateCustomers);
        tvEmptyTitle = emptyState.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = emptyState.findViewById(R.id.tvEmptySubtitle);

        isQuickAdd = getIntent().getBooleanExtra(EXTRA_QUICK_ADD, false);

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(
                this::showCustomerActionMenu,
                this::dialCustomer,
                this::smsCustomer
        );
        rvCustomers.setAdapter(adapter);

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> clearForm());

        btnClearSearch.setOnClickListener(v -> edSearch.setText(""));

        Ui.onTextChanged(edSearch, () -> {
            String q = Ui.text(edSearch);
            btnClearSearch.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);
            reload();
        });

        reload();
        setupTaskbar(NavTab.CUSTOMERS);
    }

    private void showCustomerActionMenu(Customer customer) {
        String[] actions = {
                getString(R.string.action_view_history),
                getString(R.string.action_edit),
                getString(R.string.action_delete)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(customer.name() + " (" + customer.phone() + ")")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showCustomerHistory(customer);
                    } else if (which == 1) {
                        startEditing(customer);
                    } else if (which == 2) {
                        delete(customer);
                    }
                })
                .show();
    }

    private void showCustomerHistory(Customer customer) {
        List<Order> customerOrders = orders.findByCustomer(customer.id());
        if (customerOrders.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.dialog_history_title, customer.name()))
                    .setMessage(R.string.no_orders_for_customer)
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        double totalSpend = 0;
        String[] orderDescriptions = new String[customerOrders.size()];
        for (int i = 0; i < customerOrders.size(); i++) {
            Order o = customerOrders.get(i);
            totalSpend += o.total();
            orderDescriptions[i] = "#" + o.id() + " • " + o.service().name() + " (" + Formats.weight(o.weight()) + "kg)\n"
                    + Formats.money(this, o.total()) + " • " + getString(o.status().label()) + " • " + getString(o.payment().label());
        }

        String summary = getString(R.string.dialog_history_summary, customerOrders.size(), Formats.money(this, totalSpend));

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_history_title, customer.name()) + "\n" + summary)
                .setItems(orderDescriptions, (dialog, which) -> {
                    Order selectedOrder = customerOrders.get(which);
                    startActivity(OrderFormActivity.editIntent(this, selectedOrder.id()));
                })
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void dialCustomer(Customer customer) {
        String phone = customer.phone();
        if (phone != null && !phone.trim().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.trim()));
            startActivity(intent);
        }
    }

    private void smsCustomer(Customer customer) {
        String phone = customer.phone();
        if (phone != null && !phone.trim().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone.trim()));
            startActivity(intent);
        }
    }

    private void reload() {
        String keyword = Ui.text(edSearch);
        items = customers.search(keyword);
        adapter.setItems(items);

        int totalCount = customers.findAll().size();
        tvCustomerCount.setText(totalCount + " khách");

        if (items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            if (!keyword.isEmpty()) {
                tvEmptyTitle.setText(R.string.empty_search_title);
                tvEmptySubtitle.setText(R.string.empty_search_desc);
            } else {
                tvEmptyTitle.setText(R.string.empty_customers_title);
                tvEmptySubtitle.setText(R.string.empty_customers_desc);
            }
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    private void save() {
        String name = Ui.text(edName);
        String phone = Ui.text(edPhone);
        if (name.isEmpty() || phone.isEmpty()) {
            toast(R.string.customer_required);
            return;
        }
        int id = editing == null ? 0 : editing.id();
        int newId = customers.save(new Customer(id, name, phone, Ui.text(edAddress)));
        if (newId <= 0) {
            toast(R.string.save_failed);
            return;
        }
        toast(editing == null ? R.string.customer_added : R.string.customer_updated);
        if (isQuickAdd && editing == null) {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_NEW_CUSTOMER_ID, newId));
            finish();
            return;
        }
        clearForm();
        reload();
    }

    private void startEditing(Customer customer) {
        editing = customer;
        edName.setText(customer.name());
        edPhone.setText(customer.phone());
        edAddress.setText(customer.address());
        tvFormHeader.setText("Cập nhật thông tin khách hàng");
        btnSave.setText(R.string.action_update_customer);
        btnCancel.setVisibility(View.VISIBLE);
        edName.requestFocus();
    }

    private void clearForm() {
        editing = null;
        edName.setText("");
        edPhone.setText("");
        edAddress.setText("");
        tvFormHeader.setText("Thêm khách hàng mới");
        btnSave.setText(R.string.action_save_customer);
        btnCancel.setVisibility(View.GONE);
    }

    private void delete(Customer customer) {
        confirmDelete(getString(R.string.customer_confirm_delete, customer.name()), () -> {
            if (!customers.delete(customer.id())) {
                toast(R.string.customer_in_use);
                return;
            }
            if (editing != null && editing.id() == customer.id()) {
                clearForm();
            }
            toast(R.string.customer_deleted);
            reload();
        });
    }
}
