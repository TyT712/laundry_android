package com.example.laundrymanager.ui;

import android.widget.TextView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.OrderRepository;
import com.example.laundrymanager.model.Stats;
import com.example.laundrymanager.util.Formats;

/** Thống kê doanh thu; chỉ dành cho Quản lý. */
public class StatsActivity extends BaseActivity {

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
        Stats stats = new OrderRepository(this).stats();
        TextView result = findViewById(R.id.tvStatsResult);
        result.setText(getString(R.string.stats_body,
                stats.orders(),
                Formats.money(this, stats.revenue()),
                Formats.money(this, stats.paid()),
                Formats.money(this, stats.unpaid()),
                stats.customers(),
                stats.services()));
        findViewById(R.id.btnBackStats).setOnClickListener(v -> finish());
        setupTaskbar(NavTab.STATS);
    }
}

