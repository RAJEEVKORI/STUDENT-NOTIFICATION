package com.example.studentdepartment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Login screen for Department staff.
 * The actual username/password check happens in AuthManager, not here --
 * this class only handles the screen and shows errors/toasts.
 */
public class DepartmentActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department);

        authManager = new AuthManager(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btLogin = findViewById(R.id.btLogin);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is Required");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is Required");
            etPassword.requestFocus();
            return;
        }

        if (authManager.signIn("Department", username, password)) {
            Intent intent = new Intent(DepartmentActivity.this, DepartmentDashborad.class);
            // Clears the back stack so pressing "back" from the dashboard
            // doesn't return to this login screen.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // Kept as one generic message on purpose -- telling the user
            // specifically which field was wrong makes it easier to guess
            // valid usernames.
            Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
        }
    }
}
