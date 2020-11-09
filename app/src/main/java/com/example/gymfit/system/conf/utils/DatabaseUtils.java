package com.example.gymfit.system.conf.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.main.ActivitySystemOnBoarding;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class DatabaseUtils {

    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String[] collections = ResourceUtils.getStringArrayFromID(R.array.collections);

    private static Context mContext;
    private static SharedPreferences preferences = mContext.getSharedPreferences("my_preferences", MODE_PRIVATE);


    public interface FindItemCallback<T> {
        void onCallback(T value);
    }

    // Get methods

    @SuppressWarnings("unchecked")
    public static void getGymByUID(@NonNull String uid, FindItemCallback<Gym> callback) {
        db.collection(collections[1]).document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot ds = task.getResult();
                        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

                        final String email = ds.getString(keys[2]) != null ? ds.getString(keys[2]) : "null";
                        final String phone = ds.getString(keys[4]) != null ? ds.getString(keys[4]) : "null";
                        final String name = ds.getString(keys[1]) != null ? ds.getString(keys[1]) : "null";
                        final String address = ds.getString(keys[5]) != null ? ds.getString(keys[5]) : "null";
                        final String image = ds.getString(keys[3]) != null ? ds.getString(keys[3]) : "null";

                        final LatLng position = ds.getGeoPoint(keys[6]) != null
                                ? new LatLng(
                                Objects.requireNonNull(ds.getGeoPoint(keys[6])).getLatitude(),
                                Objects.requireNonNull(ds.getGeoPoint(keys[6])).getLongitude())
                                : new LatLng(0, 0);

                        final List<String> subscribers = ds.get(keys[8]) != null
                                ? ((ArrayList<String>) ds.get(keys[8]))
                                : new ArrayList<>();

                        final Map<String, Boolean> subscription = ds.get(keys[7]) != null
                                ? (HashMap<String, Boolean>) ds.get(keys[7])
                                : getDefaultSubscription();

                        final Map<String, Boolean[]> turn = new TreeMap<>();
                        final Map<String, Object> turns = ds.get(keys[9]) != null
                                ? (HashMap<String, Object>) ds.get(keys[9])
                                : getDefaultTurn();
                        Objects.requireNonNull(turns).forEach((keyTurn, valueTurn) -> {
                            final Map<String, Boolean> turnTmp = Gym.sortValueTurn(keyTurn, (HashMap<String, Boolean>) valueTurn);
                            turn.put(keyTurn, Arrays.copyOf(
                                    turnTmp.values().toArray(),
                                    turnTmp.values().toArray().length,
                                    Boolean[].class));
                        });

                        final Gym gymTmp = new Gym(uid, email, phone, name, address, subscribers, position, image);
                        gymTmp.setSubscription(subscription);
                        gymTmp.setTurns(turn);
                        callback.onCallback(gymTmp);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found"));
    }

    // Initialization methods

    /**
     * Init and return a default map of boolean array for set turn gym's node
     *
     * @return Map of Boolean arrays
     */
    private static Map<String, Object> getDefaultTurn() {
        final String[] gymKeys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        return new HashMap<String, Object>() {
            {
                put(gymKeys[14], new Boolean[] { true, true, true });
                put(gymKeys[15], new Boolean[] { true, true, true });
                put(gymKeys[15], new Boolean[] { true, true, true });
            }
        };
    }

    /**
     * Init and return a default map of boolean for set subscription gym's node
     *
     * @return Map of Boolean
     */
    private static Map<String, Boolean> getDefaultSubscription() {
        final String[] gymKeys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        return new HashMap<String, Boolean>() {
            {
                put(gymKeys[10], true);
                put(gymKeys[11], true);
                put(gymKeys[12], true);
                put(gymKeys[13], true);
            }
        };
    }

    // Checked methods

    @SuppressWarnings("unchecked")
    public static void isGymSubscribersEmpty(@NotNull String uid, FindItemCallback<Boolean> callback) {
        db.collection(collections[1]).document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot ds = task.getResult();
                        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

                        final List<String> subscribers = ds.get(keys[8]) != null
                                ? ((ArrayList<String>) ds.get(keys[8]))
                                : new ArrayList<>();

                        callback.onCallback(subscribers.isEmpty());
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found"));
    }


    public static boolean isFirstTime(){
        boolean result = false;
        if(!preferences.getBoolean("onboarding_complete",false)){
            result = true;
        }
        return result;
    }

    public static boolean isLogged(){
        boolean result = false;
        if (preferences.getString("uid", null) != null){
            result = true;
        }
        return result;
    }

    public static String getUidLogged(){
        return preferences.getString("uid", null);
    }

    public static void setUidLogged(String uid){
        preferences.edit().putString("uid", uid).apply();
    }
}
