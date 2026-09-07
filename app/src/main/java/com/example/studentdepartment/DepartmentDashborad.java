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
 * Home screen for a logged-in Department user.
 * Mirrors StudentDashboard -- same two options (notices, comments) and the
 * same sign-out flow, just with the Department title on the toolbar.
 */
public class DepartmentDashborad extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_dashborad);

        authManager = new AuthManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Department");

        CardView noticesCard = findViewById(R.id.newworker);
        noticesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DepartmentDashborad.this, NoticeActivity.class));
            }
        });

        CardView commentsCard = findViewById(R.id.rate);
        commentsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DepartmentDashborad.this, CommentActivity.class));
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("Value", 1);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
