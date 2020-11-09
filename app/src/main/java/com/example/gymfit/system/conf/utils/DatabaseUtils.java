package com.example.gymfit.system.conf.utils;

import android.annotation.SuppressLint;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class DatabaseUtils {

    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String[] collections = ResourceUtils.getStringArrayFromID(R.array.collections);

    public interface FindItemCallback<T> {
        void onCallback(T result);
    }

    // Get Gym methods

    @SuppressWarnings("unchecked")
    public static void getGym(@NonNull final String uid, FindItemCallback<Gym> callback) {
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
                                : getDefaultGymSubscription();

                        final Map<String, Boolean[]> turn = new TreeMap<>();
                        final Map<String, Object> turns = ds.get(keys[9]) != null
                                ? (HashMap<String, Object>) ds.get(keys[9])
                                : getDefaultGymTurn();
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
                .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found. " + e.getMessage()));
    }

    // Set User methods

    public static void updateUserTurn(@NonNull final String uid, @NonNull final Map<String, Object> turn, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[11], FieldValue.arrayUnion(turn))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User updated.");
                    callback.onCallback(true);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User not updated. " + e.getMessage());
                    callback.onCallback(false);
                });
    }

    public static void removeUserTurn(@NonNull final String uid, @NonNull final Map<String, Object> turn, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);
        final DocumentReference reference = db.collection(collections[0]).document(uid);

        reference.update(keys[11], FieldValue.arrayRemove(turn))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User updated.");
                    isUserTurnsEmpty(uid, result -> {
                        if (result) {
                            reference.update(keys[11], null)
                                    .addOnSuccessListener(aVoid1 -> AppUtils.log(Thread.currentThread().getStackTrace(), "User turns node cleared"))
                                    .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "User turns node not cleared. " + e.getMessage()));
                        } else {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "User turns node not cleared");
                        }
                    });
                    callback.onCallback(true);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User not updated. " + e.getMessage());
                    callback.onCallback(false);
                });
    }

    public static void removeUserTurns(@NonNull final String uid, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[11], null)
                .addOnSuccessListener(aVoid1 -> {
                    callback.onCallback(true);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turns node cleared");
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(false);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turns node not cleared. " + e.getMessage());
                });
    }

    // Checked Gym methods

    public static void isGymSubscribersEmpty(@NotNull final String uid, FindItemCallback<Boolean> callback) {
        db.collection(collections[1]).document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && (task.getResult() != null)) {
                        final DocumentSnapshot ds = task.getResult();
                        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

                        @SuppressWarnings("unchecked")
                        final List<String> subscribers = ds.get(keys[8]) != null
                                ? ((ArrayList<String>) ds.get(keys[8]))
                                : new ArrayList<>();
                        callback.onCallback(subscribers != null && subscribers.isEmpty());
                    } else {
                        callback.onCallback(false);
                    }
                })
                .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found. " + e.getMessage()));
    }

    // Checked User methods

    private static void isUserTurnsEmpty(@NotNull final String uid, FindItemCallback<Boolean> callback) {
        db.collection(collections[0]).document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && (task.getResult() != null)) {
                        final DocumentSnapshot ds = task.getResult();
                        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

                        @SuppressWarnings("unchecked")
                        final List<Map<String, Object>> turns = ds.get(keys[11]) != null
                                ? (ArrayList<Map<String, Object>>) ds.get(keys[11])
                                : new ArrayList<>();
                        callback.onCallback(turns != null && turns.isEmpty());
                    } else {
                        callback.onCallback(false);
                    }
                })
                .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "User not found." + e.getMessage()));
    }

    // Other methods

    /**
     * Init and return a default map of boolean array for set turn gym's node
     *
     * @return Map of Boolean arrays
     */
    @NonNull
    private static Map<String, Object> getDefaultGymTurn() {
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
    @NonNull
    private static Map<String, Boolean> getDefaultGymSubscription() {
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

}
