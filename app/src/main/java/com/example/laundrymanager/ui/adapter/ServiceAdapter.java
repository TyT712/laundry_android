package com.example.laundrymanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.util.Formats;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    public interface OnServiceActionListener {
        void onEdit(Service service);
        void onDelete(Service service);
    }

    private final List<Service> items = new ArrayList<>();
    private final OnServiceActionListener actionListener;

    public ServiceAdapter(OnServiceActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setItems(List<Service> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        holder.bind(items.get(position), actionListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvServiceName;
        private final TextView tvServicePrice;
        private final ImageButton btnEditService;
        private final ImageButton btnDeleteService;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            btnEditService = itemView.findViewById(R.id.btnEditService);
            btnDeleteService = itemView.findViewById(R.id.btnDeleteService);
        }

        void bind(Service service, OnServiceActionListener listener) {
            tvServiceName.setText(service.name());
            tvServicePrice.setText(Formats.amount(service.price()) + " VNĐ / kg");

            btnEditService.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(service);
                }
            });

            btnDeleteService.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(service);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(service);
                }
            });
        }
    }
}
