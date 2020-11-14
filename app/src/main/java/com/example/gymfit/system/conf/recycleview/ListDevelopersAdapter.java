package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListDevelopersAdapter extends RecyclerView.Adapter<ListDevelopersAdapter.MyViewHolder> {
    private final List<Object[]> developers;
    private final Context context;

    private final OnItemClickListener listener;

    public ListDevelopersAdapter(@NonNull final Context context, @NonNull final List<Object[]> developers,
                                 @NonNull final OnItemClickListener listener) {
        this.developers = developers;
        this.context = context;
        this.listener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout row;
        private final CircleImageView image;
        private final MaterialTextView fullname;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.row = itemView.findViewById(R.id.container_row);
            this.image = itemView.findViewById(R.id.image);
            this.fullname = itemView.findViewById(R.id.fullname);
        }

        public void bind(final int position, @NonNull final OnItemClickListener listener) {
            this.row.setOnClickListener(v -> listener.onItemClick(this, position));
        }
    }

    @NonNull
    @Override
    public ListDevelopersAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(this.context);
        final View view = inflater.inflate(R.layout.layout_recycleview_developer, parent, false);
        return new ListDevelopersAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListDevelopersAdapter.MyViewHolder holder, final int position) {
        holder.bind(position, this.listener);

        final String fullname = String.valueOf(this.developers.get(position)[0]);
        holder.fullname.setText(fullname);

        final Drawable drawImage = (Drawable) this.developers.get(position)[1];
        holder.image.setImageDrawable(drawImage);
    }

    @Override
    public int getItemCount() {
        return this.developers.size();
    }

}
