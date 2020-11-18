package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListUserSubscribedAdapter extends RecyclerView.Adapter<ListUserSubscribedAdapter.MyViewHolder> {
    private final List<Gym> gyms = new ArrayList<>();
    private final Context context;

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public ListUserSubscribedAdapter(@NonNull final Context ct, @NonNull final Gym gym,
                                     @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
        this.context = ct;
        this.gyms.add(gym);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final RelativeLayout cardContainer, bcContainer;
        private final LinearLayout subsContainer;
        private final LinearLayout turnsContainer;
        private final CircleImageView startIcon;
        private final TextView username, details;

        public MyViewHolder(@NotNull final View itemView) {
            super(itemView);

            this.startIcon = itemView.findViewById(R.id.m_start_icon);
            this.username = itemView.findViewById(R.id.m_gym_name);
            this.details = itemView.findViewById(R.id.m_gym_details);

            this.cardContainer = itemView.findViewById(R.id.card_container);
            this.bcContainer = itemView.findViewById(R.id.bc_container);
            this.subsContainer = itemView.findViewById(R.id.m_subs_container);
            this.turnsContainer = itemView.findViewById(R.id.m_turns_container);
        }

        public void bind(@NotNull final Context context, @NotNull final Gym gym, @NotNull final int position,
                         @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
            this.subsContainer.removeAllViews();
            this.turnsContainer.removeAllViews();

            for (int i=0; i<4; i++) {
                subsContainer.addView(createSubscriptionRow(context,
                        (String) gym.getSubscription().keySet().toArray()[i],
                        (Boolean) gym.getSubscription().values().toArray()[i]
                ));
            }

            for (int i=0; i<3; i++) {
                turnsContainer.addView(createTurnRow(context,
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
        @NotNull
        private LinearLayout createSubscriptionRow(final Context context, final String key, final Boolean value) {
            final LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setWeightSum(2);

            final MaterialTextView subscriptionType = new MaterialTextView(context);
            subscriptionType.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0));
            subscriptionType.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
            subscriptionType.setTextSize(12);
            subscriptionType.setTextAlignment(LinearLayout.TEXT_ALIGNMENT_TEXT_START);
            subscriptionType.setText(getSubscriptionResource(context, key));

            final MaterialTextView subscription = new MaterialTextView(context);
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
        @NotNull
        private String getSubscriptionResource(final Context context, final String key) {
            final String[] gymKeys = context.getResources().getStringArray(R.array.gym_field);
            final String[] subscriptionKeys = new String[] {
                    gymKeys[10], gymKeys[11], gymKeys[12],gymKeys[13]
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
        private LinearLayout createTurnRow(@NotNull final Context context, @NotNull final String key, final Boolean[] value) {
            final LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            for (int i=0; i<3; i++) {
                if (value[i]) {
                    final MaterialTextView turnValue = new MaterialTextView(context);
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
        @NotNull
        private String getTurnResource(final Context context, final String key, final int position) {
            final String[] gymKeys = context.getResources().getStringArray(R.array.gym_field);
            final String[] turnKeys = new String[] {
                    gymKeys[14], gymKeys[15], gymKeys[16],
            };
            final String[] morningTurnKeys = context.getResources().getStringArray(R.array.morning_session_value);
            final String[] afternoonTurnKeys = context.getResources().getStringArray(R.array.afternoon_session_value);
            final String[] eveningTurnKeys = context.getResources().getStringArray(R.array.evening_session_value);

            final String resource;

            if (turnKeys[0].equals(key)) resource = morningTurnKeys[position];
            else resource = turnKeys[1].equals(key) ? afternoonTurnKeys[position] : eveningTurnKeys[position];

            return resource;
        }

    }

    @NonNull
    @Override
    public ListUserSubscribedAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_recycleview_user_subscribed, parent, false);
        return new ListUserSubscribedAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListUserSubscribedAdapter.MyViewHolder holder, final int position) {
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
        return this.gyms.size();
    }

    // Other methods

    public void removeItem(final int position) {
        this.gyms.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(@NonNull final Gym item, final int position) {
        this.gyms.add(position, item);
        notifyItemInserted(position);
    }

}
