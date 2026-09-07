package com.example.studentdepartment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.borjabravo.readmoretextview.ReadMoreTextView;

import java.util.ArrayList;

/**
 * Shows the list of questions/comments on the Comments screen.
 *
 * Each row shows who asked, when, the question text, and any attached
 * images/links (using ImageAdapter / LinkAdapter as nested horizontal
 * lists). The bottom button changes depending on who's viewing and
 * whether it's answered yet:
 *   - Not answered, viewer is Department -> "Add Answer" (goes to AnswerActivity)
 *   - Not answered, viewer is Student     -> "UnAnswered" (disabled)
 *   - Already answered                    -> "Answer" (goes to AnsQuestionActivity to view it)
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private final Context context1;
    private final ArrayList<CommentModel> commentModels;
    private final String currentUserRole;

    public CommentAdapter(Context context, ArrayList<CommentModel> commentModels) {
        this.context1 = context;
        this.commentModels = commentModels;

        SharedPreferences prefs = context.getSharedPreferences("admin_detail", 0);
        this.currentUserRole = prefs.getString("User", "");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.commentcardview, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        CommentModel item = commentModels.get(position);

        // Build a short set of initials from the asker's name for the
        // little avatar-style badge (e.g. "Rajeev Kumar" -> "RK").
        String[] nameParts = item.getName().split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        holder.title.setText(initials.toString());

        holder.date.setText("Posted on : " + item.getEvent());
        holder.question.setText(item.getQuestion());
        holder.name.setText("By : " + item.getName());

        if (item.getImageList().isEmpty()) {
            holder.attachment_text.setVisibility(View.GONE);
            holder.recyclerView.setVisibility(View.GONE);
        } else {
            LinearLayoutManager horizontalLayout = new LinearLayoutManager(context1);
            horizontalLayout.setOrientation(RecyclerView.HORIZONTAL);
            holder.recyclerView.setLayoutManager(horizontalLayout);
            holder.recyclerView.setAdapter(new ImageAdapter(context1, item.getImageList()));
        }

        if (item.getLinkList().isEmpty()) {
            holder.link_text.setVisibility(View.GONE);
            holder.recyclerView1.setVisibility(View.GONE);
        } else {
            holder.recyclerView1.setLayoutManager(new LinearLayoutManager(context1));
            holder.recyclerView1.setAdapter(new LinkAdapter(context1, item.getLinkList()));
        }

        // NOTE: the button's own text is used as its "state" (see the click
        // listener below too). Works fine as long as the three labels stay
        // unique, but a boolean/enum on CommentModel would be more robust
        // if this gets extended later.
        if (item.getAnswered().booleanValue()) {
            holder.answer_btn.setText("Answer");
        } else if (TextUtils.equals(currentUserRole, "Student")) {
            holder.answer_btn.setText("UnAnswered");
            holder.answer_btn.setClickable(false);
        } else {
            holder.answer_btn.setText("Add Answer");
        }

        holder.answer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonText = holder.answer_btn.getText().toString();
                if (TextUtils.equals(buttonText, "Add Answer")) {
                    Intent intent = new Intent(context1, AnswerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Key", commentModels.get(position).getKey());
                    context1.startActivity(intent);
                } else if (TextUtils.equals(buttonText, "Answer")) {
                    Intent intent = new Intent(context1, AnsQuestionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Key", commentModels.get(position).getKey());
                    context1.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentModels.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        TextView name;
        ReadMoreTextView question;
        TextView attachment_text;
        RecyclerView recyclerView;
        TextView link_text;
        RecyclerView recyclerView1;
        Button answer_btn;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            name = itemView.findViewById(R.id.name);
            question = itemView.findViewById(R.id.text_view_show_more);
            attachment_text = itemView.findViewById(R.id.attachment);
            recyclerView = itemView.findViewById(R.id.recycler_image);
            link_text = itemView.findViewById(R.id.links);
            recyclerView1 = itemView.findViewById(R.id.recycler_link);
            answer_btn = itemView.findViewById(R.id.answer);
        }
    }
}
