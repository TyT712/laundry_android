package com.example.laundrymanager.ui;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.CustomerRepository;
import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class CustomerActivity extends BaseActivity {
    public static final String EXTRA_QUICK_ADD = "quick_add";
    public static final String EXTRA_NEW_CUSTOMER_ID = "new_customer_id";

    public static Intent quickAddIntent(Context context) {
        return new Intent(context, CustomerActivity.class).putExtra(EXTRA_QUICK_ADD, true);
    }

    private CustomerRepository customers;
    private EditText edName;
    private EditText edPhone;
    private EditText edAddress;
    private EditText edSearch;
    private Button btnSave;
    private ListView list;
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
        edName = findViewById(R.id.edCustomerName);
        edPhone = findViewById(R.id.edCustomerPhone);
        edAddress = findViewById(R.id.edCustomerAddress);
        edSearch = findViewById(R.id.edSearchCustomer);
        btnSave = findViewById(R.id.btnSaveCustomer);
        list = findViewById(R.id.listCustomers);

        isQuickAdd = getIntent().getBooleanExtra(EXTRA_QUICK_ADD, false);
        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnClearCustomer).setOnClickListener(v -> clearForm());
        findViewById(R.id.btnSearchCustomer).setOnClickListener(v -> reload());
        findViewById(R.id.btnResetCustomerSearch).setOnClickListener(v -> {
            edSearch.setText("");
            reload();
        });
        list.setOnItemClickListener((parent, view, position, id) -> {
            Customer customer = items.get(position);
            showEditDeleteMenu(customer.name(), () -> startEditing(customer), () -> delete(customer));
        });
        reload();
        setupTaskbar(NavTab.CUSTOMERS);
    }

    private void reload() {
        items = customers.search(Ui.text(edSearch));
        list.setAdapter(Ui.listAdapter(this, items, c ->
                getString(R.string.customer_row, c.id(), c.name(), c.phone(), c.address())));
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
        btnSave.setText(R.string.action_update_customer);
    }

    private void clearForm() {
        editing = null;
        edName.setText("");
        edPhone.setText("");
        edAddress.setText("");
        btnSave.setText(R.string.action_save_customer);
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

