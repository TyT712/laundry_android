package com.example.laundrymanager.ui;

import android.widget.EditText;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.UserRepository;
import com.example.laundrymanager.util.Ui;

/** Đăng ký tài khoản mới; tài khoản tự đăng ký luôn có vai trò Nhân viên. */
public class RegisterActivity extends BaseActivity {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private UserRepository users;
    private EditText edFullname;
    private EditText edPhone;
    private EditText edUsername;
    private EditText edPassword;

    @Override
    protected int layoutRes() {
        return R.layout.activity_register;
    }

    @Override
    protected boolean isAllowed() {
        return true;
    }

    @Override
    protected void onReady() {
        users = new UserRepository(this);
        edFullname = findViewById(R.id.edRegFullname);
        edPhone = findViewById(R.id.edRegPhone);
        edUsername = findViewById(R.id.edRegUsername);
        edPassword = findViewById(R.id.edRegPassword);
        findViewById(R.id.btnRegisterSubmit).setOnClickListener(v -> register());
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());
    }

    private void register() {
        String fullname = Ui.text(edFullname);
        String username = Ui.text(edUsername);
        String password = Ui.text(edPassword);
        if (fullname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            toast(R.string.register_missing_fields);
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            toast(R.string.password_too_short, MIN_PASSWORD_LENGTH);
            return;
        }
        if (users.registerStaff(username, password, fullname, Ui.text(edPhone))) {
            toast(R.string.register_success);
            finish();
        } else {
            toast(R.string.register_duplicate);
        }
    }
}

