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
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListGymAdapter extends RecyclerView.Adapter<ListGymAdapter.MyViewHolder> implements Filterable {
    private final List<Gym> gyms;
    private final List<Gym> gymsFull;
    private final Context context;
    private LatLng currentLocation;

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public ListGymAdapter(@NonNull final Context ct, @NonNull final List<Gym> gyms,
                          @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
        this.context = ct;
        this.gyms = gyms;
        this.gymsFull = new ArrayList<>(this.gyms);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public final RelativeLayout cardContainer, bcContainer;
        private final LinearLayout subsContainer;
        private final LinearLayout turnsContainer;
        private final CircleImageView startIcon;
        private final TextView username, details;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.startIcon = itemView.findViewById(R.id.start_icon);
            this.username = itemView.findViewById(R.id.gym_name);
            this.details = itemView.findViewById(R.id.gym_details);

            this.cardContainer = itemView.findViewById(R.id.card_container);
            this.bcContainer = itemView.findViewById(R.id.bc_container);
            this.subsContainer = itemView.findViewById(R.id.subs_container);
            this.turnsContainer = itemView.findViewById(R.id.turns_container);
        }

        public void bind(@NonNull final Context context, @NonNull final Gym gym, final int position,
                         @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
            this.subsContainer.removeAllViews();
            this.turnsContainer.removeAllViews();

            for (int i = 0; i < 4; i++) {
                this.subsContainer.addView(createSubscriptionRow(context,
                        (String) gym.getSubscription().keySet().toArray()[i],
                        (Boolean) gym.getSubscription().values().toArray()[i]
                ));
            }

            for (int i = 0; i < 3; i++) {
                this.turnsContainer.addView(createTurnRow(context,
                        (String) gym.getTurns().keySet().toArray()[i],
                        (Boolean[]) gym.getTurns().values().toArray()[i]
                ));
            }

            this.cardContainer.setOnClickListener(v -> clickListener.onItemClick(this, position));
            this.cardContainer.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(this, position);
                return true;
            });
        }

        /**
         * Create and set a View container with TextView and their set values
         *
         * @param context origin Context of current ViewHolder
         * @param key String with subscription key
         * @param value Boolean with subscription value of current Gym object
         * @return View container
         */
        @NonNull
        private LinearLayout createSubscriptionRow(@NonNull final Context context, @NonNull final String key, @NonNull final Boolean value) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setWeightSum(2);

            MaterialTextView subscriptionType = new MaterialTextView(context);
            subscriptionType.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0));
            subscriptionType.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
            subscriptionType.setTextSize(12);
            subscriptionType.setTextAlignment(LinearLayout.TEXT_ALIGNMENT_TEXT_START);
            subscriptionType.setText(getSubscriptionResource(context, key));

            MaterialTextView subscription = new MaterialTextView(context);
            subscription.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            subscription.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
            subscription.setTextSize(12);
            subscription.setTextAlignment(LinearLayout.TEXT_ALIGNMENT_TEXT_END);
            subscription.setText(value
                    ? context.getResources().getString(R.string.prompt_available)
                    : context.getResources().getString(R.string.prompt_not_available));
            layout.addView(subscriptionType);
            layout.addView(subscription);

            return layout;
        }

        /**
         * Get a resource translated of subscription key
         *
         * @param context origin Context of current ViewHolder
         * @param key String with subscription key
         * @return String with subscription key translated from Resources
         */
        @NonNull
        private String getSubscriptionResource(@NonNull final Context context, @NonNull final String key) {
            final String[] gymKeys = context.getResources().getStringArray(R.array.gym_field);
            final String[] subscriptionKeys = new String[]{
                    gymKeys[10], gymKeys[11], gymKeys[12], gymKeys[13]
            };

            String resource;

            if (subscriptionKeys[0].equals(key)) {
                resource = context.getResources().getString(R.string.monthly_subscription);
            } else if (subscriptionKeys[1].equals(key)) {
                resource = context.getResources().getString(R.string.quarterly_subscription);
            } else if (subscriptionKeys[2].equals(key)) {
                resource = context.getResources().getString(R.string.six_month_subscription);
            } else {
                resource = context.getResources().getString(R.string.annual_subscription);
            }

            return resource;
        }

        /**
         * Create and set a View container with TextView and their set values
         *
         * @param context origin Context of current ViewHolder
         * @param key String with turn key
         * @param value Boolean array with turn values
         * @return View container
         */
        private LinearLayout createTurnRow(@NonNull final Context context, @NonNull final String key, final Boolean[] value) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            for (int i = 0; i < 3; i++) {
                if (value[i]) {
                    MaterialTextView turnValue = new MaterialTextView(context);
                    turnValue.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    turnValue.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
                    turnValue.setTextSize(12);
                    turnValue.setTextAlignment(LinearLayout.TEXT_ALIGNMENT_TEXT_START);
                    turnValue.setText(getTurnResource(context, key, i));

                    layout.addView(turnValue);
                }
            }

            return layout;

        }

        /**
         * Get a resource of turn value
         *
         * @param context origin Context of current ViewHolder
         * @param key String with turn key
         * @param position Position of turn value
         * @return String with turn value fom Resources
         */
        @NonNull
        private String getTurnResource(@NonNull final Context context, @NonNull final String key, final int position) {
            final String[] gymKeys = context.getResources().getStringArray(R.array.gym_field);
            final String[] turnKeys = new String[]{
                    gymKeys[14], gymKeys[15], gymKeys[16],
            };
            final String[] morningTurnKeys = context.getResources().getStringArray(R.array.morning_session_value);
            final String[] afternoonTurnKeys = context.getResources().getStringArray(R.array.afternoon_session_value);
            final String[] eveningTurnKeys = context.getResources().getStringArray(R.array.evening_session_value);

            String resource;

            if (turnKeys[0].equals(key)) resource = morningTurnKeys[position];
            else
                resource = turnKeys[1].equals(key) ? afternoonTurnKeys[position] : eveningTurnKeys[position];

            return resource;
        }

    }

    @Override
    @NonNull
    public ListGymAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_recycleview_gyms, parent, false);
        return new ListGymAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListGymAdapter.MyViewHolder holder, final int position) {
        holder.bind(this.context, this.gyms.get(position), position, this.clickListener, this.longClickListener);

        // load
        Picasso.get().load(this.gyms.get(position).getImage()).into(holder.startIcon);

        final String username = this.gyms.get(position).getName();
        holder.username.setText(username);

        final String address = this.gyms.get(position).getAddress();
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
    @NonNull
    public Filter getFilter() {
        return this.filter;
    }

    @NonNull
    public Filter getMenuFilter() {
        return this.filterMenu;
    }

    @NonNull
    public Filter getSort() {
        return this.sorter;
    }

    @NonNull
    private final Filter filter = new Filter() {

        @Override
        protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
            final List<Gym> filteredList = new ArrayList<>();

            if (constraint.length() == 0) {
                filteredList.clear();
                filteredList.addAll(gymsFull);
            } else {
                filteredList.clear();
                final String filterPattern = constraint.toString().toLowerCase();
                filterCompare(filteredList, filterPattern, "search");
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(@NonNull final CharSequence constraint, @NonNull final FilterResults results) {
            @SuppressWarnings("unchecked") final List<Gym> listTmp = (List<Gym>) results.values;
            gyms.clear();
            gyms.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final Filter filterMenu = new Filter() {

        @Override
        protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
            final List<Gym> filteredList = new ArrayList<>();

            Log.d("KEY_LOG", (String) constraint);

            if (constraint.equals(context.getString(R.string.filter_by_distance_10))) {
                filteredList.clear();
                final String filterPattern = constraint.toString();
                filterCompare(filteredList, filterPattern, "filter");
            } else if (constraint.equals(context.getString(R.string.filter_by_distance_25))) {
                filteredList.clear();
                final String filterPattern = constraint.toString();
                filterCompare(filteredList, filterPattern, "filter");
            } else if (constraint.equals(context.getString(R.string.filter_by_distance_50))) {
                filteredList.clear();
                final String filterPattern = constraint.toString();
                filterCompare(filteredList, filterPattern, "filter");
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(@NonNull final CharSequence constraint, @NonNull final FilterResults results) {
            @SuppressWarnings("unchecked") final List<Gym> listTmp = (List<Gym>) results.values;
            gyms.clear();
            gyms.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final Filter sorter = new Filter() {
        @Override
        protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
            final List<Gym> filteredList = new ArrayList<>();

            if (constraint.equals(context.getResources().getString(R.string.prompt_name))) {
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
            final List<Gym> listTmp = (List<Gym>) results.values;
            gyms.clear();
            gyms.addAll(listTmp);
            notifyDataSetChanged();
        }
    };

    private void filterCompare(@NonNull final List<Gym> filteredList, @NonNull final String constraint, @NonNull final String rule) {
        filteredList.clear();

        if (rule.equals("search")) {
            this.gymsFull.forEach(gym -> {
                if (gym.getName().toLowerCase().contains(constraint)) {
                    filteredList.add(gym);
                }
            });
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        }
        else if (rule.equals("sort")) {
            filteredList.addAll(this.gymsFull);
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        } else if (rule.equals("filter") && constraint.equals(context.getString(R.string.filter_by_distance_10))) {
            final int constKmToMeter = 1000;
            final int filterConstraint = 10 + 2; // add a margin of error for 2 km
            this.gymsFull.forEach(gym -> {
                int result = (int) distance(gym.getPosition(), currentLocation);
                if ((result / constKmToMeter) < filterConstraint) {
                    filteredList.add(gym);
                }
                AppUtils.log(Thread.currentThread().getStackTrace(), gym.getName() + " is far from you for: " + (result / constKmToMeter) + "km");
            });
        } else if (rule.equals("filter") && constraint.equals(context.getString(R.string.filter_by_distance_25))) {
            final int constKmToMeter = 1000;
            final int filterConstraint = 25 + 2; // add a margin of error for 2 km
            this.gymsFull.forEach(gym -> {
                int result = (int) distance(gym.getPosition(), currentLocation);
                if ((result / constKmToMeter) < filterConstraint) {
                    filteredList.add(gym);
                }
                AppUtils.log(Thread.currentThread().getStackTrace(), gym.getName() + " is far from you for: " + (result / constKmToMeter) + "km");
            });
        } else if (rule.equals("filter") && constraint.equals(context.getString(R.string.filter_by_distance_50))) {
            final int constKmToMeter = 1000;
            final int filterConstraint = 50 + 5; // add a margin of error for 5 km
            this.gymsFull.forEach(gym -> {
                int result = (int) distance(gym.getPosition(), currentLocation);
                if ((result / constKmToMeter) < filterConstraint) {
                    filteredList.add(gym);
                }
                AppUtils.log(Thread.currentThread().getStackTrace(), gym.getName() + " is far from you for: " + (result / constKmToMeter) + "km");
            });
        } else {
            filteredList.addAll(this.gymsFull);
            filteredList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        }
    }

    // Other methods

    public void removeItem(final int position) {
        this.gyms.remove(position);
        this.gymsFull.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(@NonNull final Gym item, final int position) {
        this.gyms.add(position, item);
        this.gymsFull.add(position, item);
        notifyItemInserted(position);
    }

    public void addItem(@NonNull final Gym item) {
        this.gyms.add(item);
        this.gymsFull.add(item);
        notifyItemInserted(this.gyms.indexOf(item));

        Log.d("KEY_LOG", this.gyms.size() + " " + this.gymsFull.size());
    }

    public void refreshItems(@NonNull final List<Gym> gyms) {
        this.gyms.clear();
        this.gyms.addAll(gyms);
        this.gymsFull.clear();
        this.gymsFull.addAll(gyms);
        notifyDataSetChanged();
    }

    public void setCurrentLocation(@NonNull final LatLng currentLocation) {
        this.currentLocation = currentLocation;
        AppUtils.log(Thread.currentThread().getStackTrace(), "Current location is:" +
                " lat " + currentLocation.latitude +
                " lng " + currentLocation.longitude);
    }

    /**
     * Calculate distance between two points in latitude and longitude.
     * If you are not interested in height difference pass 0.0.
     *
     * @return Distance in Meters
     */
    private static double distance(@NonNull final LatLng startPoint, @NonNull final LatLng endPoint) {
        final int R = 6371; // Radius of the earth

        final double latDistance = Math.toRadians(endPoint.latitude - startPoint.latitude);
        final double lonDistance = Math.toRadians(endPoint.longitude - startPoint.longitude);
        final double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(startPoint.latitude)) * Math.cos(Math.toRadians(endPoint.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        final double height = 0.0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

}
