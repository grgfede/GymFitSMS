package com.example.gymfit.system.conf.utils;

import android.annotation.SuppressLint;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.user.conf.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseUtils {
    public static final int RESULT_OK = 1;
    public static final int RESULT_FAILED = 0;
    public static final int RESULT_VOID = -1;

    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String[] collections = ResourceUtils.getStringArrayFromID(R.array.collections);

    public interface FindItemCallback<T> {
        void onCallback(@Nullable T data, final int result);
    }

    // Get methods

    /**
     * Get Gym object with default values if the respective field is null into Database,
     * otherwise take from it current value registered.
     *
     * @param uid string value used to join into correct document of Database and pick Gym nodes
     * @param callback a functional interface callback used to a result operation in the case of: success, fail and void
     */
    @SuppressWarnings("unchecked")
    public static void getGym(@NonNull final String uid, FindItemCallback<Gym> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        final DocumentReference reference = db.collection(collections[1]).document(uid);

        reference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && (task.getResult() != null)) {
                        final DocumentSnapshot ds = task.getResult();

                        final String email = ds.getString(keys[2]) != null ? ds.getString(keys[2]) : "null";
                        final String phone = ds.getString(keys[4]) != null ? ds.getString(keys[4]) : "null";
                        final String name = ds.getString(keys[1]) != null ? ds.getString(keys[1]) : "null";
                        final String address = ds.getString(keys[5]) != null ? ds.getString(keys[5]) : "null";
                        final String img = ds.getString(keys[3]) != null
                                ? ds.getString(keys[3])
                                : ResourceUtils.getURIForResource(R.drawable.default_user);;
                        final LatLng position = ds.getGeoPoint(keys[6]) != null
                                ? new LatLng(
                                Objects.requireNonNull(ds.getGeoPoint(keys[6])).getLatitude(),
                                Objects.requireNonNull(ds.getGeoPoint(keys[6])).getLongitude())
                                : new LatLng(0, 0);
                        final List<String> subscribers = ds.get(keys[8]) != null
                                ? ((ArrayList<String>) ds.get(keys[8]))
                                : new ArrayList<>();

                        final AtomicBoolean isSubscriptionNewest = new AtomicBoolean(false);
                        final Map<String, Boolean> subscription;
                        if (ds.get(keys[7]) != null) {
                            subscription = (HashMap<String, Boolean>) ds.get(keys[7]);
                        } else {
                            subscription = Gym.getDefaultGymSubscription();
                            isSubscriptionNewest.set(true);
                        }

                        final Map<String, Boolean[]> turn = new TreeMap<>();
                        final AtomicBoolean isTurnNewest = new AtomicBoolean(false);
                        final Map<String, Map<String, Boolean>> turns;
                        if (ds.get(keys[9]) != null) {
                            turns = (HashMap<String, Map<String, Boolean>>) ds.get(keys[9]);
                        } else {
                            turns = Gym.getDefaultGymDatabaseTurn();
                            isTurnNewest.set(true);
                        }
                        Objects.requireNonNull(turns).forEach((keyTurn, valueTurn) -> {
                            final Map<String, Boolean> turnTmp = Gym.sortValueTurn(keyTurn, (HashMap<String, Boolean>) valueTurn);
                            turn.put(keyTurn, Arrays.copyOf(
                                    turnTmp.values().toArray(),
                                    turnTmp.values().toArray().length,
                                    Boolean[].class));
                        });

                        final Gym gym = new Gym(uid, email, phone, name, address, subscribers, position, img);
                        gym.setSubscriptions(subscription);
                        gym.setIsSubscriptionNewest(isSubscriptionNewest.get());
                        gym.setTurns(turn);
                        gym.setIsTurnNewest(isTurnNewest.get());
                        callback.onCallback(gym, RESULT_OK);
                    } else {
                        callback.onCallback(null, RESULT_VOID);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(null, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found. " + e.getMessage());
                });
    }

    /**
     * Get User object with default values if the respective field is null into Database,
     * otherwise take from it current value registered.
     *
     * @param uid string value used to join into correct document of Database and pick User nodes
     * @param callback a functional interface callback used to a result operation in the case of: success, fail and void
     */
    @SuppressWarnings("unchecked")
    public static void getUser(@NonNull final String uid, FindItemCallback<User> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);
        final List<String> genderKeys = Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.genders_name));
        final String[] genderValues = new String[] {
                ResourceUtils.getStringFromID(R.string.prompt_gender_man),
                ResourceUtils.getStringFromID(R.string.prompt_gender_woman),
                ResourceUtils.getStringFromID(R.string.prompt_gender_other)
        };
        final DocumentReference reference = db.collection(collections[0]).document(uid);

        reference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && (task.getResult() != null)) {
                        final DocumentSnapshot ds = task.getResult();

                        final String name = ds.get(keys[1]) != null ? ds.getString(keys[1]) : "null";
                        final String surname = ds.get(keys[2]) != null ? ds.getString(keys[2]) : "null";
                        final Date dateOfBirthday = ds.get(keys[4]) != null
                                ? Objects.requireNonNull(ds.getTimestamp(keys[4])).toDate()
                                : new GregorianCalendar(1900, Calendar.JANUARY, 1).getTime();
                        final String email = ds.get(keys[5]) != null ? ds.getString(keys[5]) : "null";
                        final String gender = ds.get(keys[6]) != null
                                ?  genderValues[genderKeys.indexOf(String.valueOf(ds.get(keys[6])))]
                                : "null";
                        final String phone = ds.get(keys[7]) != null ? ds.getString(keys[7]) : "null";
                        final String img = ds.get(keys[8]) != null
                                ? ds.getString(keys[8])
                                : ResourceUtils.getURIForResource(R.drawable.default_user);
                        final String address = ds.get(keys[9]) != null ? ds.getString(keys[9]) : "null";
                        final String[] subscription = ds.get(keys[10]) != null
                                ?  ((ArrayList<String>) Objects.requireNonNull(ds.get(keys[10]))).toArray(new String[0])
                                : new String[] {"null", "null"};
                        final ArrayList<Map<String, Object>> turns = ds.get(keys[11]) != null
                                ? (ArrayList<Map<String, Object>>) ds.get(keys[11])
                                : new ArrayList<>();

                        final User user = new User(uid, name, surname, email, dateOfBirthday, address, gender, img, phone, subscription, turns);
                        callback.onCallback(user, RESULT_OK);
                    } else {
                        callback.onCallback(null, RESULT_VOID);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(null, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User not found. " + e.getMessage());
                });
    }

    public static void getGymsID(FindItemCallback<List<String>> callback) {
        db.collection(collections[1]).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<String> gymsID = new ArrayList<>();

                        if (!task.getResult().getDocuments().isEmpty()) {
                            for (QueryDocumentSnapshot ds : task.getResult()) {
                                // Take all Gym document from Database
                                gymsID.add(ds.getId());
                            }
                            callback.onCallback(gymsID, RESULT_OK);
                        } else {
                            callback.onCallback(null, RESULT_VOID);
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found. ");
                        }
                    } else {
                        callback.onCallback(null, RESULT_VOID);
                        AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found. ");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(null, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Collection not found. " + e.getMessage());
                });

    }

    public static void getUsersID(FindItemCallback<List<String>> callback) {
        db.collection(collections[0]).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<String> usersID = new ArrayList<>();

                        if (!task.getResult().getDocuments().isEmpty()) {
                            for (QueryDocumentSnapshot ds : task.getResult()) {
                                // Take all Gym document from Database
                                usersID.add(ds.getId());
                            }
                            callback.onCallback(usersID, RESULT_OK);
                        } else {
                            callback.onCallback(null, RESULT_VOID);
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Users not found. ");
                        }
                    } else {
                        callback.onCallback(null, RESULT_VOID);
                        AppUtils.log(Thread.currentThread().getStackTrace(), "Users not found. ");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(null, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Collection not found. " + e.getMessage());
                });

    }

    // Update methods

    public static void updateGymSubscribers(@NonNull final String uid, @NonNull final String subscribe, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[8], FieldValue.arrayUnion(subscribe))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscribers updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscribers not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymField(@NonNull final String uid, @NonNull final String key, @NonNull final String value, FindItemCallback<Boolean> callback) {
        db.collection(collections[1]).document(uid)
                .update(key, value)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym " + key + " updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym " + key + " not updated." + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymImg(@NonNull final String uid, @NonNull final String img, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[3], img)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym img updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym img not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymPosition(@NonNull final String uid, @NonNull final LatLng position, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[6], new GeoPoint(position.latitude, position.longitude))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym position updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym position not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymSubscription(@NonNull final String uid, @NonNull final String key, final boolean isEnable, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[7] + "." + key, isEnable)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscription " + key + " updated with: " + isEnable);
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscription " + key + " not updated." + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymSubscriptions(@NonNull final String uid, @NonNull final Map<String, Boolean> subscription, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[7], subscription)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscriptions updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscriptions not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymTurn(@NonNull final String uid, @NonNull final String category, @NonNull final String key, final boolean isEnable, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[9] + "." + category + "." + key, isEnable)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym turn " + key + " updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym turn " + key + " not updated." + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateGymTurns(@NonNull final String uid, @NonNull final Map<String, Map<String, Boolean>> turns, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        db.collection(collections[1]).document(uid)
                .update(keys[9], turns)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym turns updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym turns not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateUserField(@NonNull final String uid, @NonNull final String key, @NonNull final String value, FindItemCallback<Boolean> callback) {
        db.collection(collections[0]).document(uid)
                .update(key, value)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User " + key + " updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User " + key + " not updated." + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateUserImg(@NonNull final String uid, @NonNull final String img, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[8], img)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User img updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User img not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateUserPosition(@NonNull final String uid, @NonNull final LatLng position, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[14], new HashMap<String, Double>() {
                    {
                        put(keys[15], position.latitude);
                        put(keys[16], position.longitude);
                    }
                })
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym position updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym position not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateUserDateOfBirthday(@NonNull final String uid, @NonNull final Date dateOfBirthday, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[4], new Timestamp(dateOfBirthday))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User dateOfBirthday updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User dateOfBirthday not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateUserTurn(@NonNull final String uid, @NonNull final Map<String, Object> turn, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[11], FieldValue.arrayUnion(turn))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turn updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turn not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void updateUserSubscription(@NonNull final String uid, @NonNull final String[] subscription, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[10], FieldValue.arrayUnion(subscription[0], subscription[1]))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User subscription updated.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User subscription not updated. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    // Remove methods

    public static void removeGymSubscriber(@NonNull final String uid, @NonNull final String subscribe, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        final DocumentReference reference = db.collection(collections[1]).document(uid);

        reference.update(keys[8], FieldValue.arrayRemove(subscribe))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscriber removed.");
                    isGymSubscribersEmpty(uid, ((data, result) -> {
                        if (data) {
                            reference.update(keys[8], null)
                                    .addOnSuccessListener(aVoid1 -> AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscribers cleared"))
                                    .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscribers not cleared. " + e.getMessage()));
                        } else {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscribers not cleared");
                        }
                    }));
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscriber not removed. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void removeUserTurn(@NonNull final String uid, @NonNull final Map<String, Object> turn, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);
        final DocumentReference reference = db.collection(collections[0]).document(uid);

        reference.update(keys[11], FieldValue.arrayRemove(turn))
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turn removed.");
                    isUserTurnsEmpty(uid, (data, result) -> {
                        if (data) {
                            reference.update(keys[11], null)
                                    .addOnSuccessListener(aVoid1 -> AppUtils.log(Thread.currentThread().getStackTrace(), "User turns cleared"))
                                    .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "User turns not cleared. " + e.getMessage()));
                        } else {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "User turns not cleared");
                        }
                    });
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turn not removed. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    public static void removeUserTurns(@NonNull final String uid, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[11], null)
                .addOnSuccessListener(aVoid1 -> {
                    callback.onCallback(true, RESULT_OK);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turns cleared");
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(false, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User turns not cleared. " + e.getMessage());
                });
    }

    public static void removeUserSubscription(@NonNull final String uid, FindItemCallback<Boolean> callback) {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);

        db.collection(collections[0]).document(uid)
                .update(keys[10], null)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User subscription cleared.");
                    callback.onCallback(true, RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User subscription not cleared. " + e.getMessage());
                    callback.onCallback(false, RESULT_FAILED);
                });
    }

    // Checked methods

    private static void isGymSubscribersEmpty(@NotNull final String uid, FindItemCallback<Boolean> callback) {
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
                        callback.onCallback(subscribers != null && subscribers.isEmpty(), RESULT_OK);
                    } else {
                        callback.onCallback(false, RESULT_VOID);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(false, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found. " + e.getMessage());
                });
    }

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
                        callback.onCallback(turns != null && turns.isEmpty(), RESULT_OK);
                    } else {
                        callback.onCallback(false, RESULT_VOID);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(false, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User not found." + e.getMessage());
                });
    }

    public static void isGymContains(@NotNull final String uid, FindItemCallback<Boolean> callback) {
        db.collection(collections[1]).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().isEmpty()) {
                            final List<DocumentSnapshot> qs = task.getResult().getDocuments();
                            callback.onCallback(qs.stream().anyMatch(ds -> ds.getId().equals(uid)), RESULT_OK);
                        } else {
                            callback.onCallback(false, RESULT_VOID);
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found. ");
                        }
                    } else {
                        callback.onCallback(false, RESULT_VOID);
                        AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found. ");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(false, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Collection not found. " + e.getMessage());
                });
    }

    public static void isUserContains(@NotNull final String uid, FindItemCallback<Boolean> callback) {
        db.collection(collections[0]).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().isEmpty()) {
                            final List<DocumentSnapshot> qs = task.getResult().getDocuments();
                            callback.onCallback(qs.stream().anyMatch(ds -> ds.getId().equals(uid)), RESULT_OK);
                        } else {
                            callback.onCallback(false, RESULT_VOID);
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Users not found. ");
                        }
                    } else {
                        callback.onCallback(false, RESULT_VOID);
                        AppUtils.log(Thread.currentThread().getStackTrace(), "Users not found. ");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(false, RESULT_FAILED);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Collection not found. " + e.getMessage());
                });
    }

}
