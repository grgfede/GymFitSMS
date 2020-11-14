package com.example.gymfit.system.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.OnTurnFragment;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSystemHelpFaq#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSystemHelpFaq extends Fragment implements OnTurnFragment {

    private final AtomicBoolean fragmentLaid = new AtomicBoolean(false);

    public static FragmentSystemHelpFaq newInstance() {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentSystemHelpFaq created");
        return new FragmentSystemHelpFaq();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_system_help_faq, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.fragmentLaid.set(true);
    }

    @Override
    public void onFragmentBecomeVisible() {
    }

    @Override
    public void onFragmentRefresh() {
    }

}