package com.example.studentdepartment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import ozaydin.serkan.com.image_zoom_view.ImageViewZoom;

/**
 * Small horizontal list of attached images, shown inside a comment/notice
 * card. Uses Glide to load each image URL and ImageViewZoom so the user
 * can pinch-zoom to see detail.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private final Context context1;
    private final ArrayList<String> imageModels;

    public ImageAdapter(Context context, ArrayList<String> imageModels) {
        this.context1 = context;
        this.imageModels = imageModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagecardview, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // fitCenter + PREFER_ARGB_8888 keeps image quality good when zoomed;
        // override(Integer.MIN_VALUE) tells Glide to load at original size
        // instead of guessing a smaller target size from the view.
        Glide.with(context1)
                .load(imageModels.get(position))
                .apply(new RequestOptions()
                        .fitCenter()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Integer.MIN_VALUE))
                .into(holder.imageViewZoom);
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageViewZoom imageViewZoom;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageViewZoom = itemView.findViewById(R.id.image_view);
        }
    }
}
