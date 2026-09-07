package com.example.studentdepartment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Lets a user post a new question, optionally attaching images/links, to
 * Firebase under Comments/{timestamp}/question. Structurally this mirrors
 * AnswerActivity closely (same attach/upload flow) -- the main difference
 * is this one also looks up the poster's display name first.
 */
public class PostCommentActivty extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private EditText questionInput;
    private TextView attachmentsLabel;
    private RecyclerView attachmentsRecycler;
    private FloatingActionMenu attachFabMenu;
    private FloatingActionButton pickImageFab;
    private FloatingActionButton takePhotoFab;
    private FloatingActionButton addLinkFab;

    private ProgressDialog progressDialog;
    private final ArrayList<AttachmentModel> attachmentModelArrayList = new ArrayList<>();
    private Uri photoURI;
    private String currentPhotoPath;
    private String posterName = "Guest User"; // fallback shown until/unless a real name is loaded below
    private String postTimestampKey;

    private int uploadedCount = 0;
    private int imageCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment_activty);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Question");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        loadPosterName();

        questionInput = findViewById(R.id.answer);
        attachmentsLabel = findViewById(R.id.attahment_Text);
        attachmentsLabel.setVisibility(View.INVISIBLE);

        attachFabMenu = findViewById(R.id.menu);
        attachFabMenu.setClosedOnTouchOutside(true);
        pickImageFab = findViewById(R.id.menu_item1);
        takePhotoFab = findViewById(R.id.menu_item2);
        addLinkFab = findViewById(R.id.menu_item3);

        attachmentsRecycler = findViewById(R.id.attachments_list);
        attachmentsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        attachmentsRecycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if (attachmentModelArrayList.isEmpty()) {
                    attachmentsLabel.setVisibility(View.INVISIBLE);
                }
            }
        });

        pickImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        takePhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCameraOrRequestPermission();
            }
        });

        addLinkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddLinkDialog();
            }
        });
    }

    /**
     * Looks up the poster's display name from Firebase under UserInfo/{uid}.
     *
     * IMPORTANT: this only works once Firebase Authentication is connected
     * (getCurrentUser() would be null right now, since login is local-only
     * -- see AuthManager). Guarded with a null check so it just falls back
     * to posterName's default instead of crashing until that's wired up.
     */
    private void loadPosterName() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("UserInfo");
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (TextUtils.equals(userSnapshot.getKey(), uid)) {
                        posterName = userSnapshot.child("name").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void launchCameraOrRequestPermission() {
        boolean hasCamera = checkSelfPermission(android.Manifest.permission.CAMERA) == 0;
        boolean hasStorage = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0;
        if (!hasCamera && !hasStorage) {
            requestPermissions(new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_PERMISSION_CODE);
            return;
        }
        openCamera();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            return;
        }
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    private void showAddLinkDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enter Link");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.enter_link, null);
        final TextInputEditText linkField = dialogView.findViewById(R.id.link_url);
        linkField.setHint("Link");
        Button saveBtn = dialogView.findViewById(R.id.saveBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

        dialog.setCancelable(false);
        dialog.setView(dialogView);
        final AlertDialog alertDialog = dialog.create();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = linkField.getText().toString();
                if (!TextUtils.isEmpty(link)) {
                    attachmentModelArrayList.add(new AttachmentModel("Add_Link", link));
                    attachmentsRecycler.setAdapter(new AttachmentAdapter(PostCommentActivty.this, attachmentModelArrayList));
                    attachmentsLabel.setVisibility(View.VISIBLE);
                    attachFabMenu.close(true);
                    alertDialog.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
                handleGalleryResult(data);
            } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                attachmentModelArrayList.add(new AttachmentModel("Take_Image", photoURI.getLastPathSegment(), photoURI));
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
            attachFabMenu.close(true);
            attachmentsRecycler.setAdapter(new AttachmentAdapter(this, attachmentModelArrayList));
            attachmentsLabel.setVisibility(attachmentModelArrayList.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleGalleryResult(Intent data) {
        if (data.getData() != null) {
            Uri imageUri = data.getData();
            attachmentModelArrayList.add(new AttachmentModel("Add_Image", imageUri.getLastPathSegment(), imageUri));
        } else if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            ArrayList<Uri> pickedUris = new ArrayList<>();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                pickedUris.add(uri);
                attachmentModelArrayList.add(new AttachmentModel("Add_Image", uri.getLastPathSegment(), uri));
            }
            Log.v("LOG_TAG", "Selected Images" + pickedUris.size());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CAMERA_PERMISSION_CODE) {
            return;
        }
        boolean granted = grantResults.length > 1 && grantResults[0] == 0 && grantResults[1] == 0;
        if (granted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
            openCamera();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.postButton) {
            postQuestion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void postQuestion() {
        if (TextUtils.isEmpty(questionInput.getText().toString())) {
            Toast.makeText(getApplicationContext(), "No question asked!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        // Used as the database key for this question -- unique enough for
        // a student project since it's down to the second.
        postTimestampKey = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        HashMap<String, Object> questionData = new HashMap<>();
        questionData.put("Question", questionInput.getText().toString());
        questionData.put("Name", posterName);
        questionData.put("Date", new SimpleDateFormat("dd/MM/YYYY").format(new Date()));

        if (attachmentModelArrayList.isEmpty()) {
            saveQuestion(questionData);
            return;
        }

        uploadedCount = 0;
        imageCounter = 0;
        int linkCounter = 0;

        Iterator<AttachmentModel> it = attachmentModelArrayList.iterator();
        while (it.hasNext()) {
            AttachmentModel model = it.next();
            if (TextUtils.equals("Add_Link", model.getType())) {
                linkCounter++;
                questionData.put("link" + linkCounter, model.getUri());
                uploadedCount++;
                if (uploadedCount == attachmentModelArrayList.size()) {
                    saveQuestion(questionData);
                }
            } else {
                uploadImageThenAddToMap(model, questionData);
            }
        }
    }

    private void uploadImageThenAddToMap(AttachmentModel model, final HashMap<String, Object> questionData) {
        final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                .child("Comments").child(model.getName().getLastPathSegment());

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), model.getName());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        filePath.putBytes(baos.toByteArray())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Oops/" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uploadedCount++;
                    imageCounter++;
                    questionData.put("image" + imageCounter, String.valueOf(task.getResult()));
                    if (uploadedCount == attachmentModelArrayList.size()) {
                        saveQuestion(questionData);
                    }
                });
    }

    private void saveQuestion(HashMap<String, Object> questionData) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postTimestampKey).child("question");
        ref.setValue(questionData);
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_SHORT).show();
        finish();
    }
}
