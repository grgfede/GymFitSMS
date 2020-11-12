package com.example.gymfit.gym.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.recycleview.ItemTouchHelperCallback;
import com.example.gymfit.system.conf.recycleview.OnItemSwipeListener;
import com.example.gymfit.system.conf.recycleview.ListSubscriberAdapter;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.user.conf.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FragmentGymSubs extends Fragment implements OnItemSwipeListener {
    private static final String GYM_KEY = "gym_key";

    private ListSubscriberAdapter listSubscriberAdapter;

    private View messageAnchor = null;
    private final Map<String, Boolean> viewVisibility = new HashMap<>();
    private boolean isEmptyData = false;

    private Gym gym = null;
    private final List<User> users = new ArrayList<>();

    public static FragmentGymSubs newInstance(@NonNull final Gym gym) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentGymSubs created");

        final FragmentGymSubs fragment = new FragmentGymSubs();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(GYM_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.gym = (Gym) getArguments().getSerializable(GYM_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_subs, container, false);

        initSystemInterface(rootView);
        initInterface();

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentGymSubs layout XML created");

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresher);
        refreshLayout.setColorSchemeResources(R.color.tint_refresher,
                R.color.tint_refresher_first, R.color.tint_refresher_second, R.color.tint_refresher_third);

        // if pull down with gesture refresh all available gyms adapter
        refreshLayout.setOnRefreshListener(() -> {
            AppUtils.message(this.messageAnchor, getString(R.string.refresh_users_available), Snackbar.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                refreshLayout.setRefreshing(false);
                refreshUserAdapter();

                AppUtils.message(this.messageAnchor, getString(R.string.refresh_completed), Snackbar.LENGTH_SHORT).show();
                AppUtils.log(Thread.currentThread().getStackTrace(), "Refresh turns and subscriptions.");
            }, AppUtils.getRandomDelayMillis());
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gym_subs_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {

        // If there are some subscribers enable actions of new menu, otherwise keep them disabled (no effects on recycle)
        if (!this.isEmptyData) {
            switch (item.getItemId()) {
                case R.id.app_bar_search:
                    SearchView searchView = (SearchView) item.getActionView();
                    searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            listSubscriberAdapter.getFilter().filter(newText);
                            return false;
                        }
                    });
                    break;
                case R.id.app_bar_filter:
                    listSubscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_default));
                    break;
                case R.id.action_filter_monthly:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_monthly));
                    break;
                case R.id.action_filter_quarterly:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_quarterly));
                    break;
                case R.id.action_filter_six_month:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_six_month));
                    break;
                case R.id.action_filter_annual:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_annual));
                    break;
                case R.id.action_sort_by_name:
                    listSubscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_name));
                    break;
                case R.id.action_sort_by_surname:
                    listSubscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_surname));
                    break;
                default:
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSwipe(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        String username = this.users.get(position).getFullname();

        // Backup before delete for undo action
        final User item = this.users.get(position);

        // Remove the item from recycleView
        this.listSubscriberAdapter.removeItem(position);
        AppUtils.message(this.messageAnchor, username + " " + getResources().getString(R.string.gym_subscriber_removing), Snackbar.LENGTH_LONG)
            .setAction(getResources().getString(R.string.prompt_cancel), v -> this.listSubscriberAdapter.restoreItem(item, position))
            .setActionTextColor(requireContext().getColor(R.color.tint_message_text))
            .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        AppUtils.log(Thread.currentThread().getStackTrace(),"User " + item.getFullname() + " is removed from UserAdapter");
                        removeUserFromGym(item.getUid());
                    } else if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        AppUtils.log(Thread.currentThread().getStackTrace(),"User " + item.getFullname() + " is restored into UserAdapter");
                    }
                }
            })
            .show();

    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull final View rootView) {
        // init new checked item on navigation Drawer
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);
        navigationView.getMenu().findItem(R.id.nav_menu_subs).setChecked(true);

        // Abilities toolbar item options
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_subs_toolbar_title));

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentGymSubs initialized");
    }

    /**
     * Try to find any subscribers to init Recycle or show a alert message if they aren't for this gym
     *
     */
    private void initInterface() {
        this.isEmptyData = false;

        if (!this.gym.getSubscribers().isEmpty()) {
            final int size = this.gym.getSubscribers().size();
            final AtomicInteger count = new AtomicInteger(0);

            this.gym.getSubscribers().forEach(uid ->
                    DatabaseUtils.getUser(uid, ((user, result) -> {
                            if (result == DatabaseUtils.RESULT_OK) {
                                this.users.add(user);
                                count.incrementAndGet();
                            }

                            if (count.get() == size) {
                                setUpRecycleView();
                            }
            })));
        } else {
            refreshUserAdapter();
        }
    }

    private void setUpRecycleView() {
        final RecyclerView recyclerView = requireView().getRootView().findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        this.listSubscriberAdapter = new ListSubscriberAdapter(requireActivity(), this.users, (viewHolder, position) ->
                isListVisibility(
                    viewHolder.itemView.findViewById(R.id.content_toggle_container),
                    viewHolder.itemView.findViewById(R.id.end_icon),
                    this.users.get(position).getUid()));
        recyclerView.setAdapter(listSubscriberAdapter);

        final ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    // Other methods

    private void isListVisibility(@NonNull final LinearLayout container, @NonNull final ImageView arrow, @NonNull final String viewName) {

        // reaction of null pointer with a new creation of card visibility
        if (!this.viewVisibility.containsKey(viewName)) {
            this.viewVisibility.put(viewName, false);
        }

        // if card selected is not in visible mode so enable it and replace its state, icon and layout height
        if (!Objects.requireNonNull(this.viewVisibility.get(viewName))) {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up));
            AppUtils.expandCard(container);
            this.viewVisibility.replace(viewName, true);
        } else {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down));
            AppUtils.collapseCard(container);
            this.viewVisibility.replace(viewName, false);
        }
    }

    private void refreshUserAdapter() {
        final List<User> filteredList = new ArrayList<>();

        DatabaseUtils.getGym(this.gym.getUid(), ((gym, resultGym) -> {
            if (resultGym == DatabaseUtils.RESULT_OK) {
                this.gym.setSubscribers(gym.getSubscribers());

                DatabaseUtils.getUsersID(((data, result) -> {
                    if (result == DatabaseUtils.RESULT_OK) {
                        final int size = data.size();
                        final AtomicInteger count = new AtomicInteger(0);

                        data.forEach(uid -> {
                            if (this.gym.getSubscribers().contains(uid)) {
                                DatabaseUtils.getUser(uid, ((user, resultUser) -> {
                                    if (resultUser == DatabaseUtils.RESULT_OK) {
                                        filteredList.add(user);
                                        count.incrementAndGet();
                                    }

                                    if (count.get() == size) {
                                        this.listSubscriberAdapter.refreshItems(filteredList);
                                        this.users.clear();
                                        this.users.addAll(filteredList);
                                    }
                                }));
                            }
                        });

                    }
                }));
            }
        }));
    }

    // Database methods

    private void removeUserFromGym(@NonNull final String uid) {
        if (!this.gym.getSubscribers().isEmpty() && this.gym.getSubscribers().contains(uid)) {
            // remove this user from gym object
            this.gym.removeSubscriber(uid);

            // remove this user from gym db node
            DatabaseUtils.removeGymSubscriber(this.gym.getUid(), uid, ((data, result) -> {}));

            // remove gym from user db node and clear all his turns
            DatabaseUtils.removeUserSubscription(uid, ((data, result) -> {}));
        } else {
            AppUtils.log(Thread.currentThread().getStackTrace(),"User is not removed from gym  node database and object.");
        }
    }

}