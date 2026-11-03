package com.example.laundrymanager.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.laundrymanager.R;
import com.example.laundrymanager.util.Session;

/**
 * Khung chung cho mọi màn hình: kiểm tra quyền truy cập, nạp layout rồi gọi {@link #onReady()}.
 * Màn hình không đủ quyền sẽ tự đóng (hoặc chuyển về đăng nhập nếu chưa đăng nhập).
 */
public abstract class BaseActivity extends Activity {
    public enum NavTab {
        ORDERS, CUSTOMERS, SERVICES, STATS, PROFILE
    }

    protected Session session;
    private boolean ready;

    protected abstract int layoutRes();

    /** Gọi một lần sau khi layout được nạp; khởi tạo view và listener tại đây. */
    protected abstract void onReady();

    /** Gọi mỗi lần màn hình hiện lại (kể cả lần đầu); mặc định không làm gì. */
    protected void onShow() {
    }

    /** Điều kiện được vào màn hình; mặc định chỉ cần đã đăng nhập. */
    protected boolean isAllowed() {
        return session.isLoggedIn();
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(this);
        if (!isAllowed()) {
            if (!session.isLoggedIn()) {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
            return;
        }
        setContentView(layoutRes());
        ready = true;
        onReady();
    }

    @Override
    protected final void onResume() {
        super.onResume();
        if (ready) {
            onShow();
        }
    }

    protected void setupTaskbar(NavTab currentTab) {
        View taskbar = findViewById(R.id.bottomTaskbar);
        if (taskbar == null) {
            return;
        }

        View btnServices = findViewById(R.id.navServices);
        View btnStats = findViewById(R.id.navStats);

        int managerVisibility = session.isManager() ? View.VISIBLE : View.GONE;
        if (btnServices != null) {
            btnServices.setVisibility(managerVisibility);
        }
        if (btnStats != null) {
            btnStats.setVisibility(managerVisibility);
        }

        wireNavButton(R.id.navOrders, currentTab == NavTab.ORDERS, MainActivity.class);
        wireNavButton(R.id.navCustomers, currentTab == NavTab.CUSTOMERS, CustomerActivity.class);
        wireNavButton(R.id.navServices, currentTab == NavTab.SERVICES, ServiceActivity.class);
        wireNavButton(R.id.navStats, currentTab == NavTab.STATS, StatsActivity.class);
        wireNavButton(R.id.navProfile, currentTab == NavTab.PROFILE, ProfileActivity.class);
    }

    private void wireNavButton(int buttonId, boolean isCurrent, Class<?> targetActivity) {
        Button btn = findViewById(buttonId);
        if (btn == null) {
            return;
        }

        if (isCurrent) {
            btn.setTextColor(0xFF0066CC);
            btn.setTypeface(null, Typeface.BOLD);
        } else {
            btn.setTextColor(0xFF555555);
            btn.setTypeface(null, Typeface.NORMAL);
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(this, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                if (!(this instanceof MainActivity)) {
                    finish();
                }
            });
        }
    }

    protected void toast(int messageRes, Object... args) {
        Toast.makeText(this, getString(messageRes, args), Toast.LENGTH_SHORT).show();
    }

    protected void confirmDelete(String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_confirm_delete)
                .setMessage(message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> onConfirm.run())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    protected void showEditDeleteMenu(String title, Runnable onEdit, Runnable onDelete) {
        String[] actions = {getString(R.string.action_edit), getString(R.string.action_delete)};
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(actions, (dialog, which) -> (which == 0 ? onEdit : onDelete).run())
                .show();
    }
}
