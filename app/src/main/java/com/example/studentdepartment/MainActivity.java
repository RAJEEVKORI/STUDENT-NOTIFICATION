package com.example.studentdepartment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.rbddevs.splashy.Splashy;

/**
 * First screen the user sees when the app opens.
 *
 * What happens here:
 * 1. Show a splash screen for a few seconds.
 * 2. If AuthManager says someone is already logged in, skip straight to
 *    their dashboard (Student or Department) -- no need to log in again.
 * 3. Otherwise, let the user pick "Student" or "Department" using the two
 *    cards, then tap Next to go to that role's login screen.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvSelectedRole;
    private CardView cardDepartment;
    private CardView cardStudent;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);

        tvSelectedRole = findViewById(R.id.tvLogin);
        cardDepartment = findViewById(R.id.adminid);
        cardStudent = findViewById(R.id.workerid);
        Button nextButton = findViewById(R.id.nextbtn);

        // Tapping a card highlights it and stores the pick in tvSelectedRole's
        // text. A bit unusual to use a TextView as the "selected value" holder,
        // but it's how the layout already reads the choice, so kept as-is.
        cardDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardDepartment.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.lightgray));
                cardStudent.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                tvSelectedRole.setText("Department");
            }
        });

        cardStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStudent.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.lightgray));
                cardDepartment.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                tvSelectedRole.setText("Student");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedRole = tvSelectedRole.getText().toString();
                if (TextUtils.equals(selectedRole, "Login")) {
                    // "Login" is the placeholder text from the XML layout,
                    // meaning no card has been tapped yet.
                    Toast.makeText(getApplicationContext(), "Select a type", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.equals(selectedRole, "Department")) {
                    startActivity(new Intent(MainActivity.this, DepartmentActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, StudentActivity.class));
                }
            }
        });

        // Only auto-skip to a dashboard on a fresh launch of the app, not
        // when we've navigated back here with a specific "Value" extra set.
        int launchValue = getIntent().getIntExtra("Value", 0);
        if (launchValue == 0) {
            showSplashScreen();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (authManager.isLoggedIn()) {
                        boolean isDepartment = TextUtils.equals(authManager.getLoggedInRole(), "Department");
                        Class<?> destination = isDepartment ? DepartmentDashborad.class : StudentDashboard.class;

                        Intent intent = new Intent(MainActivity.this, destination);
                        // Clears the activity history so "back" can't return here.
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }, 3000L);
        }
    }

    private void showSplashScreen() {
        new Splashy(this)
                .setLogo(R.drawable.splashy)
                .setAnimation(Splashy.Animation.SLIDE_IN_TOP_BOTTOM, 800L)
                .setTitle("Major Project")
                .setTitleColor(R.color.black)
                .setSubTitle("App for student department communication")
                .setProgressColor(R.color.black)
                .setBackgroundResource(R.color.white)
                .setFullScreen(true)
                .setTitleFontStyle("fonts/Satisfy-Regular.ttf")
                .setTime(3000L)
                .show();
    }
}
