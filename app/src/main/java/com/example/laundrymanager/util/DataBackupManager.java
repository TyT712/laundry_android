package com.example.laundrymanager.util;

import android.content.Context;
import android.content.Intent;

import com.example.laundrymanager.data.CustomerRepository;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.data.ServiceRepository;
import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.Service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Tiện ích xuất và sao lưu dữ liệu toàn bộ cửa hàng. */
public final class DataBackupManager {

    private DataBackupManager() {
    }

    public static String generateBackupJson(Context context) {
        try {
            JSONObject root = new JSONObject();
            root.put("app", "LaundryManagerAndroid");
            root.put("version", "1.0");
            root.put("exportedAt", Formats.dateTime(System.currentTimeMillis()));

            // Customers
            CustomerRepository customerRepo = new CustomerRepository(context);
            List<Customer> customers = customerRepo.findAll();
            JSONArray customersArray = new JSONArray();
            for (Customer c : customers) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.id());
                obj.put("name", c.name());
                obj.put("phone", c.phone());
                obj.put("address", c.address());
                customersArray.put(obj);
            }
            root.put("customers", customersArray);

            // Services
            ServiceRepository serviceRepo = new ServiceRepository(context);
            List<Service> services = serviceRepo.findAll();
            JSONArray servicesArray = new JSONArray();
            for (Service s : services) {
                JSONObject obj = new JSONObject();
                obj.put("id", s.id());
                obj.put("name", s.name());
                obj.put("price", s.price());
                servicesArray.put(obj);
            }
            root.put("services", servicesArray);

            // Orders
            OrderRepository orderRepo = new OrderRepository(context);
            List<Order> orders = orderRepo.findAll();
            JSONArray ordersArray = new JSONArray();
            for (Order o : orders) {
                JSONObject obj = new JSONObject();
                obj.put("id", o.id());
                obj.put("customerId", o.customer().id());
                obj.put("serviceId", o.service().id());
                obj.put("weight", o.weight());
                obj.put("total", o.total());
                obj.put("note", o.note());
                obj.put("status", o.status().name());
                obj.put("paymentStatus", o.payment().name());
                obj.put("createdAt", o.createdAt());
                obj.put("dueDate", o.dueDate());
                ordersArray.put(obj);
            }
            root.put("orders", ordersArray);

            return root.toString(2);
        } catch (Exception e) {
            return null;
        }
    }

    public static Intent createShareBackupIntent(String json) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String subject = "LaundryManager_Backup_" + timestamp + ".json";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, json);
        return Intent.createChooser(intent, "Sao lưu dữ liệu cửa hàng");
    }
}
