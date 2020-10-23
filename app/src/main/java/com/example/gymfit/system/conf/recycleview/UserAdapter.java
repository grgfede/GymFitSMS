package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.user.conf.User;
import com.google.firebase.Timestamp;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> implements Filterable {
    private final List<User> users;
    private final List<User> usersFull;
    private final Context context;
    private int rowPosition;

    public UserAdapter(Context ct, List<User> users) {
        this.context = ct;
        this.users = users;
        this.usersFull = new ArrayList<>(this.users);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout toggleContainer;
        private final CircleImageView startIcon;
        private final ImageView endIcon;
        private final TextView username;
        private final TextView details;
        private Boolean isVisible = false;
        private final List<TextView> textViews = new ArrayList<>();

        public MyViewHolder(@NotNull View itemView) {
            super(itemView);

            startIcon = itemView.findViewById(R.id.start_icon);
            endIcon = itemView.findViewById(R.id.end_icon);
            username = itemView.findViewById(R.id.user_name);
            details = itemView.findViewById(R.id.user_details);
            toggleContainer = itemView.findViewById(R.id.content_toggle_container);

            setTurnView(getTurnCount(rowPosition), toggleContainer);

            endIcon.setOnClickListener(v -> {
                setListVisibility(toggleContainer, endIcon, isVisible);
                isVisible = !isVisible;
            });
        }

        private void setTurnView(int rowCount, ViewGroup viewGroup) {

            for (int i=0; i<rowCount; i++) {
                TextView textView = new TextView(context);
                textView.setTextAppearance(R.style.AppTheme_UserTurnSessionText);
                textViews.add(textView);

                viewGroup.addView(textView);
            }
        }

        private int getTurnCount(int position) {
            return users.get(position).getTurns().size();
        }
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
        rowPosition = position;

        Picasso.get().load(this.users.get(position).getUrlImage()).into(holder.startIcon);

        String username = this.users.get(position).getName() + " " + this.users.get(position).getSurname();
        holder.username.setText(username);

        String subscription = getSubscription(this.users.get(position).getSubscription());
        holder.details.setText(subscription);

        List<Map<String, Object>> turnMap = this.users.get(position).getTurns();

        // Order for date all entry of turn
        Collections.sort(turnMap, (map1, map2) -> {
            Date date1 = ((Timestamp) Objects.requireNonNull(map1.get("date"))).toDate();
            Date date2 = ((Timestamp) Objects.requireNonNull(map2.get("date"))).toDate();
            return date1.compareTo(date2);
        });

        for (int i=0; i<holder.textViews.size(); i++) {
            String type = getTurn(Objects.requireNonNull(turnMap.get(i).get("type")).toString());

            Timestamp timestamp = (Timestamp) Objects.requireNonNull(turnMap.get(i).get("date"));
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            sdf.format(date);
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTime(date);
            String dateString = cal.get(Calendar.DAY_OF_MONTH) + " " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + cal.get(Calendar.YEAR);

            String placeHolder = dateString + ": " + type;
            holder.textViews.get(i).setText(placeHolder);
        }

    }

    @Override
    public int getItemCount() {
        return this.users == null ? 0 : this.users.size();
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    public Filter getFilterSub() {
        return this.filterSub;
    }

    public Filter getSort() {
        return this.filterSort;
    }

    private final Filter filter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();
            users.clear();

            if (constraint == null || constraint.length() == 0) {
                filteredList.clear();
                filteredList.addAll(usersFull);
            } else {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase().trim();
                filteredList = filterCompare(filteredList, filterPattern, "username");
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            List<User> listTmp = (List<User>) results.values;
            users.clear();
            users.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    private final Filter filterSub = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();
            users.clear();

            if (constraint.equals(context.getResources().getString(R.string.prompt_monthly))) {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "subscription");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_quarterly))) {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "subscription");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_six_month))) {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "subscription");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_annual))) {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "subscription");
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            List<User> listTmp = (List<User>) results.values;
            users.clear();
            users.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    private final Filter filterSort = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();

            if (constraint.equals(context.getResources().getString(R.string.prompt_name))) {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "sort");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_surname))) {
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "sort");
            } else if (constraint.equals(context.getResources().getString(R.string.prompt_default))){
                filteredList.clear();
                String filterPattern = constraint.toString().toLowerCase();
                filteredList = filterCompare(filteredList, filterPattern, "sort");
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            List<User> listTmp = (List<User>) results.values;
            users.clear();
            users.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    private List<User> filterCompare(List<User> filteredList, String constraint, String rule) {
        filteredList.clear();

        if (rule.equals("sort") && (constraint.equals(context.getString(R.string.prompt_name).toLowerCase()) || constraint.equals("default"))) {
            filteredList.addAll(usersFull);
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        } else if (rule.equals("sort") && constraint.equals(context.getString(R.string.prompt_surname).toLowerCase())) {
            filteredList.addAll(usersFull);
            filteredList.sort((o1, o2) -> o1.getSurname().compareTo(o2.getSurname()));
        } else if (rule.equals("username")) {
            usersFull.forEach(user -> {
                if (user.getUsername().toLowerCase().trim().contains(constraint)) {
                    filteredList.add(user);
                }
            });
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        } else if (rule.equals("subscription")) {
            usersFull.forEach(user -> {
                if (user.getSubscription().toLowerCase().trim().equals(constraint)) {
                    filteredList.add(user);
                }
            });
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        }

        return filteredList;
    }


    // Other methods

    private String getSubscription(String subscription) {
        String titleSubscription = this.context.getResources().getString(R.string.title_subcription);

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

    private String getTurn(String turn) {
        switch (turn) {
            case "morningFirst":
                turn = this.context.getResources().getString(R.string.first_morning_session);
                break;
            case "morningSecond":
                turn = this.context.getResources().getString(R.string.second_morning_session);
                break;
            case "morningThird":
                turn = this.context.getResources().getString(R.string.third_morning_session);
                break;
            case "afternoonFirst":
                turn = this.context.getResources().getString(R.string.first_afternoon_session);
                break;
            case "afternoonSecond":
                turn = this.context.getResources().getString(R.string.second_afternoon_session);
                break;
            case "afternoonThird":
                turn = this.context.getResources().getString(R.string.third_afternoon_session);
                break;
            case "eveningFirst":
                turn = this.context.getResources().getString(R.string.first_evening_session);
                break;
            case "eveningSecond":
                turn = this.context.getResources().getString(R.string.second_evening_session);
                break;
            case "eveningThird":
                turn = this.context.getResources().getString(R.string.third_evening_session);
                break;
        }

        return turn;
    }

    private void setListVisibility(LinearLayout container, ImageView arrow, boolean isVisible) {

        if (!isVisible) {
            arrow.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_arrow_up));
            expandCard(container);
        } else {
            arrow.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_arrow_down));
            collapseCard(container);
        }
    }

    private static void expandCard(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(300);
        v.startAnimation(a);
    }

    private static void collapseCard(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(150);
        v.startAnimation(a);
    }

}
