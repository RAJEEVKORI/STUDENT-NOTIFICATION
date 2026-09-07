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
 * Login screen for Students.
 * Mirrors DepartmentActivity -- the only difference is the "Student" role
 * passed to AuthManager, which is what tells it which demo username to check.
 */
public class StudentActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        authManager = new AuthManager(this);
        etUsername = findViewById(R.id.etUsername1);
        etPassword = findViewById(R.id.etPassword1);
        Button btLogin = findViewById(R.id.btLogin1);

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

        if (authManager.signIn("Student", username, password)) {
            Intent intent = new Intent(StudentActivity.this, StudentDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
        }
    }
}
