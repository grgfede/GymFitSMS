package com.example.gymfit.system.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.google.firebase.database.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private String[] titles, subtitles;
    private Context context;
    private int rowCount;

    public UserAdapter(Context ct, String[] titles, String[] subtitles) {
        this.context = ct;
        this.titles = titles;
        this.subtitles = subtitles;
        this.rowCount = titles.length;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_user_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.startIcon.setImageDrawable(ResourcesCompat.getDrawable(this.context.getResources(), R.drawable.ic_account, null));
        holder.endIcon.setImageDrawable(ResourcesCompat.getDrawable(this.context.getResources(), R.drawable.ic_menu_vertical, null));
        holder.username.setText(this.titles[position]);
        holder.details.setText(this.subtitles[position]);

    }

    @Override
    public int getItemCount() {
        return this.rowCount;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView startIcon;
        private ImageView endIcon;
        private TextView username;
        private TextView details;

        public MyViewHolder(@NotNull View itemView) {
            super(itemView);

            startIcon = itemView.findViewById(R.id.start_icon);
            endIcon = itemView.findViewById(R.id.end_icon);
            username = itemView.findViewById(R.id.user_name);
            details = itemView.findViewById(R.id.user_details);
        }
    }
}
