package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.user.conf.User;
import com.google.firebase.Timestamp;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListSubscriberAdapter extends RecyclerView.Adapter<ListSubscriberAdapter.MyViewHolder> implements Filterable {
    private final List<User> users;
    private final List<User> usersFull;
    private final Context context;

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public ListSubscriberAdapter(@NonNull final Context ct, @NonNull final List<User> users,
                                 @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
        this.context = ct;
        this.users = users;
        this.usersFull = new ArrayList<>(this.users);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public final RelativeLayout cardContainer, deleteContainer;
        private final LinearLayout turnContainer;
        private final CircleImageView startIcon;
        private final TextView username, details;
        private final List<TextView> turnList = new ArrayList<>();

        public MyViewHolder(@NotNull View itemView) {
            super(itemView);

            this.startIcon = itemView.findViewById(R.id.start_icon);
            this.username = itemView.findViewById(R.id.user_name);
            this.details = itemView.findViewById(R.id.user_details);

            this.cardContainer = itemView.findViewById(R.id.card_container);
            this.cardContainer.setLongClickable(true);
            this.cardContainer.setClickable(true);

            this.deleteContainer = itemView.findViewById(R.id.delete_container);
            this.turnContainer = itemView.findViewById(R.id.turn_container);
        }

        public void bind(@NonNull final Context context, @NonNull final User user, final int position,
                         @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
            final int turnCount = user.getTurns() != null
                    ? user.getTurns().size()
                    : 0;

            this.turnList.clear();
            this.turnContainer.removeAllViews();

            for (int i=0; i<turnCount; i++) {
                final TextView turn = new TextView(context);
                turn.setTextAppearance(R.style.AppTheme_UserTurnSessionText);
                turnList.add(turn);
                turnContainer.addView(turn);
            }

            this.cardContainer.setOnClickListener(v -> clickListener.onItemClick(this, position));
            this.cardContainer.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(this, position);
                return true;
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_recycleview_subscriber, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.bind(this.context, this.users.get(position), position, this.clickListener, this.longClickListener);

        // load
        Picasso.get().load(this.users.get(position).getImg()).into(holder.startIcon);

        final String username = this.users.get(position).getName() + " " + this.users.get(position).getSurname();
        holder.username.setText(username);

        final String subscription = getSubscription(this.users.get(position).getSubscription()[1]);
        holder.details.setText(subscription);

        if (this.users.get(position).getTurns() != null) {
            final List<Map<String, Object>> turnMap = this.users.get(position).getTurns();

            turnMap.sort((map1, map2) -> {
                final Date date1 = ((Timestamp) Objects.requireNonNull(map1.get("date"))).toDate();
                final Date date2 = ((Timestamp) Objects.requireNonNull(map2.get("date"))).toDate();
                return date1.compareTo(date2);
            });

            for (int i=0; i<holder.turnList.size(); i++) {
                final String type = getTurn(Objects.requireNonNull(turnMap.get(i).get("type")).toString());

                final Timestamp timestamp = (Timestamp) Objects.requireNonNull(turnMap.get(i).get("date"));
                final Date date = timestamp.toDate();
                final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                sdf.format(date);
                final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                cal.setTime(date);
                final String dateString = cal.get(Calendar.DAY_OF_MONTH) + " " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + cal.get(Calendar.YEAR);

                final String placeHolder = dateString + ": " + type;
                holder.turnList.get(i).setText(placeHolder);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (this.users == null) {
            return 0;
        } else {
            return this.users.size();
        }
    }

    @Override
    @NonNull
    public Filter getFilter() {
        return this.filter;
    }

    @NonNull
    public Filter getFilterSub() {
        return this.filterSub;
    }

    @NonNull
    public Filter getSort() {
        return this.filterSort;
    }

    @NonNull
    private final Filter filter = new Filter() {

        @Override
        protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
            final List<User> filteredList = new ArrayList<>();
            users.clear();

            if (constraint == null || constraint.length() == 0) {
                filteredList.clear();
                filteredList.addAll(usersFull);
            } else {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase().trim();
                filterCompare(filteredList, filterPattern, "username");
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(@NonNull final CharSequence constraint, @NonNull final FilterResults results) {
            @SuppressWarnings("unchecked")
            final List<User> listTmp = (List<User>) results.values;
            users.clear();
            users.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final Filter filterSub = new Filter() {

        @Override
        protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
            final List<User> filteredList = new ArrayList<>();
            users.clear();

            if (constraint.equals(context.getResources().getString(R.string.prompt_monthly))) {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "subscription");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_quarterly))) {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "subscription");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_six_month))) {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "subscription");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_annual))) {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "subscription");
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(@NonNull final CharSequence constraint, @NonNull final FilterResults results) {
            @SuppressWarnings("unchecked")
            final List<User> listTmp = (List<User>) results.values;
            users.clear();
            users.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final Filter filterSort = new Filter() {
        @Override
        protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
            final List<User> filteredList = new ArrayList<>();

            if (constraint.equals(context.getResources().getString(R.string.prompt_name))) {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "sort");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_surname))) {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "sort");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_default))){
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "sort");
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(@NonNull final CharSequence constraint, @NonNull final FilterResults results) {
            @SuppressWarnings("unchecked")
            final List<User> listTmp = (List<User>) results.values;
            users.clear();
            users.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    private void filterCompare(@NonNull final List<User> filteredList, @NonNull final String constraint, @NonNull final String rule) {
        filteredList.clear();

        if (rule.equals("sort") && (constraint.equals(context.getString(R.string.prompt_name).toLowerCase()) || constraint.equals("default"))) {
            filteredList.addAll(usersFull);
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        }
        else if (rule.equals("sort") && constraint.equals(context.getString(R.string.prompt_surname).toLowerCase())) {
            filteredList.addAll(usersFull);
            filteredList.sort((o1, o2) -> o1.getSurname().compareTo(o2.getSurname()));
        }
        else if (rule.equals("username")) {
            usersFull.forEach(user -> {
                if (user.getFullname().toLowerCase().trim().contains(constraint)) {
                    filteredList.add(user);
                }
            });
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        }
        else if (rule.equals("subscription")) {
            usersFull.forEach(user -> {
                if (user.getSubscription()[1].toLowerCase().trim().equals(constraint)) {
                    filteredList.add(user);
                }
            });
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        }

    }

    // Other methods

    @NonNull
    private String getSubscription(@NonNull String subscription) {
        String titleSubscription = this.context.getResources().getString(R.string.title_subscription);

        switch (subscription) {
            case "annual":
                subscription = ": " + this.context.getResources().getString(R.string.annual_subscription);
                break;
            case "monthly":
                subscription = ": " + this.context.getResources().getString(R.string.monthly_subscription);
                break;
            case "quarterly":
                subscription = ": " + this.context.getResources().getString(R.string.quarterly_subscription);
                break;
            case "sixMonth":
                subscription = ": " + this.context.getResources().getString(R.string.six_month_subscription);
                break;
        }

        return titleSubscription + subscription;
    }

    @NonNull
    private String getTurn(@NonNull String turn) {
        switch (turn) {
            case "morningFirst":
                turn = this.context.getResources().getStringArray(R.array.morning_session_value)[0];
                break;
            case "morningSecond":
                turn = this.context.getResources().getStringArray(R.array.morning_session_value)[1];
                break;
            case "morningThird":
                turn = this.context.getResources().getStringArray(R.array.morning_session_value)[2];
                break;
            case "afternoonFirst":
                turn = this.context.getResources().getStringArray(R.array.afternoon_session_value)[0];
                break;
            case "afternoonSecond":
                turn = this.context.getResources().getStringArray(R.array.afternoon_session_value)[1];
                break;
            case "afternoonThird":
                turn = this.context.getResources().getStringArray(R.array.afternoon_session_value)[2];
                break;
            case "eveningFirst":
                turn = this.context.getResources().getStringArray(R.array.evening_session_value)[0];
                break;
            case "eveningSecond":
                turn = this.context.getResources().getStringArray(R.array.evening_session_value)[1];
                break;
            case "eveningThird":
                turn = this.context.getResources().getStringArray(R.array.evening_session_value)[2];
                break;
        }

        return turn;
    }

    public void removeItem(final int position) {
        this.users.remove(position);
        this.usersFull.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(@NonNull final User item, final int position) {
        this.users.add(position, item);
        this.usersFull.add(position, item);
        notifyItemInserted(position);
    }

    public void refreshItems(@NonNull final List<User> items) {
        this.users.clear();
        this.users.addAll(items);
        this.usersFull.clear();
        this.usersFull.addAll(items);
        notifyDataSetChanged();
    }

}
