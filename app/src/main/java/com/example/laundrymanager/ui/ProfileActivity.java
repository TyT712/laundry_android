package com.example.laundrymanager.ui;

import android.widget.EditText;
import android.widget.TextView;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.UserRepository;
import com.example.laundrymanager.model.User;
import com.example.laundrymanager.util.Ui;

public class ProfileActivity extends BaseActivity {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private UserRepository users;
    private TextView tvUsername;
    private TextView tvRole;
    private EditText edFullname;
    private EditText edPhone;
    private EditText edPassword;

    @Override
    protected int layoutRes() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onReady() {
        users = new UserRepository(this);
        tvUsername = findViewById(R.id.tvProfileUsername);
        tvRole = findViewById(R.id.tvProfileRole);
        edFullname = findViewById(R.id.edProfileFullname);
        edPhone = findViewById(R.id.edProfilePhone);
        edPassword = findViewById(R.id.edProfilePassword);
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> save());
        findViewById(R.id.btnCloseProfile).setOnClickListener(v -> finish());
        load();
        setupTaskbar(NavTab.PROFILE);
    }

    private void load() {
        User user = users.findById(session.userId());
        if (user == null) {
            finish();
            return;
        }
        tvUsername.setText(getString(R.string.profile_username, user.username()));
        tvRole.setText(getString(R.string.profile_role, getString(user.role().label())));
        edFullname.setText(user.fullname());
        edPhone.setText(user.phone());
    }

    private void save() {
        String fullname = Ui.text(edFullname);
        String newPassword = Ui.text(edPassword);
        if (fullname.isEmpty()) {
            toast(R.string.profile_fullname_required);
            return;
        }
        if (!newPassword.isEmpty() && newPassword.length() < MIN_PASSWORD_LENGTH) {
            toast(R.string.password_too_short, MIN_PASSWORD_LENGTH);
            return;
        }
        if (users.updateProfile(session.userId(), fullname, Ui.text(edPhone), newPassword)) {
            session.updateFullname(fullname);
            edPassword.setText("");
            toast(R.string.profile_saved);
        } else {
            toast(R.string.save_failed);
        }
    }
}

