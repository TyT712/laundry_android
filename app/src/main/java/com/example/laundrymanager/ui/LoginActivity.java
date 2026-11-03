package com.example.laundrymanager.ui;

import android.content.Intent;
import android.widget.EditText;

import com.example.laundrymanager.R;
import com.example.laundrymanager.data.UserRepository;
import com.example.laundrymanager.model.User;
import com.example.laundrymanager.util.Ui;

public class LoginActivity extends BaseActivity {
    private UserRepository users;
    private EditText edUsername;
    private EditText edPassword;

    @Override
    protected int layoutRes() {
        return R.layout.activity_login;
    }

    @Override
    protected boolean isAllowed() {
        return true;
    }

    @Override
    protected void onReady() {
        if (session.isLoggedIn()) {
            openMain();
            return;
        }
        users = new UserRepository(this);
        edUsername = findViewById(R.id.edUsername);
        edPassword = findViewById(R.id.edPassword);
        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
        findViewById(R.id.btnGoRegister)
                .setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = Ui.text(edUsername);
        String password = edPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            toast(R.string.login_missing_fields);
            return;
        }
        User user = users.authenticate(username, password);
        if (user == null) {
            toast(R.string.login_failed);
            return;
        }
        session.start(user);
        toast(R.string.login_success, getString(user.role().label()));
        openMain();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

