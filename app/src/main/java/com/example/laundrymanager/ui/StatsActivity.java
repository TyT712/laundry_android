package com.example.laundrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.util.Pair;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.model.DetailedStats;
import com.example.laundrymanager.util.Formats;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Thống kê doanh thu và báo cáo phân tích nâng cao; chỉ dành cho Quản lý. */
public class StatsActivity extends BaseActivity {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private OrderRepository orderRepository;

    private ChipGroup chipGroupTime;
    private TextView tvDateRange;
    private TextView tvRevenue;
    private TextView tvPaid;
    private TextView tvUnpaid;
    private TextView tvOrders;
    private TextView tvWeight;
    private TextView tvCustomers;
    private TextView tvServices;
    private LinearLayout layoutServiceBreakdown;
    private TextView tvNoServiceData;

    private long filterFromMillis = 0;
    private long filterToMillis = 0;
    private String filterLabel = "Toàn thời gian (Tất cả đơn hàng)";

    @Override
    protected int layoutRes() {
        return R.layout.activity_stats;
    }

    @Override
    protected boolean isAllowed() {
        return session.isManager();
    }

    @Override
    protected void onReady() {
        orderRepository = new OrderRepository(this);

        chipGroupTime = findViewById(R.id.chipGroupStatsTime);
        tvDateRange = findViewById(R.id.tvStatsDateRange);
        tvRevenue = findViewById(R.id.tvStatRevenue);
        tvPaid = findViewById(R.id.tvStatPaid);
        tvUnpaid = findViewById(R.id.tvStatUnpaid);
        tvOrders = findViewById(R.id.tvStatOrders);
        tvWeight = findViewById(R.id.tvStatWeight);
        tvCustomers = findViewById(R.id.tvStatCustomers);
        tvServices = findViewById(R.id.tvStatServices);
        layoutServiceBreakdown = findViewById(R.id.layoutServiceBreakdown);
        tvNoServiceData = findViewById(R.id.tvNoServiceData);

        findViewById(R.id.btnBackStats).setOnClickListener(v -> finish());

        chipGroupTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipStatsToday) {
                applyTodayFilter();
            } else if (checkedId == R.id.chipStats7Days) {
                apply7DaysFilter();
            } else if (checkedId == R.id.chipStatsMonth) {
                applyMonthFilter();
            } else if (checkedId == R.id.chipStatsCustom) {
                showCustomDateRangePicker();
                return;
            } else {
                applyAllTimeFilter();
            }
            loadStats();
        });

        applyAllTimeFilter();
        loadStats();
        setupTaskbar(NavTab.STATS);
    }

    private void applyAllTimeFilter() {
        filterFromMillis = 0;
        filterToMillis = 0;
        filterLabel = "Khoảng thời gian: Toàn bộ lịch sử hoạt động";
    }

    private void applyTodayFilter() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        filterFromMillis = c.getTimeInMillis();

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        filterToMillis = c.getTimeInMillis();

        filterLabel = "Hôm nay: " + DATE_FORMAT.format(new Date(filterFromMillis));
    }

    private void apply7DaysFilter() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        filterToMillis = c.getTimeInMillis();

        c.add(Calendar.DAY_OF_YEAR, -6);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        filterFromMillis = c.getTimeInMillis();

        filterLabel = "7 ngày qua: " + DATE_FORMAT.format(new Date(filterFromMillis)) + " — " + DATE_FORMAT.format(new Date(filterToMillis));
    }

    private void applyMonthFilter() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        filterFromMillis = c.getTimeInMillis();

        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        filterToMillis = c.getTimeInMillis();

        filterLabel = "Tháng này: " + DATE_FORMAT.format(new Date(filterFromMillis)) + " — " + DATE_FORMAT.format(new Date(filterToMillis));
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Chọn khoảng ngày thống kê")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                filterFromMillis = selection.first;
                // Đến hết ngày của mốc kết thúc (23:59:59)
                filterToMillis = selection.second + (24 * 3600 * 1000L - 1);
                filterLabel = "Tùy chọn: " + DATE_FORMAT.format(new Date(filterFromMillis)) + " — " + DATE_FORMAT.format(new Date(filterToMillis));
                loadStats();
            }
        });

        picker.addOnNegativeButtonClickListener(dialog -> {
            // Nếu hủy thì quay lại "Toàn thời gian"
            chipGroupTime.check(R.id.chipStatsAll);
        });

        picker.show(getSupportFragmentManager(), "STATS_DATE_RANGE");
    }

    private void loadStats() {
        tvDateRange.setText(filterLabel);

        DetailedStats stats = orderRepository.detailedStats(filterFromMillis, filterToMillis);

        tvRevenue.setText(Formats.money(this, stats.revenue()));
        tvPaid.setText(Formats.money(this, stats.paid()));
        tvUnpaid.setText(Formats.money(this, stats.unpaid()));
        tvOrders.setText(stats.orders() + " đơn");
        tvWeight.setText(Formats.weight(stats.totalWeight()) + " kg");
        tvCustomers.setText(String.valueOf(stats.customersCount()));
        tvServices.setText(String.valueOf(stats.servicesCount()));

        // Nạp danh sách cơ cấu doanh thu theo dịch vụ
        layoutServiceBreakdown.removeAllViews();
        if (stats.serviceBreakdown().isEmpty()) {
            tvNoServiceData.setVisibility(View.VISIBLE);
        } else {
            tvNoServiceData.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);
            double maxRevenue = stats.revenue() > 0 ? stats.revenue() : 1;

            for (DetailedStats.ServiceStat s : stats.serviceBreakdown()) {
                View itemView = inflater.inflate(R.layout.item_stat_service, layoutServiceBreakdown, false);

                TextView tvName = itemView.findViewById(R.id.tvStatServiceName);
                TextView tvDetails = itemView.findViewById(R.id.tvStatServiceDetails);
                TextView tvAmount = itemView.findViewById(R.id.tvStatServiceRevenue);
                ProgressBar pbRatio = itemView.findViewById(R.id.pbStatServiceRatio);

                int percent = (int) Math.round((s.totalRevenue() / maxRevenue) * 100);

                tvName.setText(s.serviceName());
                tvDetails.setText(s.orderCount() + " đơn • " + Formats.weight(s.totalWeight()) + " kg • " + percent + "% doanh thu");
                tvAmount.setText(Formats.money(this, s.totalRevenue()));
                pbRatio.setProgress(Math.max(4, percent)); // Ít nhất 4% để thanh bar luôn hiển thị một chút màu

                layoutServiceBreakdown.addView(itemView);
            }
        }
    }
}
