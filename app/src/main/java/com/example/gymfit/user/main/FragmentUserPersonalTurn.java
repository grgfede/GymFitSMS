package com.example.gymfit.user.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.recycleview.ItemTouchHelperCallback;
import com.example.gymfit.system.conf.recycleview.ListDatePickerAdapter;
import com.example.gymfit.system.conf.recycleview.ListTurnPickedAdapter;
import com.example.gymfit.system.conf.recycleview.OnItemClickListener;
import com.example.gymfit.system.conf.recycleview.OnItemSwipeListener;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.user.conf.OnTurnFragment;
import com.example.gymfit.user.conf.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserPersonalTurn#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserPersonalTurn extends Fragment implements OnItemSwipeListener, OnTurnFragment {
    private static final String USER_KEY = "user_key";

    private View messageAnchor = null;

    private User user = null;

    private final Map<String, MaterialTextView> textViewMap = new HashMap<>();
    private final Map<String, LinearLayout> containerMap = new HashMap<>();

    private ListDatePickerAdapter listDatePickerAdapter = null;
    private ListTurnPickedAdapter listTurnPickedAdapter = null;

    public static FragmentUserPersonalTurn newInstance(@NonNull final User user) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserPersonalTurn created");

        FragmentUserPersonalTurn fragment = new FragmentUserPersonalTurn();
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_KEY, user);
        fragment.setArguments(bundle);

        return fragment;
    }

    private boolean fragmentLaid = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_personal_turn, container, false);

        initSystemInterface(rootView);
        initInterface(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentUserPersonalTurn layout XML created");

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentLaid = true;
    }

    @Override
    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int position) {
        final String itemDate = new SimpleDateFormat("EEEE dd, ", Locale.getDefault())
                .format(((Date) ((ListTurnPickedAdapter.MyViewHolder) viewHolder).getAdapterTurn()[0]));
        final String itemType = AppUtils.getTurnValueFromKey(String.valueOf(((ListTurnPickedAdapter.MyViewHolder) viewHolder).getAdapterTurn()[1]));
        final Object[] item = ((ListTurnPickedAdapter.MyViewHolder) viewHolder).getAdapterTurn();

        this.listTurnPickedAdapter.removeItem(position);

        final String placeholder = getString(R.string.user_unbooking) + itemDate + " " + itemType;
        AppUtils.message(this.messageAnchor, placeholder, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.prompt_cancel), v -> this.listTurnPickedAdapter.restoreItem(item, position))
                .setActionTextColor(ResourcesCompat.getColor(getResources(), R.color.tint_message_text, null))
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);

                        if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Turn picked removed definitely.");
                            removeTurnFromUser(item);
                            AppUtils.message(messageAnchor, getString(R.string.user_unbooked), Snackbar.LENGTH_SHORT).show();
                        } else if (event == DISMISS_EVENT_ACTION) {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Turn picked restored with action.");
                        }
                    }
                })
                .show();
    }

    @Override
    public void onFragmentBecomeVisible() {
        // if change fragment refresh turn picked view
        if (fragmentLaid && !this.user.getSubscription()[0].equals("null")) {
            refreshTurnPickedAdapter();
        }
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull View rootView) {
        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentUserPersonalTurn initialized");
    }

    /**
     * Set all View map object.
     * Initialize programmatically all View (date picker, turn picker) of Layout XML with specific values
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initInterface(@NonNull final View rootView) {
        // set Views maps
        setTextViewMap(rootView);
        setContainerMap(rootView);

        // set Date-picker
        initDatePicker(rootView);

        // set Turn-picker
        initTurnPicker(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Layout interface of FragmentUserListTurns initialized");

    }

    /**
     * Set and init the TextView of date-picker box and all days of current week (recycleView).
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initDatePicker(@NonNull final View rootView) {
        final Calendar calendar = Calendar.getInstance();

        // set and init TextView of date-picker bar with current month and year
        final String currDate = new SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(calendar.getTime()).toLowerCase();
        Objects.requireNonNull(this.textViewMap.get("currDate")).setText(StringUtils.capitalize(currDate));

        // set and init RecycleView of each day of current week
        final String[] dateKeys = new String[7];
        final String[] dateValues = new String[7];
        final Date[] days = getDaysOfWeek(new Date());
        for (int i = 0; i < 7; i++) {
            dateValues[i] = new SimpleDateFormat("dd", Locale.getDefault()).format(days[i]);
            dateKeys[i] = String.valueOf(DateFormat.format("EEEE", days[i].getTime()).charAt(0));
        }
        setRecycleViewDatePicker(rootView, Arrays.asList(dateValues), Arrays.asList(dateKeys), ((viewHolder, position) -> {
            // if change date picker item checked then refresh turn picked view
            if (!this.user.getSubscription()[0].equals("null")) {
                refreshTurnPickedAdapter();
            }
        }));

        AppUtils.log(Thread.currentThread().getStackTrace(), "Personal date-picker recycleview initialized");
    }

    /**
     * Set and init TextView of title turn-picker and all recycleView of gym turn's available with their listener
     *
     * @param rootView Root View object of Fragment. From it can be get the context
     */
    private void initTurnPicker(@NonNull final View rootView) {
        // If User has already subscribed to any gym can see turn and book them. Otherwise show a alert message to tell him about subscribe
        if (!this.user.getSubscription()[0].equals("null")) {
            // show turn picker layout
            Objects.requireNonNull(this.containerMap.get("turnPicker")).setVisibility(View.VISIBLE);

            DatabaseUtils.getGym(this.user.getSubscription()[0], (data, result) -> {
                // set all text view details with gym info
                this.textViewMap.forEach((key, textView) -> {
                    if (key.equals("name")) {
                        textView.setText(data.getName());
                    } else if (key.equals("subscription")) {
                        final String title = StringUtils.capitalize(getString(R.string.title_subscription).toLowerCase());
                        final String subscription = StringUtils.capitalize(Gym.getTranslatedFromSubscription(this.user.getSubscription()[1]).toLowerCase());
                        final String placeholder = title + " " + subscription;
                        textView.setText(placeholder);
                    }
                });

                // set recycle view:
                final List<Object[]> turnPicked = getUserTurnPicked(this.listDatePickerAdapter.getItemChecked());
                setRecycleViewTurnPicked(rootView, turnPicked);
            });
        }
        else {
            Objects.requireNonNull(this.containerMap.get("turnPicker")).setVisibility(View.INVISIBLE);

            AppUtils.message(this.messageAnchor, getString(R.string.user_subscriptions_void), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.prompt_subscribe),
                            v -> AppUtils.startFragment((ActivityUserProfile) requireActivity(), FragmentUserListGyms.newInstance(this.user), true))
                    .setActionTextColor(ResourcesCompat.getColor(getResources(), R.color.tint_message_text, null))
                    .show();
        }

        AppUtils.log(Thread.currentThread().getStackTrace(), "Personal turn-picked recycleview initialized");
    }

    /**
     * Set this object with all View component Text of layout XML
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setTextViewMap(@NonNull final View rootView) {
        this.textViewMap.put("currDate", rootView.findViewById(R.id.date_picker_curr_date));
        this.textViewMap.put("name", rootView.findViewById(R.id.turn_picker_name_gym));
        this.textViewMap.put("subscription", rootView.findViewById(R.id.turn_picker_subscription_gym));
    }

    /**
     * Set this object with all View component LinearLayout of layout XML (turn containers)
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setContainerMap(@NonNull final View rootView) {
        this.containerMap.put("datePicker", rootView.findViewById(R.id.date_picker));
        this.containerMap.put("turnPicker", rootView.findViewById(R.id.turn_picker));
    }

    private void setRecycleViewDatePicker(@NonNull final View rootView, @NonNull final List<String> dateValues, @NonNull final List<String> dateKeys,
                                          OnItemClickListener listener) {
        final RecyclerView datePickerRecycleView = rootView.findViewById(R.id.date_picker_rv);
        datePickerRecycleView.setHasFixedSize(true);
        datePickerRecycleView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 7));

        listDatePickerAdapter = new ListDatePickerAdapter(rootView.getContext(), dateValues, dateKeys, listener);
        datePickerRecycleView.setAdapter(listDatePickerAdapter);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Recycle date picker is set and init with: \n" +
                dateKeys);
    }

    private void setRecycleViewTurnPicked(@NonNull final View rootView, @NonNull final List<Object[]> turns) {
        final RecyclerView turnPickedRecycleView = rootView.findViewById(R.id.turn_picked_rv);
        turnPickedRecycleView.setHasFixedSize(true);
        turnPickedRecycleView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 1));

        this.listTurnPickedAdapter = new ListTurnPickedAdapter(rootView.getContext(), turns);
        turnPickedRecycleView.setAdapter(this.listTurnPickedAdapter);

        final ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(turnPickedRecycleView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Recycle turn picked is set and init");
    }

    // Other methods

    /**
     * By passing firstDayOfWeek you are free to choose MONDAY or SUNDAY as start of the week.
     *
     * @param refDate reference date of current date
     * @return array unformatted, and as Date. This way you can let the calling code decide what to do - print it or do some further calculations.
     */
    private static Date[] getDaysOfWeek(@NonNull final Date refDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(refDate);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date[] daysOfWeek = new Date[7];
        for (int i = 0; i < 7; i++) {
            daysOfWeek[i] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return daysOfWeek;
    }

    @NonNull
    private List<Object[]> getUserTurnPicked(final int checkedDay) {
        final String[] userKeys = getResources().getStringArray(R.array.user_field);
        final List<Object[]> filteredList = new ArrayList<>();

        // In the first time, take turn type value and turn date value for each current User turns.
        // So, in the case of turn date value is same of date-picker item selected or checked add this turn as picked.
        this.user.getTurns().forEach(turn -> {
            final String turnType = turn.get(userKeys[13]) != null
                    ? String.valueOf(turn.get(userKeys[13])) : null;
            final Date turnDate = turn.get(userKeys[12]) != null
                    ? ((Timestamp) Objects.requireNonNull(turn.get(userKeys[12]))).toDate() : null;

            if (turnType != null && turnDate != null) {
                final int currentDay = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(turnDate));

                if (currentDay == checkedDay) {
                    filteredList.add(new Object[] {
                            turnDate,
                            turnType,
                    });
                }
            }
        });

        return filteredList;
    }

    private void removeTurnFromUser(@NonNull Object[] turn) {
        final String[] userKeys = getResources().getStringArray(R.array.user_field);

        // remove current User object turn
        final Map<String, Object> turnTmp = new HashMap<String, Object>() {
            {
                put(userKeys[12], new Timestamp((Date) turn[0]));
                put(userKeys[13], String.valueOf(turn[1]));
            }
        };
        this.user.removeTurn(turnTmp);

        // remove current User database note turn
        DatabaseUtils.removeUserTurn(this.user.getUid(), turnTmp, (data, result) -> {});
    }

    private void refreshTurnPickedAdapter() {
        DatabaseUtils.getGym(this.user.getSubscription()[0], (data, result) -> {
            // set recycle view:
            final List<Object[]> turnPicked = getUserTurnPicked(this.listDatePickerAdapter.getItemChecked());
            this.listTurnPickedAdapter.replaceItems(turnPicked);

            AppUtils.log(Thread.currentThread().getStackTrace(), "All items of turn-picked adapter are replaced.");
        });
    }

}