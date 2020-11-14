package com.example.gymfit.system.main;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.OnTurnFragment;
import com.example.gymfit.user.conf.User;
import com.example.gymfit.user.conf.UserViewPagerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSystemMainHelp#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSystemMainHelp<T extends GenericUser> extends Fragment {
    private static final String USER_KEY = "user_key";

    private View messageAnchor = null;

    private GenericUser user = null;

    public static <T extends GenericUser> FragmentSystemMainHelp<T> newInstance(@NonNull final T user) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserMainTurn created");

        final FragmentSystemMainHelp<T> fragment = new FragmentSystemMainHelp<>();
        final Bundle bundle = new Bundle();

        if (user instanceof User) {
            bundle.putParcelable(USER_KEY, (User) user);
        } else {
            bundle.putSerializable(USER_KEY, user);
        }

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getSerializable(USER_KEY) instanceof User) {
                this.user = (User) getArguments().getParcelable(USER_KEY);
            } else {
                this.user = (Gym) getArguments().getSerializable(USER_KEY);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_system_main_help, container, false);

        initSystemInterface(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentUserMainTurn layout XML created");

        return rootView;
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Orientation changed: " + newConfig.orientation);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            try {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                if (Build.VERSION.SDK_INT >= 26) {
                    ft.setReorderingAllowed(false);
                }
                ft.detach(this).attach(this).commit();

                AppUtils.log(Thread.currentThread().getStackTrace(), "Orientation changed: replaced interface");

            } catch (Exception e) {
                AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                AppUtils.message(this.messageAnchor, e.toString(), Snackbar.LENGTH_SHORT).show();
                AppUtils.restartActivity((AppCompatActivity) requireActivity());
            }
        }
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull final View rootView) {

        // init new checked item on navigation Drawer
        final NavigationView navigationView;
        if (this.user instanceof User) {
            navigationView = requireActivity().findViewById(R.id.navigation_user);
        } else {
            navigationView = requireActivity().findViewById(R.id.navigation_gym);
        }
        navigationView.getMenu().findItem(R.id.nav_menu_help).setChecked(true);

        // get ViewPager and TabLayout from Layout XML and init them
        final ViewPager viewPager = rootView.findViewById(R.id.system_view_pager);
        final TabLayout tabLayout = rootView.findViewById(R.id.menu_system_tab);

        // init refresher layout
        final SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.refresher);
        refreshLayout.setColorSchemeResources(R.color.tint_refresher,
                R.color.tint_refresher_first, R.color.tint_refresher_second, R.color.tint_refresher_third);

        final UserViewPagerAdapter adapter = new UserViewPagerAdapter(getChildFragmentManager(), 0);
        adapter.addFragment(FragmentSystemHelpFaq.newInstance(), getString(R.string.system_tab_help_faq));
        adapter.addFragment(FragmentSystemHelpAbout.newInstance(), getString(R.string.system_tab_help_about));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                final OnTurnFragment fragment = (OnTurnFragment) adapter.instantiateItem(viewPager, position);
                fragment.onFragmentBecomeVisible();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                toggleRefreshing(refreshLayout, state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        // if pull down with gesture refresh all picker turn from adapter
        refreshLayout.setOnRefreshListener(() -> {
            final OnTurnFragment fragment = (OnTurnFragment) adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
            AppUtils.message(this.messageAnchor, getString(R.string.refresh_system_info), Snackbar.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                refreshLayout.setRefreshing(false);
                fragment.onFragmentRefresh();
            }, AppUtils.getRandomDelayMillis());

        });

        // Abilities toolbar item options
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.system_toolbar_help));

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentSystemMainHelp initialized");
    }

    private void toggleRefreshing(@NonNull final SwipeRefreshLayout swipeRefreshLayout, final boolean enabled) {
        swipeRefreshLayout.setEnabled(enabled);
    }

}