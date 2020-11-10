package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

public class ListDatePickerAdapter extends RecyclerView.Adapter<ListDatePickerAdapter.MyViewHolder> {
    private final List<String> dayPickerValue;
    private final List<String> dayPickerKeys;
    private final Context context;
    private int itemCheckedPosition = RecyclerView.NO_POSITION;

    private final OnItemClickListener listener;

    private boolean isCurrentDayChecked = false;

    public ListDatePickerAdapter(@NonNull final Context context, @NonNull final List<String> dayPickerValues, @NonNull final List<String> dayPickerKeys,
                                 final OnItemClickListener listener) {
        this.dayPickerValue = dayPickerValues;
        this.dayPickerKeys = dayPickerKeys;
        this.context = context;
        this.listener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView textViewKey, textViewValue;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textViewKey = itemView.findViewById(R.id.date_picker_key);
            this.textViewValue = itemView.findViewById(R.id.date_picker_value);
        }

        public void bind(final int position, final int numberOfDay, OnItemClickListener listener) {
            this.textViewValue.setOnClickListener(v -> {
                if (itemCheckedPosition != RecyclerView.NO_POSITION && !isLowerDate(numberOfDay)) {
                    listener.onItemClick(this, position);
                    itemCheckedPosition = position;
                    notifyDataSetChanged();
                }
            });
        }

    }

    @NonNull
    @Override
    public ListDatePickerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_recycleview_date_picker, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListDatePickerAdapter.MyViewHolder holder, int position) {
        holder.bind(position, Integer.parseInt(this.dayPickerValue.get(position)), this.listener);

        String datePickerKey = this.dayPickerKeys.get(position);
        holder.textViewKey.setText(datePickerKey);

        String datePickerValue = this.dayPickerValue.get(position);
        holder.textViewValue.setText(datePickerValue);

        // check if current day to select him
        if ((Integer.parseInt(this.dayPickerValue.get(position)) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) && !this.isCurrentDayChecked) {
            this.isCurrentDayChecked = true;
            this.itemCheckedPosition = position;
        }

        if (isLowerDate(Integer.parseInt(this.dayPickerValue.get(position)))) {
            disabledItem(holder.textViewValue);
        } else {
            if (this.itemCheckedPosition == position) {
                checkedItem(holder.textViewValue);
            } else {
                uncheckedItem(holder.textViewValue);
            }
        }

    }

    @Override
    public int getItemCount() {
        return this.dayPickerKeys.size();
    }

    // Other methods

    private void checkedItem(@NonNull MaterialTextView textView) {
        textView.setBackground(ResourceUtils.getDrawableFromID(R.drawable.ic_date_picker_checked));
        textView.setTextColor(ResourceUtils.getColorFromID(R.color.tint_first_line_dark));
    }

    private void uncheckedItem(@NonNull MaterialTextView textView) {
        textView.setBackground(ResourceUtils.getDrawableFromID(R.drawable.ic_date_picker_unchecked));
        textView.setTextColor(ResourceUtils.getColorFromID(R.color.tint_first_line_light));
    }

    private void disabledItem(@NotNull MaterialTextView textView) {
        textView.setBackground(ResourceUtils.getDrawableFromID(R.drawable.ic_date_picker_unchecked));
        textView.setTextColor(ResourceUtils.getColorFromID(R.color.tint_third_line));
    }

    public int getItemChecked() {
        return Integer.parseInt(this.dayPickerValue.get(this.itemCheckedPosition));
    }

    public int getItemCheckedPosition() {
        return this.itemCheckedPosition;
    }

    private boolean isLowerDate(final int numberOfDay) {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > numberOfDay;
    }

}
