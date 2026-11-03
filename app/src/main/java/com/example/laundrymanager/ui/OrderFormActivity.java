package com.example.laundrymanager.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.CustomerRepository;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.data.ServiceRepository;
import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.OrderStatus;
import com.example.laundrymanager.model.PaymentStatus;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.util.Formats;
import com.example.laundrymanager.util.Ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Form đơn giặt dùng chung cho hai chế độ: tạo đơn mới (không có extra) và
 * xem chi tiết / sửa / xóa đơn (có {@link #EXTRA_ORDER_ID}).
 */
public class OrderFormActivity extends BaseActivity {
    private static final String EXTRA_ORDER_ID = "order_id";
    private static final int REQUEST_QUICK_ADD_CUSTOMER = 101;

    public static Intent editIntent(Context context, int orderId) {
        return new Intent(context, OrderFormActivity.class).putExtra(EXTRA_ORDER_ID, orderId);
    }

    private OrderRepository orders;
    private CustomerRepository customerRepository;
    private List<Customer> customers;
    private List<Service> services;
    private Order editing;

    private Spinner spCustomer;
    private Spinner spService;
    private Spinner spStatus;
    private Spinner spPayment;
    private EditText edWeight;
    private EditText edNote;
    private TextView tvTotal;

    @Override
    protected int layoutRes() {
        return R.layout.activity_order_form;
    }

    @Override
    protected void onReady() {
        orders = new OrderRepository(this);
        customerRepository = new CustomerRepository(this);
        spCustomer = findViewById(R.id.spOrderCustomer);
        spService = findViewById(R.id.spOrderService);
        spStatus = findViewById(R.id.spOrderStatus);
        spPayment = findViewById(R.id.spOrderPayment);
        edWeight = findViewById(R.id.edOrderWeight);
        edNote = findViewById(R.id.edOrderNote);
        tvTotal = findViewById(R.id.tvOrderTotal);

        spStatus.setAdapter(Ui.enumAdapter(this, OrderStatus.values()));
        spPayment.setAdapter(Ui.enumAdapter(this, PaymentStatus.values()));

        services = new ServiceRepository(this).findAll();
        spService.setAdapter(Ui.spinnerAdapter(this, services, s ->
                getString(R.string.service_label, s.name(), Formats.amount(s.price()))));
        showCustomers(0);

        Ui.onItemSelected(spService, this::updateTotal);
        Ui.onTextChanged(edWeight, this::updateTotal);
        findViewById(R.id.btnQuickAddCustomer)
                .setOnClickListener(v -> startActivityForResult(CustomerActivity.quickAddIntent(this), REQUEST_QUICK_ADD_CUSTOMER));
        findViewById(R.id.btnSaveOrder).setOnClickListener(v -> save());
        findViewById(R.id.btnDeleteOrder).setOnClickListener(v -> delete());
        findViewById(R.id.btnCancelOrder).setOnClickListener(v -> finish());

        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, 0);
        if (orderId != 0) {
            editing = orders.find(orderId);
            if (editing == null) {
                finish();
                return;
            }
            showOrder(editing);
        }
        updateTotal();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QUICK_ADD_CUSTOMER && resultCode == RESULT_OK && data != null) {
            int newCustomerId = data.getIntExtra(CustomerActivity.EXTRA_NEW_CUSTOMER_ID, 0);
            if (newCustomerId > 0) {
                showCustomers(newCustomerId);
            }
        }
    }

    /** Nạp lại danh sách khách hàng (giữ nguyên lựa chọn hiện tại nếu có). */
    @Override
    protected void onShow() {
        Customer selected = getSelectedCustomer();
        showCustomers(selected == null ? 0 : selected.id());
    }

    private void showCustomers(int selectedId) {
        customers = customerRepository.findAll();
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.select_customer_prompt));
        int targetPosition = 0;
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            labels.add(getString(R.string.customer_label, c.name(), c.phone()));
            if (c.id() == selectedId) {
                targetPosition = i + 1;
            }
        }
        spCustomer.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
        spCustomer.setSelection(targetPosition);
    }

    private Customer getSelectedCustomer() {
        int pos = spCustomer.getSelectedItemPosition();
        if (pos > 0 && pos <= customers.size()) {
            return customers.get(pos - 1);
        }
        return null;
    }

    private void showOrder(Order order) {
        ((TextView) findViewById(R.id.tvOrderTitle)).setText(R.string.order_edit_title);
        TextView info = findViewById(R.id.tvOrderInfo);
        info.setText(getString(R.string.order_info, order.id(), Formats.dateTime(order.createdAt())));
        info.setVisibility(View.VISIBLE);
        findViewById(R.id.groupOrderStatus).setVisibility(View.VISIBLE);
        findViewById(R.id.btnDeleteOrder).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.btnSaveOrder)).setText(R.string.action_save_order);

        showCustomers(order.customer().id());
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).id() == order.service().id()) {
                spService.setSelection(i);
                break;
            }
        }
        spStatus.setSelection(order.status().ordinal());
        spPayment.setSelection(order.payment().ordinal());
        edWeight.setText(Ui.plainNumber(order.weight()));
        edNote.setText(order.note());
    }

    private void updateTotal() {
        Service service = Ui.selected(spService, services);
        Double weight = Ui.positiveNumber(Ui.text(edWeight));
        double total = service == null || weight == null ? 0 : service.priceFor(weight);
        tvTotal.setText(getString(R.string.order_total, Formats.money(this, total)));
    }

    private void save() {
        Customer customer = getSelectedCustomer();
        Service service = Ui.selected(spService, services);
        if (customers.isEmpty()) {
            toast(R.string.order_no_customers);
            return;
        }
        if (customer == null) {
            toast(R.string.order_customer_required);
            return;
        }
        if (service == null) {
            toast(R.string.order_no_services);
            return;
        }
        if (Ui.text(edWeight).isEmpty()) {
            toast(R.string.order_weight_required);
            return;
        }
        Double weight = Ui.positiveNumber(Ui.text(edWeight));
        if (weight == null) {
            toast(R.string.order_weight_invalid);
            return;
        }
        Order order = new Order(
                editing == null ? 0 : editing.id(),
                customer,
                service,
                weight,
                service.priceFor(weight),
                Ui.text(edNote),
                Ui.selected(spStatus, OrderStatus.values()),
                Ui.selected(spPayment, PaymentStatus.values()),
                editing == null ? System.currentTimeMillis() : editing.createdAt());
        if (!orders.save(order)) {
            toast(R.string.save_failed);
            return;
        }
        toast(editing == null ? R.string.order_created : R.string.order_updated);
        finish();
    }

    private void delete() {
        confirmDelete(getString(R.string.order_confirm_delete, editing.id()), () -> {
            if (orders.delete(editing.id())) {
                toast(R.string.order_deleted);
                finish();
            }
        });
    }
}
