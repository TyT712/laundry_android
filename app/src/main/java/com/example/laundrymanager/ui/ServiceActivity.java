package com.example.laundrymanager.ui;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.ServiceRepository;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.ui.adapter.ServiceAdapter;
import com.example.laundrymanager.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Quản lý danh mục dịch vụ; chỉ dành cho Quản lý. */
public class ServiceActivity extends BaseActivity {
    private ServiceRepository services;
    private EditText edName;
    private EditText edPrice;
    private Button btnSave;
    private Button btnCancel;
    private TextView tvHeader;
    private TextView tvCount;

    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private View emptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    private List<Service> items = new ArrayList<>();
    private Service editing;

    @Override
    protected int layoutRes() {
        return R.layout.activity_service;
    }

    @Override
    protected boolean isAllowed() {
        return session.isManager();
    }

    @Override
    protected void onReady() {
        services = new ServiceRepository(this);
        edName = findViewById(R.id.edServiceName);
        edPrice = findViewById(R.id.edServicePrice);
        btnSave = findViewById(R.id.btnSaveService);
        btnCancel = findViewById(R.id.btnClearService);
        tvHeader = findViewById(R.id.tvServiceFormHeader);
        tvCount = findViewById(R.id.tvServiceCount);
        rvServices = findViewById(R.id.rvServices);
        emptyState = findViewById(R.id.emptyStateServices);
        tvEmptyTitle = emptyState.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = emptyState.findViewById(R.id.tvEmptySubtitle);

        rvServices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(new ServiceAdapter.OnServiceActionListener() {
            @Override
            public void onEdit(Service service) {
                startEditing(service);
            }

            @Override
            public void onDelete(Service service) {
                delete(service);
            }
        });
        rvServices.setAdapter(adapter);

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> clearForm());

        reload();
        setupTaskbar(NavTab.SERVICES);
    }

    private void reload() {
        items = services.findAll();
        adapter.setItems(items);
        tvCount.setText(items.size() + " dịch vụ");

        if (items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            tvEmptyTitle.setText(R.string.empty_services_title);
            tvEmptySubtitle.setText(R.string.empty_services_desc);
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    private void save() {
        String name = Ui.text(edName);
        if (name.isEmpty() || Ui.text(edPrice).isEmpty()) {
            toast(R.string.service_required);
            return;
        }
        Double price = Ui.positiveNumber(Ui.text(edPrice));
        if (price == null) {
            toast(R.string.service_invalid_price);
            return;
        }
        int id = editing == null ? 0 : editing.id();
        if (!services.save(new Service(id, name, price))) {
            toast(R.string.service_duplicate);
            return;
        }
        toast(editing == null ? R.string.service_added : R.string.service_updated);
        clearForm();
        reload();
    }

    private void startEditing(Service service) {
        editing = service;
        edName.setText(service.name());
        edPrice.setText(Ui.plainNumber(service.price()));
        tvHeader.setText("Cập nhật dịch vụ");
        btnSave.setText(R.string.action_update_service);
        btnCancel.setVisibility(View.VISIBLE);
        edName.requestFocus();
    }

    private void clearForm() {
        editing = null;
        edName.setText("");
        edPrice.setText("");
        tvHeader.setText("Thêm dịch vụ mới");
        btnSave.setText(R.string.action_save_service);
        btnCancel.setVisibility(View.GONE);
    }

    private void delete(Service service) {
        confirmDelete(getString(R.string.service_confirm_delete, service.name()), () -> {
            if (!services.delete(service.id())) {
                toast(R.string.service_in_use);
                return;
            }
            if (editing != null && editing.id() == service.id()) {
                clearForm();
            }
            toast(R.string.service_deleted);
            reload();
        });
    }
}
