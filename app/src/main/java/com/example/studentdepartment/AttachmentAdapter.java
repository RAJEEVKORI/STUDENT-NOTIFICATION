package com.example.studentdepartment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ozaydin.serkan.com.image_zoom_view.ImageViewZoom;

/**
 * Shows the list of attachments (images/links) the user has picked so far,
 * while they're composing a notice or a comment -- before it's submitted.
 *
 * Tapping a card previews the image (or opens the link in a browser).
 * Tapping the delete icon removes that item from the list.
 */
public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.MyViewHolder> {

    private final Context context1;
    private final ArrayList<AttachmentModel> attachmentModels;

    public AttachmentAdapter(Context context, ArrayList<AttachmentModel> attachmentModels) {
        this.context1 = context;
        this.attachmentModels = attachmentModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachmentcardview, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        AttachmentModel item = attachmentModels.get(position);
        holder.link.setText(item.getUri());

        // Pick an icon depending on what kind of attachment this is.
        String type = item.getType();
        if (TextUtils.equals(type, "Take_Image")) {
            holder.type.setImageResource(R.drawable.baseline_add_a_photo_24);
        } else if (TextUtils.equals(type, "Add_Image")) {
            holder.type.setImageResource(R.drawable.baseline_add_photo_alternate_24);
        } else if (TextUtils.equals(type, "Add_Link")) {
            holder.type.setImageResource(R.drawable.baseline_insert_link_24);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = attachmentModels.get(position).getType();
                if (TextUtils.equals(type, "Take_Image") || TextUtils.equals(type, "Add_Image")) {
                    showImagePreview(attachmentModels.get(position).getName());
                } else if (TextUtils.equals(type, "Add_Link")) {
                    openLink(attachmentModels.get(position).getUri());
                }
            }
        });

        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete(position);
            }
        });
    }

    /** Opens a full-screen, zoomable preview of a picked image. */
    private void showImagePreview(Uri imageUri) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context1);
        dialog.setTitle("Image");

        View previewView = LayoutInflater.from(context1).inflate(R.layout.view_image, null);
        ImageViewZoom imageView = previewView.findViewById(R.id.image_view);
        ImageView closeButton = previewView.findViewById(R.id.delete_btn);

        imageView.setImageURI(imageUri);
        dialog.setCancelable(false);
        dialog.setView(previewView);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.show();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    /** Opens a picked link in the browser, adding "http://" if it's missing. */
    private void openLink(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context1.startActivity(browserIntent);
    }

    /** Confirms with the user, then removes an item from the list. */
    private void confirmDelete(final int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context1);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Do you want to delete this item?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attachmentModels.remove(position);
                notifyDataSetChanged();
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.create().show();
    }

    @Override
    public int getItemCount() {
        return attachmentModels.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView link;
        ImageView delete_btn;
        ImageView type;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            link = itemView.findViewById(R.id.link);
            delete_btn = itemView.findViewById(R.id.delete);
            type = itemView.findViewById(R.id.type);
            cardView = itemView.findViewById(R.id.cardview);
        }
    }
}
