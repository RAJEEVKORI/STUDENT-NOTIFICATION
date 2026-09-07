package com.example.studentdepartment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

/**
 * Home screen for a logged-in Student.
 * Two options: view/post notices, or go to the comments section.
 * The toolbar menu also has a "sign out" item (see onOptionsItemSelected).
 */
public class StudentDashboard extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        authManager = new AuthManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Student");

        CardView noticesCard = findViewById(R.id.newworker);
        noticesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentDashboard.this, NoticeActivity.class));
            }
        });

        CardView commentsCard = findViewById(R.id.rate);
        commentsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentDashboard.this, CommentActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.first, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out1) {
            authManager.signOut();

            Intent intent = new Intent(this, MainActivity.class);
            // Clears the back stack, and "Value" = 1 tells MainActivity not
            // to auto-skip past the role picker (see MainActivity.onCreate).
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("Value", 1);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
