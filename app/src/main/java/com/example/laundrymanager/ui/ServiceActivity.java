package com.example.laundrymanager.ui;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.ServiceRepository;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.util.Formats;
import com.example.laundrymanager.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Quản lý danh mục dịch vụ; chỉ dành cho Quản lý. */
public class ServiceActivity extends BaseActivity {
    private ServiceRepository services;
    private EditText edName;
    private EditText edPrice;
    private Button btnSave;
    private ListView list;
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
        list = findViewById(R.id.listServices);

        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnClearService).setOnClickListener(v -> clearForm());
        list.setOnItemClickListener((parent, view, position, id) -> {
            Service service = items.get(position);
            showEditDeleteMenu(service.name(), () -> startEditing(service), () -> delete(service));
        });
        reload();
        setupTaskbar(NavTab.SERVICES);
    }

    private void reload() {
        items = services.findAll();
        list.setAdapter(Ui.listAdapter(this, items, s ->
                getString(R.string.service_label, s.name(), Formats.amount(s.price()))));
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
        btnSave.setText(R.string.action_update_service);
    }

    private void clearForm() {
        editing = null;
        edName.setText("");
        edPrice.setText("");
        btnSave.setText(R.string.action_save_service);
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

