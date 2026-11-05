package com.example.laundrymanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.model.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    public interface OnItemClickListener {
        void onClick(Customer customer);
    }

    public interface OnCallClickListener {
        void onCall(Customer customer);
    }

    public interface OnSmsClickListener {
        void onSms(Customer customer);
    }

    private final List<Customer> items = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnCallClickListener callListener;
    private final OnSmsClickListener smsListener;

    public CustomerAdapter(OnItemClickListener clickListener, OnCallClickListener callListener, OnSmsClickListener smsListener) {
        this.clickListener = clickListener;
        this.callListener = callListener;
        this.smsListener = smsListener;
    }

    public void setItems(List<Customer> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener, callListener, smsListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatarLetter;
        private final TextView tvCustomerName;
        private final TextView tvCustomerPhone;
        private final TextView tvCustomerAddress;
        private final ImageButton btnCallCustomer;
        private final ImageButton btnSmsCustomer;

        CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tvAvatarLetter);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            btnCallCustomer = itemView.findViewById(R.id.btnCallCustomer);
            btnSmsCustomer = itemView.findViewById(R.id.btnSmsCustomer);
        }

        void bind(Customer customer, OnItemClickListener clickListener, OnCallClickListener callListener, OnSmsClickListener smsListener) {
            String name = customer.name() != null ? customer.name().trim() : "";
            tvCustomerName.setText(name);

            String initial = "?";
            if (!name.isEmpty()) {
                String[] parts = name.split("\\s+");
                String lastWord = parts[parts.length - 1];
                if (!lastWord.isEmpty()) {
                    initial = lastWord.substring(0, 1).toUpperCase();
                }
            }
            tvAvatarLetter.setText(initial);

            tvCustomerPhone.setText(customer.phone());

            String address = customer.address();
            if (address != null && !address.trim().isEmpty()) {
                tvCustomerAddress.setText(address.trim());
                tvCustomerAddress.setVisibility(View.VISIBLE);
            } else {
                tvCustomerAddress.setText("Chưa cập nhật địa chỉ");
                tvCustomerAddress.setVisibility(View.VISIBLE);
            }

            btnCallCustomer.setOnClickListener(v -> {
                if (callListener != null) {
                    callListener.onCall(customer);
                }
            });

            btnSmsCustomer.setOnClickListener(v -> {
                if (smsListener != null) {
                    smsListener.onSms(customer);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onClick(customer);
                }
            });
        }
    }
}
