package com.example.gymfit.user.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.recycleview.ListDatePickerAdapter;
import com.example.gymfit.system.conf.recycleview.ListTurnPickerAdapter;
import com.example.gymfit.system.conf.recycleview.OnItemClickListener;
import com.example.gymfit.system.conf.recycleview.OnUserSubscriptionResultCallback;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.user.conf.OnTurnFragment;
import com.example.gymfit.user.conf.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserListTurns#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserListTurns extends Fragment implements OnTurnFragment {
    private static final String USER_KEY = "user_key";

    private View messageAnchor = null;

    private User user = null;

    private final Map<String, MaterialTextView> textViewMap = new HashMap<>();
    private final Map<String, LinearLayout> containerMap = new HashMap<>();
    private final Map<String, LinearLayout> cardViewMap = new HashMap<>();
    private final Map<String, Boolean> cardViewStatus = new HashMap<>();
    private final Map<String, LinearLayout> cardViewTurnsMap = new HashMap<>();
    private final Map<String, Object[]> recycleViewTurnMap = new HashMap<>();
    private final Map<String, ImageView> endIconMap = new HashMap<>();

    private ListDatePickerAdapter listDatePickerAdapter = null;

    private boolean fragmentLaid = false;

    public static FragmentUserListTurns newInstance(@NonNull final User user) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserListTurns created");

        FragmentUserListTurns fragment = new FragmentUserListTurns();
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_KEY, user);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_user_list_turns, container, false);

        initSystemInterface(rootView);
        initInterface(rootView);

        // Turn card listener
        this.cardViewMap.forEach((key, cardView) ->
                cardView.setOnClickListener(v -> {
                    isListVisibility(Objects.requireNonNull(this.cardViewTurnsMap.get(key)), Objects.requireNonNull(this.endIconMap.get(key)), key);
        }));

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentUserListTurns layout XML created");

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentLaid = true;
    }

    @Override
    public void onFragmentBecomeVisible() {
        // if change fragment then refresh turn picker view
        if (fragmentLaid && !this.user.getSubscription()[0].equals("null")) {
            refreshTurnPickerAdapter();
        }
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull final View rootView) {
        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentUserListTurns initialized");
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
        setCardViewMap(rootView);
        setCardViewTurnsMap(rootView);
        setEndIconMap(rootView);
        setRecycleViewTurnMap();

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
        String currDate = new SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(calendar.getTime());
        currDate = currDate.substring(0, 1).toUpperCase() + currDate.substring(1).toLowerCase();
        Objects.requireNonNull(this.textViewMap.get("currDate")).setText(currDate);

        // set and init RecycleView of each day of current week
        final String[] dateKeys = new String[7];
        final String[] dateValues = new String[7];
        final Date[] days = getDaysOfWeek(new Date());
        for (int i = 0; i < 7; i++) {
            dateValues[i] = new SimpleDateFormat("dd", Locale.getDefault()).format(days[i]);
            dateKeys[i] = String.valueOf(DateFormat.format("EEEE", days[i].getTime()).charAt(0));
        }
        setRecycleViewDatePicker(rootView, Arrays.asList(dateValues), Arrays.asList(dateKeys), ((viewHolder, position) -> {
            // if change date picker item checked then refresh turn picker view
            if (!this.user.getSubscription()[0].equals("null")) {
                refreshTurnPickerAdapter();
            }
        }));
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
                        String title = getString(R.string.title_subscription);
                        title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
                        String subscription = Gym.getTranslatedFromSubscription(this.user.getSubscription()[1]);
                        subscription = subscription.substring(0, 1).toUpperCase() + subscription.substring(1).toLowerCase();
                        final String placeholder = title + " " + subscription;
                        textView.setText(placeholder);
                    } else if (!key.equals("currDate")) {
                        textView.setText(getSubscriptionsAvailable(key, data));
                    }
                });

                // set all recycle view:
                // key (morning, afternoon, evening), data (0 - recycleView, 1 - adapter, 3 - layout recycleView container)
                final List<String> turnKeys = new ArrayList<String>() {
                    {
                        add(getString(R.string.prompt_morning));
                        add(getString(R.string.prompt_afternoon));
                        add(getString(R.string.prompt_evening));
                    }
                };
                turnKeys.forEach(turnKey -> {
                    final List<String> turnAvailable = getUserTurnAvailable(
                            Collections.synchronizedList(new LinkedList<>(Arrays.asList(getSubscriptionsAvailable(turnKey, data).split(", ")))),
                            this.listDatePickerAdapter.getItemChecked());

                    setRecycleViewTurnPicker(rootView, turnAvailable, turnKey, (Integer) Objects.requireNonNull(this.recycleViewTurnMap.get(turnKey))[2],
                        ((viewHolder, position) -> {
                            // backup before actions
                            final String item = turnAvailable.get(position);

                            final int checkedDayPosition = this.listDatePickerAdapter.getItemCheckedPosition();
                            final Date currentDate = FragmentUserListTurns.getDaysOfWeek(Calendar.getInstance().getTime())[checkedDayPosition];
                            final String message = DateFormat.format("EEEE", currentDate.getTime()) + " " + item;

                            createSubscribeDialog(message, resultDialog -> {
                                if (resultDialog != null) {
                                    // remove item from adapter
                                    ((ListTurnPickerAdapter) Objects.requireNonNull(recycleViewTurnMap.get(turnKey))[1]).removeItem(position);
                                    ((ListTurnPickerAdapter) Objects.requireNonNull(recycleViewTurnMap.get(turnKey))[1]).notifyDataSetChanged();

                                    AppUtils.message(this.messageAnchor, getString(R.string.user_booked) + message, Snackbar.LENGTH_LONG)
                                        .setAction(getString(R.string.prompt_cancel), v -> {
                                            // restore item from adapter
                                            ((ListTurnPickerAdapter) Objects.requireNonNull(recycleViewTurnMap.get(turnKey))[1]).restoreItem(item, position);
                                            AppUtils.log(Thread.currentThread().getStackTrace(), "Abort booking at: " + message);
                                        })
                                        .setActionTextColor(ResourcesCompat.getColor(getResources(), R.color.tint_message_text, null))
                                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                            @Override
                                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                                super.onDismissed(transientBottomBar, event);
                                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                                                    isListVisibility(Objects.requireNonNull(cardViewTurnsMap.get(turnKey)), Objects.requireNonNull(endIconMap.get(turnKey)), turnKey);
                                                    addTurnIntoUser(currentDate, item);
                                                    AppUtils.log(Thread.currentThread().getStackTrace(), "Booking at: " + message);
                                                } else if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                                    AppUtils.log(Thread.currentThread().getStackTrace(), message + " is restored into ListTurnPickerAdapter");
                                                }
                                            }
                                        })
                                        .show();
                                } else {
                                    AppUtils.log(Thread.currentThread().getStackTrace(), "Abort booking at: " + message);
                                }
                            });
                        }));
                });
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
        this.textViewMap.put("morning", rootView.findViewById(R.id.turn_morning_details));
        this.textViewMap.put("afternoon", rootView.findViewById(R.id.turn_afternoon_details));
        this.textViewMap.put("evening", rootView.findViewById(R.id.turn_evening_details));
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

    /**
     * Set this object with all View component LinearLayout of layout XML (turn containers)
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setCardViewMap(@NonNull final View rootView) {
        this.cardViewMap.put("morning", rootView.findViewById(R.id.turn_picker_morning));
        this.cardViewMap.put("afternoon", rootView.findViewById(R.id.turn_picker_afternoon));
        this.cardViewMap.put("evening", rootView.findViewById(R.id.turn_picker_evening));
    }

    /**
     * Set this object with all View component LinearLayout of layout XML (turn toggle containers)
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setCardViewTurnsMap(@NonNull final View rootView) {
        this.cardViewTurnsMap.put("morning", rootView.findViewById(R.id.turn_container_morning));
        this.cardViewTurnsMap.put("afternoon", rootView.findViewById(R.id.turn_container_afternoon));
        this.cardViewTurnsMap.put("evening", rootView.findViewById(R.id.turn_container_evening));
    }

    /**
     * Set this object with all RecycleView and Adapter object of turn
     */
    private void setRecycleViewTurnMap() {
        this.recycleViewTurnMap.put("morning", new Object[] {null, null, R.id.turn_picker_morning_rv});
        this.recycleViewTurnMap.put("afternoon", new Object[] {null, null, R.id.turn_picker_afternoon_rv});
        this.recycleViewTurnMap.put("evening", new Object[] {null, null, R.id.turn_picker_evening_rv});
    }

    /**
     * Set this object with all View component ImageView of layout XML (endIcon)
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setEndIconMap(@NonNull final View rootView) {
        this.endIconMap.put("morning", rootView.findViewById(R.id.end_icon_morning));
        this.endIconMap.put("afternoon", rootView.findViewById(R.id.end_icon_afternoon));
        this.endIconMap.put("evening", rootView.findViewById(R.id.end_icon_evening));
    }

    private void setRecycleViewDatePicker(@NonNull final View rootView, @NonNull final List<String> dateValues, @NonNull final List<String> dateKeys,
                                          OnItemClickListener listener) {
        final RecyclerView datePickerRecycleView = rootView.findViewById(R.id.date_picker_rv);
        datePickerRecycleView.setHasFixedSize(true);
        datePickerRecycleView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 7));

        listDatePickerAdapter = new ListDatePickerAdapter(rootView.getContext(), dateValues, dateKeys, listener);
        datePickerRecycleView.setAdapter(listDatePickerAdapter);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Recycle date picker is set and init");
    }

    private void setRecycleViewTurnPicker(@NonNull final View rootView, @NonNull final List<String> turnValue, @NonNull final String turnKey,
                                          final int recycleViewId, OnItemClickListener listener) {
        final RecyclerView turnPickerRecycleView = rootView.findViewById(recycleViewId);
        turnPickerRecycleView.setHasFixedSize(true);
        turnPickerRecycleView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 1));

        final ListTurnPickerAdapter adapter = new ListTurnPickerAdapter(rootView.getContext(), turnValue, listener);
        turnPickerRecycleView.setAdapter(adapter);

        this.recycleViewTurnMap.replace(turnKey, new Object[] {
                turnPickerRecycleView, adapter, recycleViewId
        });

        AppUtils.log(Thread.currentThread().getStackTrace(), "Recycle turn picker is set and init");
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

    /**
     * Set the Material Text View respective of turn node with the new placeholder. It contains only text of true subscriptions
     *
     * @param key String used for access on the correct turn node
     * @param gym Gym object used for get respective subscription turn available
     */
    @NonNull
    private String getSubscriptionsAvailable(@NonNull final String key, @NonNull final Gym gym) {
        final String[] nameTurnArray;
        final List<String> checkedTurnList = new ArrayList<>();
        final Boolean[] checkedTurnArray = gym.getTurns().get(key); // get current turn category from Gym object (0 - key, 1 - available)

        // get all turn keys
        switch (key) {
            case "afternoon":
                nameTurnArray = getResources().getStringArray(R.array.afternoon_session_value);
                break;
            case "evening":
                nameTurnArray = getResources().getStringArray(R.array.evening_session_value);
                break;
            default:
                nameTurnArray = getResources().getStringArray(R.array.morning_session_value);
                break;
        }

        // filter turn keys with only available turn of Gym
        for (int i = 0; i< Objects.requireNonNull(checkedTurnArray).length; i++) {
            if (checkedTurnArray[i]) {
                checkedTurnList.add(nameTurnArray[i]);
            }
        }

        // format list if available gym turn with:
        // sort (by name),
        // deleting ", " and add into new String
        // so return it
        Collections.sort(checkedTurnList);
        final StringJoiner joiner = new StringJoiner(", ");
        for (String s : checkedTurnList) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    @NonNull
    private List<String> getUserTurnAvailable(@NonNull final List<String> subscriptionAvailable, final int checkedDay) {
        final String[] userKeys = getResources().getStringArray(R.array.user_field);

        // get all turn keys
        final List<String> allTurnKeys = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.morning_session_name)));
        allTurnKeys.addAll(Arrays.asList(getResources().getStringArray(R.array.afternoon_session_name)));
        allTurnKeys.addAll(Arrays.asList(getResources().getStringArray(R.array.evening_session_name)));

        // In the first time, take turn type value and turn date value for each current User turns.
        // So, in the case of turn date value is same of date-picker item selected or checked remove respective turn key from all Gym's turn available.
        this.user.getTurns().forEach(turn -> {
            final String turnType = turn.get(userKeys[13]) != null
                    ? String.valueOf(turn.get(userKeys[13])) : null;
            final Date turnDate = turn.get(userKeys[12]) != null
                    ? ((Timestamp) Objects.requireNonNull(turn.get(userKeys[12]))).toDate() : null;

            if (turnType != null && turnDate != null) {
                final int currentDay = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(turnDate));

                if (currentDay == checkedDay) {
                    final String turnKey = allTurnKeys.get(allTurnKeys.indexOf(turnType));
                    subscriptionAvailable.remove(AppUtils.getTurnValueFromKey(turnKey));
                }
            }
        });

        return subscriptionAvailable;
    }

    /**
     * Create and set a Material dialog to show which are current turn selected from User
     * Return item checked if positive, otherwise nothing
     *
     * @param message date string complete of turn selected from user
     */
    private void createSubscribeDialog(@NonNull final String message, OnUserSubscriptionResultCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.prompt_book));
        final String placeholder = getString(R.string.user_booking) + "\n" + message + " ?";
        builder.setMessage(placeholder);
        builder.setPositiveButton(getString(R.string.prompt_book), (dialog, which) -> callback.onCallback(Boolean.toString(true)));
        builder.setNegativeButton(getString(R.string.prompt_cancel), (dialog, which) -> callback.onCallback(null));
        builder.setOnCancelListener(dialog -> callback.onCallback(null));
        builder.create();
        builder.show();
    }

    private void addTurnIntoUser(@NonNull final Date date, @NonNull final String turnValue) {
        final String[] userKeys = getResources().getStringArray(R.array.user_field);
        final String type = AppUtils.getTurnKeyFromValue(turnValue);

        // set current User object turn
        final Map<String, Object> turn = new HashMap<String, Object>() {
            {
                put(userKeys[12], new Timestamp(date));
                put(userKeys[13], type);
            }
        };
        this.user.setTurn(turn);

        // set current User database note turn
        DatabaseUtils.updateUserTurn(this.user.getUid(), turn, (data, result) -> {});
    }

    private void isListVisibility(@NonNull final LinearLayout container, @NonNull final ImageView arrow, @NonNull final String viewName) {

        // reaction of null pointer with a new creation of card visibility
        if (!this.cardViewStatus.containsKey(viewName)) {
            this.cardViewStatus.put(viewName, false);
        }

        // if card selected is not in visible mode so enable it and replace its state, icon and layout height
        if (!Objects.requireNonNull(this.cardViewStatus.get(viewName))) {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up));
            AppUtils.expandCard(container);
            this.cardViewStatus.replace(viewName, true);
        } else {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down));
            AppUtils.collapseCard(container);
            this.cardViewStatus.replace(viewName, false);
        }
    }

    private void refreshTurnPickerAdapter() {
        DatabaseUtils.getGym(this.user.getSubscription()[0], ((data, result) -> {
            // refresh turn picker recycleview:
            // key (morning, afternoon, evening), data (0 - recycleView, 1 - adapter, 3 - layout recycleView container)
            final List<String> turnKeys = new ArrayList<String>() {
                {
                    add(getString(R.string.prompt_morning));
                    add(getString(R.string.prompt_afternoon));
                    add(getString(R.string.prompt_evening));
                }
            };
            turnKeys.forEach(turnKey -> {
                final List<String> turnAvailable = getUserTurnAvailable(
                        Collections.synchronizedList(new LinkedList<>(Arrays.asList(getSubscriptionsAvailable(turnKey, data).split(", ")))),
                        this.listDatePickerAdapter.getItemChecked());

                if (turnAvailable != null) {

                }

                ((ListTurnPickerAdapter) Objects.requireNonNull(recycleViewTurnMap.get(turnKey))[1]).replaceItems(turnAvailable);
            });
        }));
    }

}