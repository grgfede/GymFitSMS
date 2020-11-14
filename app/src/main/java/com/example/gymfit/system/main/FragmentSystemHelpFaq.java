package com.example.gymfit.system.main;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.gymfit.BuildConfig;
import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.recycleview.ListDevelopersAdapter;
import com.example.gymfit.system.conf.recycleview.ListFaqAdapter;
import com.example.gymfit.system.conf.recycleview.OnItemClickListener;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.OnTurnFragment;
import com.example.gymfit.user.conf.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSystemHelpFaq#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSystemHelpFaq extends Fragment implements OnTurnFragment, OnItemClickListener {
    private static final String USER_KEY = "user_key";

    private View messageAnchor = null;

    private GenericUser user = null;

    private final AtomicBoolean fragmentLaid = new AtomicBoolean(false);
    private final Map<String, Boolean> faqStatus = new HashMap<>();

    public static <T extends GenericUser> FragmentSystemHelpFaq newInstance(@NonNull final T user) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentSystemHelpFaq created");

        final FragmentSystemHelpFaq fragment = new FragmentSystemHelpFaq();
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
        final View rootView = inflater.inflate(R.layout.fragment_system_help_faq, container, false);

        initSystemInterface(rootView);
        initInterface(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentSystemHelpFaq layout XML created");

        return rootView;
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
        AppUtils.message(this.messageAnchor, getString(R.string.refresh_completed), Snackbar.LENGTH_SHORT).show();
        AppUtils.log(Thread.currentThread().getStackTrace(), "Refresh system info.");
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        isListVisibility(
                ((ListFaqAdapter.MyViewHolder) viewHolder).getAnswerContainer(),
                ((ListFaqAdapter.MyViewHolder) viewHolder).getEndIcon(),
                ((ListFaqAdapter.MyViewHolder) viewHolder).getCode()
        );
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

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentSystemHelpFaq initialized");
    }

    /**
     * Initialize Faq recycleview with gym/user questions and respective answers.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initInterface(@NonNull final View rootView) {
        final List<String[]> faqs = new ArrayList<>();
        final String[] questions;
        final String[] answers;
        final String[] code;

        if (this.user instanceof User) {
            questions = rootView.getResources().getStringArray(R.array.user_questions);
            answers = rootView.getResources().getStringArray(R.array.user_answers);
        } else {
            questions = rootView.getResources().getStringArray(R.array.gym_questions);
            answers = rootView.getResources().getStringArray(R.array.gym_answers);
        }

        code = generateRandomUUID(questions.length);
        for (int i=0; i<questions.length; i++) {
            faqs.add(new String[] {
                    questions[i],
                    answers[i],
                    code[i]
            });
        }

        setFaqRecyclerView(rootView, faqs, this);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Interface of FragmentSystemHelpFaq initialized");
    }

    private void setFaqRecyclerView(@NonNull final View rootView, @NonNull final List<String[]> faqs, @NonNull final OnItemClickListener listener) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.faq_rv);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 1));

        final ListFaqAdapter developersAdapter = new ListFaqAdapter(rootView.getContext(), faqs, listener);
        recyclerView.setAdapter(developersAdapter);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Faq adapter initialized");
    }

    // Other methods

    private void isListVisibility(@NonNull final LinearLayout container, @NonNull final ImageView arrow, @NonNull final String viewName) {

        // reaction of null pointer with a new creation of card visibility
        if (!this.faqStatus.containsKey(viewName)) {
            this.faqStatus.put(viewName, false);
        }

        // if card selected is not in visible mode so enable it and replace its state, icon and layout height
        if (!Objects.requireNonNull(this.faqStatus.get(viewName))) {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up));
            AppUtils.expandCard(container);
            this.faqStatus.replace(viewName, true);
        } else {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down));
            AppUtils.collapseCard(container);
            this.faqStatus.replace(viewName, false);
        }
    }

    private String[] generateRandomUUID(final int count) {
        final String[] codes = new String[count];
        for (int i=0; i<count; i++) {
            codes[i] = UUID.randomUUID().toString() + i;
        }

        return codes;
    }


}