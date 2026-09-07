package com.example.studentdepartment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Read-only "question + answer" screen -- opened from CommentAdapter when a
 * question has already been answered. Loads the question first, then the
 * answer nested under it, and fills in both halves of the screen.
 */
public class AnsQuestionActivity extends AppCompatActivity {

    private String commentKey;

    private TextView questionText;
    private TextView answerText;
    private TextView questionDate;
    private TextView answerDate;
    private TextView askedByName;
    private TextView questionAttachmentLabel;
    private TextView answerAttachmentLabel;
    private TextView questionLinkLabel;
    private TextView answerLinkLabel;
    private RecyclerView questionImages;
    private RecyclerView answerImages;
    private RecyclerView questionLinks;
    private RecyclerView answerLinks;

    private ProgressDialog progressDialog;
    private final CommentModel question = new CommentModel();
    private final CommentModel answer = new CommentModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ans_question);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Answer");

        bindViews();
        setUpRecyclerViews();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait....");
        progressDialog.setCancelable(false);
        progressDialog.show();

        commentKey = getIntent().getStringExtra("Key");
        loadQuestionThenAnswer();
    }

    private void bindViews() {
        questionImages = findViewById(R.id.qa_recycler_image_1);
        answerImages = findViewById(R.id.qa_recycler_image_2);
        questionLinks = findViewById(R.id.qa_recycler_link_1);
        answerLinks = findViewById(R.id.qa_recycler_link_2);
        questionText = findViewById(R.id.qa_text_view_show_more_1);
        answerText = findViewById(R.id.qa_text_view_show_more_2);
        questionDate = findViewById(R.id.qa_date1);
        answerDate = findViewById(R.id.qa_date_2);
        questionAttachmentLabel = findViewById(R.id.qa_attachment_1);
        answerAttachmentLabel = findViewById(R.id.qa_attachment_2);
        questionLinkLabel = findViewById(R.id.qa_links_1);
        answerLinkLabel = findViewById(R.id.qa_links_2);
        askedByName = findViewById(R.id.qa_name);
    }

    private void setUpRecyclerViews() {
        questionImages.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        answerImages.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        questionLinks.setLayoutManager(new LinearLayoutManager(this));
        answerLinks.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Reads "question" first, and once that arrives, reads "answer" nested
     * under the same comment key. Both reads happen once (not live updates)
     * since an already-answered Q&A isn't expected to keep changing.
     */
    private void loadQuestionThenAnswer() {
        FirebaseDatabase.getInstance().getReference()
                .child("Comments").child(commentKey).child("question")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fillModelFromSnapshot(question, dataSnapshot, "Question");
                        loadAnswer();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void loadAnswer() {
        FirebaseDatabase.getInstance().getReference()
                .child("Comments").child(commentKey).child("answer")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fillModelFromSnapshot(answer, dataSnapshot, "Answer");
                        displayQuestionAndAnswer();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }

    /**
     * Shared parsing logic for both the "question" and "answer" nodes --
     * they store the same shape of data (text field, Date, image*, link*),
     * just under a different field name for the main text.
     *
     * @param textFieldName "Question" for the question node, "Answer" for the answer node
     */
    private void fillModelFromSnapshot(CommentModel target, DataSnapshot dataSnapshot, String textFieldName) {
        for (DataSnapshot field : dataSnapshot.getChildren()) {
            String fieldName = field.getKey();
            if (TextUtils.equals(fieldName, "Name")) {
                target.setName(field.getValue().toString());
            } else if (TextUtils.equals(fieldName, textFieldName)) {
                target.setQuestion(field.getValue().toString());
            } else if (TextUtils.equals(fieldName, "Date")) {
                target.setEvent(field.getValue().toString());
            } else if (fieldName.startsWith("image")) {
                target.getImageList().add(field.getValue().toString());
            } else if (fieldName.startsWith("link")) {
                target.getLinkList().add(field.getValue().toString());
            }
        }
    }

    private void displayQuestionAndAnswer() {
        questionText.setText(question.getQuestion());
        answerText.setText(answer.getQuestion());
        questionDate.setText("Posted On : " + question.getEvent());
        answerDate.setText("Answered On : " + answer.getEvent());
        askedByName.setText("Asked By : " + question.getName());

        showLinksOrHide(question.getLinkList(), questionLinkLabel, questionLinks);
        showLinksOrHide(answer.getLinkList(), answerLinkLabel, answerLinks);
        showImagesOrHide(question.getImageList(), questionAttachmentLabel, questionImages);
        showImagesOrHide(answer.getImageList(), answerAttachmentLabel, answerImages);
    }

    private void showLinksOrHide(java.util.ArrayList<String> links, TextView label, RecyclerView recyclerView) {
        if (links.isEmpty()) {
            label.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setAdapter(new LinkAdapter(getApplicationContext(), links));
        }
    }

    private void showImagesOrHide(java.util.ArrayList<String> images, TextView label, RecyclerView recyclerView) {
        if (images.isEmpty()) {
            label.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setAdapter(new ImageAdapter(getApplicationContext(), images));
        }
    }
}
