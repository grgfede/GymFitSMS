package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.google.android.material.textview.MaterialTextView;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListTurnPickedAdapter extends RecyclerView.Adapter<ListTurnPickedAdapter.MyViewHolder> {
    private final List<Object[]> turns;
    private final Context context;

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public ListTurnPickedAdapter(@NonNull final Context context, @NonNull final List<Object[]> turns,
                                 @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
        this.turns = turns;
        this.turns.sort((o1, o2) -> ((Date) o2[0]).compareTo((Date) o1[0]));
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final RelativeLayout cardContainer, deleteContainer;
        private final CircleImageView startIcon;
        private final MaterialTextView date, type;

        private Object[] adapterTurn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.cardContainer = itemView.findViewById(R.id.card_container);
            this.cardContainer.setClickable(true);
            this.cardContainer.setLongClickable(true);

            this.deleteContainer = itemView.findViewById(R.id.delete_container);
            this.startIcon = itemView.findViewById(R.id.start_icon);
            this.date = itemView.findViewById(R.id.turn_date_value);
            this.type = itemView.findViewById(R.id.turn_type_value);
        }

        public void bind(@NonNull final Object[] turn, final int position,
                         @NonNull final OnItemClickListener clickListener, @NonNull final OnItemLongClickListener longClickListener) {
            this.adapterTurn = turn;

            this.cardContainer.setOnClickListener(v -> clickListener.onItemClick(this, position));
            this.cardContainer.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(this, position);
                return true;
            });
        }

        @NonNull
        public Object[] getAdapterTurn() {
            return  this.adapterTurn;
        }

    }

    @NonNull
    @Override
    public ListTurnPickedAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(this.context);
        final View view = inflater.inflate(R.layout.layout_recycleview_turn_picked, parent, false);
        return new ListTurnPickedAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListTurnPickedAdapter.MyViewHolder holder, final int position) {
        holder.bind(this.turns.get(position), position, this.clickListener, this.longClickListener);

        final String date = new SimpleDateFormat("EEEE, dd", Locale.getDefault()).format(((Date) this.turns.get(position)[0]));
        holder.date.setText(StringUtils.capitalize(date));

        final String type = AppUtils.getTurnValueFromKey(String.valueOf(this.turns.get(position)[1]));
        holder.type.setText(type);

        switch (AppUtils.getCategoryFromTurnKey(String.valueOf(this.turns.get(position)[1]))) {
            case "morning":
                holder.startIcon.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_weather_morning));
                break;
            case "afternoon":
                holder.startIcon.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_weather_afternoon));
                break;
            case "evening":
                holder.startIcon.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_weather_evening));
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return this.turns.size();
    }

    public void replaceItems(@NonNull final List<Object[]> turns) {
        this.turns.clear();
        this.turns.addAll(turns);
        this.turns.sort((o1, o2) -> ((Date) o2[0]).compareTo((Date) o1[0]));
        notifyDataSetChanged();
    }

    public void removeItem(final int position) {
        this.turns.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(@NonNull final Object[] turn, final int position) {
        this.turns.add(position, turn);
        notifyItemRemoved(position);
    }

}
