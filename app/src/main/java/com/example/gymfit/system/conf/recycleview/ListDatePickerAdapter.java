package com.example.gymfit.system.conf.recycleview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.google.android.material.textview.MaterialTextView;

import java.util.Calendar;
import java.util.List;

public class ListDatePickerAdapter extends RecyclerView.Adapter<ListDatePickerAdapter.MyViewHolder> {
    private final List<String> dayPickerValue;
    private final List<String> dayPickerKeys;
    private final Context context;
    public int itemCheckedPosition = RecyclerView.NO_POSITION;

    private boolean isCurrentDayChecked = false;

    public ListDatePickerAdapter(@NonNull Context context, @NonNull List<String> dayPickerValues, @NonNull List<String> dayPickerKeys) {
        this.dayPickerValue = dayPickerValues;
        this.dayPickerKeys = dayPickerKeys;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView textViewKey, textViewValue;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textViewKey = itemView.findViewById(R.id.date_picker_key);
            this.textViewValue = itemView.findViewById(R.id.date_picker_value);
        }

        public void bind(final int position) {
            this.textViewValue.setOnClickListener(v -> {
                if (itemCheckedPosition != RecyclerView.NO_POSITION) {
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
        holder.bind(position);

        String datePickerKey = this.dayPickerKeys.get(position);
        holder.textViewKey.setText(datePickerKey);

        String datePickerValue = this.dayPickerValue.get(position);
        holder.textViewValue.setText(datePickerValue);

        // check if current day to select him
        if ((Integer.parseInt(this.dayPickerValue.get(position)) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) && !this.isCurrentDayChecked) {
            this.isCurrentDayChecked = true;
            this.itemCheckedPosition = position;
        }

        if (this.itemCheckedPosition == position) {
            checkedItem(holder.textViewValue);
        } else {
            uncheckedItem(holder.textViewValue);
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

    public int getCheckedItem() {
        return this.itemCheckedPosition;
    }

}
