package com.example.studentdepartment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * "Queries" screen -- shows every question/comment posted so far, read live
 * from Firebase Realtime Database under the "Comments" node. The "+" menu
 * button opens PostCommentActivty to ask a new question.
 *
 * NOTE: this screen already reads/writes through Firebase Realtime Database
 * (that's separate from the login system, which is still local-only for
 * now -- see AuthManager). To actually run this screen you'd need a
 * Firebase project connected via google-services.json.
 */
public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private ProgressDialog progressDialog;
    private ArrayList<CommentModel> commentModelArrayList;
    private DatabaseReference commentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Queries");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        recyclerView = findViewById(R.id.comment_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        emptyStateText = findViewById(R.id.imp);
        commentModelArrayList = new ArrayList<>();

        commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadComments(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Not handled -- if the read fails, the progress dialog just
                // stays up. Worth adding a real error message here later.
            }
        });
    }

    /** Turns the raw Firebase snapshot into a list of CommentModel objects. */
    private void loadComments(DataSnapshot dataSnapshot) {
        commentModelArrayList = new ArrayList<>();

        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
            CommentModel commentModel = new CommentModel();
            commentModel.setKey(commentSnapshot.getKey());
            commentModel.setAnswered(commentSnapshot.child("answer").exists());

            for (DataSnapshot field : commentSnapshot.child("question").getChildren()) {
                String fieldName = field.getKey();
                if (TextUtils.equals(fieldName, "Name")) {
                    commentModel.setName(field.getValue().toString());
                } else if (TextUtils.equals(fieldName, "Question")) {
                    commentModel.setQuestion(field.getValue().toString());
                } else if (TextUtils.equals(fieldName, "Date")) {
                    commentModel.setEvent(field.getValue().toString());
                } else if (fieldName.startsWith("image")) {
                    commentModel.getImageList().add(field.getValue().toString());
                } else if (fieldName.startsWith("link")) {
                    commentModel.getLinkList().add(field.getValue().toString());
                }
            }
            commentModelArrayList.add(commentModel);
        }

        emptyStateText.setVisibility(commentModelArrayList.isEmpty() ? View.VISIBLE : View.INVISIBLE);

        recyclerView.setAdapter(new CommentAdapter(getApplicationContext(), commentModelArrayList));
        progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ask_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ask_btn) {
            startActivity(new Intent(this, PostCommentActivty.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
