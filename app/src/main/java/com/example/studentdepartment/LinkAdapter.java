package com.example.studentdepartment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * List of attached links shown inside a comment/notice card.
 * Tapping a link opens it in the browser.
 */
public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.MyViewHolder> {

    private final Context context1;
    private final ArrayList<String> linkModels;

    public LinkAdapter(Context context, ArrayList<String> linkModels) {
        this.context1 = context;
        this.linkModels = linkModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.linkcardview, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.index.setText(" " + (position + 1));
        holder.file.setText(linkModels.get(position));

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = linkModels.get(position);
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context1.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return linkModels.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView index;
        TextView file;

        public MyViewHolder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linear_layout);
            index = itemView.findViewById(R.id.index);
            file = itemView.findViewById(R.id.file_name);
        }
    }
}
