package com.example.laundrymanager.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.OrderStatus;
import com.example.laundrymanager.model.PaymentStatus;
import com.example.laundrymanager.util.Formats;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnItemClickListener {
        void onClick(Order order);
    }

    private final List<Order> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public OrderAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Order> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvOrderDate;
        private final TextView tvCustomerName;
        private final TextView tvCustomerPhone;
        private final TextView tvServiceAndWeight;
        private final TextView tvOrderTotal;
        private final TextView tvOrderDueDate;
        private final TextView tvOverdueBadge;
        private final TextView tvOrderNote;
        private final TextView tvOrderStatusBadge;
        private final TextView tvPaymentStatusBadge;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvServiceAndWeight = itemView.findViewById(R.id.tvServiceAndWeight);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderDueDate = itemView.findViewById(R.id.tvOrderDueDate);
            tvOverdueBadge = itemView.findViewById(R.id.tvOverdueBadge);
            tvOrderNote = itemView.findViewById(R.id.tvOrderNote);
            tvOrderStatusBadge = itemView.findViewById(R.id.tvOrderStatusBadge);
            tvPaymentStatusBadge = itemView.findViewById(R.id.tvPaymentStatusBadge);
        }

        void bind(Order order, OnItemClickListener listener) {
            Context context = itemView.getContext();

            tvOrderId.setText("#" + order.id());
            tvOrderDate.setText(Formats.dateTime(order.createdAt()));
            tvCustomerName.setText(order.customer().name());
            tvCustomerPhone.setText(order.customer().phone());
            tvServiceAndWeight.setText(order.service().name() + " • " + Formats.weight(order.weight()) + " kg");
            tvOrderTotal.setText(Formats.money(context, order.total()));

            // Hẹn trả đồ & Cảnh báo quá hạn
            if (order.dueDate() > 0) {
                tvOrderDueDate.setText(context.getString(R.string.due_date_prefix, Formats.dateTime(order.dueDate())));
                tvOrderDueDate.setVisibility(View.VISIBLE);
            } else {
                tvOrderDueDate.setVisibility(View.GONE);
            }

            if (order.isOverdue()) {
                tvOverdueBadge.setVisibility(View.VISIBLE);
                tvOrderDueDate.setTextColor(ContextCompat.getColor(context, R.color.danger));
            } else {
                tvOverdueBadge.setVisibility(View.GONE);
                tvOrderDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            String note = order.note();
            if (note != null && !note.trim().isEmpty()) {
                tvOrderNote.setVisibility(View.VISIBLE);
                tvOrderNote.setText("Ghi chú: " + note.trim());
            } else {
                tvOrderNote.setVisibility(View.GONE);
            }

            bindOrderStatus(context, order.status());
            bindPaymentStatus(context, order.payment());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(order);
                }
            });
        }

        private void bindOrderStatus(Context context, OrderStatus status) {
            tvOrderStatusBadge.setText(context.getString(status.label()));
            int bgColorRes;
            int textColorRes;

            switch (status) {
                case RECEIVED:
                    bgColorRes = R.color.status_received_bg;
                    textColorRes = R.color.status_received_text;
                    break;
                case WASHING:
                    bgColorRes = R.color.status_washing_bg;
                    textColorRes = R.color.status_washing_text;
                    break;
                case DRYING:
                    bgColorRes = R.color.status_drying_bg;
                    textColorRes = R.color.status_drying_text;
                    break;
                case DONE:
                    bgColorRes = R.color.status_done_bg;
                    textColorRes = R.color.status_done_text;
                    break;
                case DELIVERED:
                default:
                    bgColorRes = R.color.status_delivered_bg;
                    textColorRes = R.color.status_delivered_text;
                    break;
            }

            tvOrderStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, bgColorRes)));
            tvOrderStatusBadge.setTextColor(ContextCompat.getColor(context, textColorRes));
        }

        private void bindPaymentStatus(Context context, PaymentStatus payment) {
            tvPaymentStatusBadge.setText(context.getString(payment.label()));
            int bgColorRes = payment == PaymentStatus.PAID ? R.color.payment_paid_bg : R.color.payment_unpaid_bg;
            int textColorRes = payment == PaymentStatus.PAID ? R.color.payment_paid_text : R.color.payment_unpaid_text;

            tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, bgColorRes)));
            tvPaymentStatusBadge.setTextColor(ContextCompat.getColor(context, textColorRes));
        }
    }
}
