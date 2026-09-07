package com.example.studentdepartment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Notice board -- lists every notice, newest at the bottom (reverse layout,
 * stacked from the end). Department users get a "+" button to add a new
 * notice, and can long-press an existing one to edit or delete it. Students
 * get a read-only view (the "+" button is hidden for them).
 *
 * Notices are read live via FirebaseUI's FirebaseRecyclerAdapter, which
 * keeps the list in sync automatically as the "Notices" node changes.
 */
public class NoticeActivity extends AppCompatActivity {

    private RecyclerView historyList;
    private FloatingActionButton fab;
    private ProgressDialog progressDialog;
    private DatabaseReference noticesRef;
    private ImageView profileImage;
    private Uri pickedImageUri;
    private String currentUserRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notices");

        SharedPreferences prefs = getSharedPreferences("admin_detail", 0);
        currentUserRole = prefs.getString("User", "");

        historyList = findViewById(R.id.historyList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        historyList.setLayoutManager(layoutManager);
        historyList.setHasFixedSize(true);

        fab = findViewById(R.id.add_fab_btn);
        if (TextUtils.equals(currentUserRole, "Student")) {
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddNoticeDialog();
            }
        });

        noticesRef = FirebaseDatabase.getInstance().getReference().child("Notices");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        loadList();
    }

    private void loadList() {
        FirebaseRecyclerOptions<NoticeModel> options =
                new FirebaseRecyclerOptions.Builder<NoticeModel>().setQuery(noticesRef, NoticeModel.class).build();

        FirebaseRecyclerAdapter<NoticeModel, ItemViewHolder> adapter =
                new FirebaseRecyclerAdapter<NoticeModel, ItemViewHolder>(options) {
                    @Override
                    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.our_notice, parent, false);
                        return new ItemViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(ItemViewHolder holder, int position, final NoticeModel model) {
                        final String key = getRef(position).getKey();
                        holder.setTitle(model.getTitle());
                        holder.setDescription(model.getDescription());
                        holder.setDateofnotice(model.getDateofnotice());
                        holder.setItemImage(model.getImage(), getApplicationContext());

                        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                if (TextUtils.equals(currentUserRole, "Department")) {
                                    showEditNoticeDialog(model.getTitle(), model.getDescription(), model.getDateofnotice(), model.getImage(), key);
                                }
                                return false;
                            }
                        });
                    }

                    @Override
                    public void onDataChanged() {
                        super.onDataChanged();
                        progressDialog.dismiss();
                    }
                };
        adapter.startListening();
        historyList.setAdapter(adapter);
    }

    private void showAddNoticeDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Add Notice");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_notice_dialog_layout, null);
        final TextInputEditText titleField = dialogView.findViewById(R.id.name);
        final TextInputEditText descriptionField = dialogView.findViewById(R.id.company);
        final TextInputEditText dateField = dialogView.findViewById(R.id.session);
        titleField.setHint("Title");
        descriptionField.setHint("Description");
        dateField.setHint("Date of Notice");

        Button saveBtn = dialogView.findViewById(R.id.saveBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        profileImage = dialogView.findViewById(R.id.image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromGallery();
            }
        });

        dialog.setCancelable(false);
        dialog.setView(dialogView);
        final AlertDialog alertDialog = dialog.create();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!allFieldsFilled(titleField, descriptionField, dateField) || pickedImageUri == null || Uri.EMPTY.equals(pickedImageUri)) {
                    Toast.makeText(getApplicationContext(), "Please Fill All Fields...", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                uploadImage(pickedImageUri, "Notices", downloadUrl -> {
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("title", titleField.getText().toString());
                    fields.put("description", descriptionField.getText().toString());
                    fields.put("dateofnotice", dateField.getText().toString());
                    fields.put("image", String.valueOf(downloadUrl));

                    String newKey = noticesRef.push().getKey();
                    noticesRef.child(newKey).updateChildren(fields).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        alertDialog.dismiss();
                        pickedImageUri = null;
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Added!!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Oops/" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
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

    /**
     * NOTE: this dialog originally wrote to the fields "name"/"company"/
     * "session" -- which don't match NoticeModel (title/description/
     * dateofnotice) or the Add dialog above. That meant editing a notice's
     * text silently failed to update what was shown, since those fields
     * were never read back anywhere. Fixed here to use the same field
     * names as Add, so editing actually works.
     */
    private void showEditNoticeDialog(String title, String description, String dateOfNotice, String image, final String key) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Edit Notice");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_our_star_dialog_layout, null);
        final TextInputEditText titleField = dialogView.findViewById(R.id.name);
        final TextInputEditText descriptionField = dialogView.findViewById(R.id.company);
        final TextInputEditText dateField = dialogView.findViewById(R.id.session);
        titleField.setHint("Title");
        descriptionField.setHint("Description");
        dateField.setHint("Date of Notice");
        titleField.setText(title);
        descriptionField.setText(description);
        dateField.setText(dateOfNotice);

        ImageView deleteBtn = dialogView.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDeleteNotice(key);
            }
        });

        Button saveBtn = dialogView.findViewById(R.id.saveBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        profileImage = dialogView.findViewById(R.id.image);
        Glide.with(getApplicationContext()).load(image).into(profileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromGallery();
            }
        });

        dialog.setCancelable(false);
        dialog.setView(dialogView);
        final AlertDialog alertDialog = dialog.create();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!allFieldsFilled(titleField, descriptionField, dateField)) {
                    Toast.makeText(getApplicationContext(), "Please Fill All Fields...", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean imageChanged = pickedImageUri != null && !Uri.EMPTY.equals(pickedImageUri);
                if (imageChanged) {
                    progressDialog.show();
                    uploadImage(pickedImageUri, "Notices", downloadUrl -> {
                        HashMap<String, Object> fields = new HashMap<>();
                        fields.put("title", titleField.getText().toString());
                        fields.put("description", descriptionField.getText().toString());
                        fields.put("dateofnotice", dateField.getText().toString());
                        fields.put("image", String.valueOf(downloadUrl));
                        saveEditedFields(key, fields, alertDialog);
                    });
                } else {
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("title", titleField.getText().toString());
                    fields.put("description", descriptionField.getText().toString());
                    fields.put("dateofnotice", dateField.getText().toString());
                    saveEditedFields(key, fields, alertDialog);
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

    private void saveEditedFields(String key, HashMap<String, Object> fields, AlertDialog alertDialog) {
        noticesRef.child(key).updateChildren(fields).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            alertDialog.dismiss();
            pickedImageUri = null;
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Edited!!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Oops/" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Uploads a picked image to Storage under {folder}/ and hands back its download URL. */
    private void uploadImage(Uri imageUri, String folder, com.google.android.gms.tasks.OnSuccessListener<Uri> onDownloadUrlReady) {
        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child(folder).child(imageUri.getLastPathSegment());

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Something went wrong reading that image", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

        filePath.putBytes(baos.toByteArray())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onDownloadUrlReady.onSuccess(task.getResult());
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Oops/" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean allFieldsFilled(TextInputEditText... fields) {
        for (TextInputEditText field : fields) {
            if (TextUtils.isEmpty(field.getText().toString())) {
                return false;
            }
        }
        return true;
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void confirmDeleteNotice(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Do you want to delete this item?");
        builder.setCancelable(false);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                noticesRef.child(key).setValue(null);
                dialogInterface.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            pickedImageUri = data.getData();
            profileImage.setImageURI(pickedImageUri);
        }
    }

    /** Binds one notice card in the list to its views. */
    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String value) {
            ((TextView) mView.findViewById(R.id.name)).setText(value);
        }

        public void setDescription(String value) {
            ((TextView) mView.findViewById(R.id.placed_text)).setText(value);
        }

        public void setDateofnotice(String value) {
            ((TextView) mView.findViewById(R.id.session_text)).setText(value);
        }

        public void setItemImage(String url, Context context) {
            ImageView imageView = mView.findViewById(R.id.image);
            Glide.with(context).load(url).into(imageView);
        }
    }
}
