package com.example.gymfit.system.main;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gymfit.BuildConfig;
import com.example.gymfit.R;
import com.example.gymfit.system.conf.recycleview.ListDevelopersAdapter;
import com.example.gymfit.system.conf.recycleview.OnItemClickListener;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.OnTurnFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSystemHelpAbout#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSystemHelpAbout extends Fragment implements OnTurnFragment {

    private final AtomicBoolean fragmentLaid = new AtomicBoolean(false);
    private View messageAnchor = null;

    public static FragmentSystemHelpAbout newInstance() {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentSystemHelpAbout created");
        return new FragmentSystemHelpAbout();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_system_help_about, container, false);

        initSystemInterface(rootView);
        initInterface(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentSystemHelpAbout layout XML created");

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.fragmentLaid.set(true);
    }

    @Override
    public void onFragmentBecomeVisible() {
        // if change fragment refresh system info
        if (fragmentLaid.get()) {
            refreshSystemInfo();
        }
    }

    @Override
    public void onFragmentRefresh() {
        refreshSystemInfo();
        AppUtils.message(this.messageAnchor, getString(R.string.refresh_completed), Snackbar.LENGTH_SHORT).show();
        AppUtils.log(Thread.currentThread().getStackTrace(), "Refresh system info.");
    }

    // Interface methods

    /**
     * Initialize Snackbar anchor
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull final View rootView) {
        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentSystemHelpAbout initialized");
    }

    /**
     *
     *
     */
    private void initInterface(@NonNull final View rootView) {
        final List<String> names = Arrays.asList(rootView.getResources().getStringArray(R.array.system_developer_name));
        final List<Object[]> developers = new ArrayList<Object[]>() {
            {
                add(new Object[] { names.get(0), ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_dev_01_round, null) });
                add(new Object[] { names.get(1), ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_dev_02_round, null) });
                add(new Object[] { names.get(2), ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_dev_03_round, null) });
                add(new Object[] { names.get(3), ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_dev_04_round, null) });
            }
        };

        setDeveloperRecycleview(rootView, developers, (viewHolder, position) -> {});

        final MaterialTextView deviceOSVersion = rootView.findViewById(R.id.device_os_version);
        deviceOSVersion.setText(android.os.Build.VERSION.RELEASE);

        final MaterialTextView deviceAppVersion = rootView.findViewById(R.id.app_version);
        deviceAppVersion.setText(BuildConfig.VERSION_NAME);

        final MaterialTextView deviceCountry = rootView.findViewById(R.id.device_country);
        final String country = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).getCountry();
        deviceCountry.setText(country);

        final MaterialTextView deviceLang = rootView.findViewById(R.id.device_lang);
        final String lang = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).getLanguage();
        deviceLang.setText(lang);

        final MaterialTextView deviceConn = rootView.findViewById(R.id.device_conn);
        final String conn = getNetworkClass(rootView.getContext());
        deviceConn.setText(conn);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Interface of FragmentSystemHelpAbout initialized");
    }

    private void setDeveloperRecycleview(@NonNull final View rootView, @NonNull final List<Object[]> developers,
                                         @NonNull final OnItemClickListener listener) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.developer_rv);
        recyclerView.setHasFixedSize(false);

        if ((rootView.getResources().getConfiguration().orientation) == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 4));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 2));
        }


        ListDevelopersAdapter developersAdapter = new ListDevelopersAdapter(rootView.getContext(), developers, listener);
        recyclerView.setAdapter(developersAdapter);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Developer adapter initialized");
    }

    // Other methods

    @NonNull
    private static String getNetworkClass(@NonNull final Context context) {
        final String[] systemConnectionTypes = context.getResources().getStringArray(R.array.system_connection_types);
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected())
            return systemConnectionTypes[0]; // not connected
        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return systemConnectionTypes[1];
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:     // api< 8: replace by 11
                case TelephonyManager.NETWORK_TYPE_GSM:      // api<25: replace by 16
                    return systemConnectionTypes[3];
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:   // api< 9: replace by 12
                case TelephonyManager.NETWORK_TYPE_EHRPD:    // api<11: replace by 14
                case TelephonyManager.NETWORK_TYPE_HSPAP:    // api<13: replace by 15
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA: // api<25: replace by 17
                    return systemConnectionTypes[4];
                case TelephonyManager.NETWORK_TYPE_LTE:      // api<11: replace by 13
                case TelephonyManager.NETWORK_TYPE_IWLAN:    // api<25: replace by 18
                case 19: // LTE_CA
                    return systemConnectionTypes[5];
                case TelephonyManager.NETWORK_TYPE_NR:       // api<29: replace by 20
                    return systemConnectionTypes[6];
                default:
                    return systemConnectionTypes[0];
            }
        }
        if (info.getType() == ConnectivityManager.TYPE_ETHERNET)
            return systemConnectionTypes[2];
        return systemConnectionTypes[0];
    }

    private void refreshSystemInfo() {
        final MaterialTextView deviceOSVersion = requireView().findViewById(R.id.device_os_version);
        deviceOSVersion.setText(android.os.Build.VERSION.RELEASE);

        final MaterialTextView deviceAppVersion = requireView().findViewById(R.id.app_version);
        deviceAppVersion.setText(BuildConfig.VERSION_NAME);

        final MaterialTextView deviceCountry = requireView().findViewById(R.id.device_country);
        final String country = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).getCountry();
        deviceCountry.setText(country);

        final MaterialTextView deviceLang = requireView().findViewById(R.id.device_lang);
        final String lang = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).getLanguage();
        deviceLang.setText(lang);

        final MaterialTextView deviceConn = requireView().findViewById(R.id.device_conn);
        final String conn = getNetworkClass(requireContext());
        deviceConn.setText(conn);
    }

}