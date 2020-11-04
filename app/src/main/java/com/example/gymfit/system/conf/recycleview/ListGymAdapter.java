package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListGymAdapter extends RecyclerView.Adapter<ListGymAdapter.MyViewHolder> implements Filterable {
    private final List<Gym> gyms;
    private final List<Gym> gymsFull;
    private final Context context;

    private final OnItemClickListener listener;

    public ListGymAdapter(Context ct, List<Gym> gyms, OnItemClickListener listener) {
        this.context = ct;
        this.gyms = gyms;
        this.gymsFull = new ArrayList<>(this.gyms);
        this.listener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public final RelativeLayout cardContainer, deleteContainer;
        private final LinearLayout toggleContainer, gymContainer;
        private final CircleImageView startIcon;
        private final ImageView endIcon;
        private final TextView username, details;
        private final List<TextView> gymList = new ArrayList<>();

        public MyViewHolder(@NotNull View itemView) {
            super(itemView);

            this.startIcon = itemView.findViewById(R.id.start_icon);
            this.endIcon = itemView.findViewById(R.id.end_icon);
            this.username = itemView.findViewById(R.id.gym_name);
            this.details = itemView.findViewById(R.id.gym_details);

            this.cardContainer = itemView.findViewById(R.id.card_container);
            this.deleteContainer = itemView.findViewById(R.id.delete_container);
            this.gymContainer = itemView.findViewById(R.id.gym_container);
            this.toggleContainer = itemView.findViewById(R.id.content_toggle_container);
        }

        public void bind(final Context context, final Gym gym, final int position, final OnItemClickListener listener) {
            this.gymList.clear();
            this.gymContainer.removeAllViews();

            this.endIcon.setOnClickListener(v -> listener.onItemClick(this, position));
        }
    }

    @NonNull
    @Override
    public ListGymAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_recycleview_gyms, parent, false);
        return new ListGymAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListGymAdapter.MyViewHolder holder, int position) {
        holder.bind(this.context, this.gyms.get(position), position, listener);

        // load
        Picasso.get().load(this.gyms.get(position).getImage()).into(holder.startIcon);

        String username = this.gyms.get(position).getName();
        holder.username.setText(username);

        String address = this.gyms.get(position).getAddress();
        holder.details.setText(address);

    }

    @Override
    public int getItemCount() {
        if (this.gyms == null) {
            return 0;
        } else {
            return this.gyms.size();
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    private final Filter filter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Gym> filteredList = new ArrayList<>();
            gyms.clear();

            if (constraint == null || constraint.length() == 0) {
                filteredList.clear();
                filteredList.addAll(gymsFull);
            } else {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase().trim();
                filterCompare(filteredList, filterPattern, "username");
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            final List<Gym> listTmp = (List<Gym>) results.values;
            gyms.clear();
            gyms.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    private void filterCompare(List<Gym> filteredList, String constraint, String rule) {
        filteredList.clear();

        filteredList.addAll(gymsFull);
    }

}
