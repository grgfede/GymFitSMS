package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListFaqAdapter extends RecyclerView.Adapter<ListFaqAdapter.MyViewHolder> {
    private final List<String[]> faqs;
    private final Context context;
    private final OnItemClickListener listener;

    public ListFaqAdapter(@NonNull final Context context, @NonNull final List<String[]> faqs, @NonNull final OnItemClickListener listener) {
        this.faqs = faqs;
        this.context = context;
        this.listener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout row;
        private final LinearLayout answerContainer;
        private final CircleImageView endIcon;
        private final MaterialTextView questionText, answerText;

        private String code;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            this.row = itemView.findViewById(R.id.faq_row);
            this.endIcon = itemView.findViewById(R.id.end_icon);
            this.answerContainer = itemView.findViewById(R.id.container_answer);
            this.questionText = itemView.findViewById(R.id.question);
            this.answerText = itemView.findViewById(R.id.answer);
        }

        public void bind(final int position, @NonNull final String code, @NonNull final OnItemClickListener listener) {
            this.code = code;
            this.row.setOnClickListener(v -> listener.onItemClick(this, position));
        }

        @NonNull
        public LinearLayout getAnswerContainer() {
            return this.answerContainer;
        }

        @NonNull
        public CircleImageView getEndIcon() {
            return this.endIcon;
        }

        @NonNull
        public String getCode() {
            return this.code;
        }

    }

    @NonNull
    @Override
    public ListFaqAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(this.context);
        final View view = inflater.inflate(R.layout.layout_recycleview_faq, parent, false);
        return new ListFaqAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListFaqAdapter.MyViewHolder holder, final int position) {
        holder.bind(position, this.faqs.get(position)[2], this.listener);

        final String question = this.faqs.get(position)[0];
        holder.questionText.setText(question);

        final String answer = this.faqs.get(position)[1];
        holder.answerText.setText(answer);
    }

    @Override
    public int getItemCount() {
        return this.faqs.size();
    }

}
