package com.example.gymfit.gym.conf;

import androidx.annotation.NonNull;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gym extends GenericUser implements Serializable {
    private String address;
    private String name;
    private String image;
    private final Double[] positionArray;
    private final List<String> subscribers;
    private final Map<String, Boolean> subscription; // 0 - monthly, 1 - quarterly, 2 - sixMonth, 3 - annual
    private final Map<String, Boolean[]> turns;

    private final AtomicBoolean isSubscriptionNewest = new AtomicBoolean(false);
    private final AtomicBoolean isTurnNewest = new AtomicBoolean(false);

    public Gym(@NonNull final String uid, @NonNull final String email, @NonNull final String phone,
               @NonNull final String name, @NonNull final String address, @NonNull final List<String> subscribers,
               @NonNull final LatLng position, @NonNull final String image) {
        super(uid, email, phone);
        this.name = name;
        this.address = address;
        this.positionArray = new Double[]{position.latitude, position.longitude};
        this.image = image;
        this.subscription = new HashMap<String, Boolean>() {
            {
                put("monthly", true);
                put("quarterly", true);
                put("sixMonth", true);
                put("annual", true);
            }
        };
        this.turns = new HashMap<String, Boolean[]>() {
            {
                put("morning", new Boolean[] {true, true, true});
                put("afternoon", new Boolean[] {true, true, true});
                put("evening", new Boolean[] {true, true, true});
            }
        };
        this.subscribers = subscribers;
    }

    // Get methods

    @NonNull
    public Map<String, Boolean[]> getTurns() {
        return turns;
    }

    @NonNull
    public Map<String, Boolean> getSubscription() {
        return subscription;
    }

    @NonNull
    public List<String> getSubscribers() {
        return subscribers;
    }

    @NonNull
    public String getImage() {
        return image;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    @NonNull
    public LatLng getPosition() {
        return new LatLng(this.positionArray[0], this.positionArray[1]);
    }

    @NonNull
    public String getName() {
        return name;
    }

    // Set methods

    public void setAddress(@NonNull final String address) {
        this.address = address;
    }

    public void setName(@NonNull final String name) {
        this.name = name;
    }

    public void setPosition(@NonNull final LatLng position) {
        this.positionArray[0] = position.latitude;
        this.positionArray[1] = position.longitude;
    }

    public void setImage(@NonNull final String image) {
        this.image = image;
    }

    public void setSubscription(@NonNull final String key, @NonNull final Boolean value) {
        this.subscription.replace(key, value);
    }

    public void setSubscriptions(@NonNull final Map<String, Boolean> subscription) {
        this.subscription.clear();
        this.subscription.putAll(subscription);
    }

    public void setTurn(@NonNull final String key, final int position, final boolean value) {
        Objects.requireNonNull(this.turns.get(key))[position] = value;
    }

    public void setTurns(@NonNull final Map<String, Boolean[]> turns) {
        this.turns.clear();
        this.turns.putAll(turns);
    }

    public void setSubscribers(@NonNull final List<String> subscribers) {
        this.subscribers.clear();
        this.subscribers.addAll(subscribers);
    }

    public void setIsSubscriptionNewest(@NonNull final Boolean isSubscriptionNewest) {
        this.isSubscriptionNewest.set(isSubscriptionNewest);
    }

    public void setIsTurnNewest(@NonNull final Boolean isTurnNewest) {
        this.isTurnNewest.set(isTurnNewest);
    }

    // Remove methods

    public void removeSubscriber(@NonNull final String subscriber) {
        this.subscribers.remove(subscriber);
    }

    // Sort methods

    @NonNull
    public static Map<String, Boolean> sortValueTurn(@NonNull final String keyTurn, @NonNull final HashMap<String, Boolean> turn) {
        final String[] gymKeys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        final String[] turnKeys = new String[] { gymKeys[14], gymKeys[15], gymKeys[16] };
        final String[] morningTurnKeys = ResourceUtils.getStringArrayFromID(R.array.morning_session_name);
        final String[] afternoonTurnKeys = ResourceUtils.getStringArrayFromID(R.array.afternoon_session_name);
        final String[] eveningTurnKeys = ResourceUtils.getStringArrayFromID(R.array.evening_session_name);

        final String[] keyArray = Arrays.copyOf(turn.keySet().toArray(), turn.keySet().toArray().length, String[].class);
        final Boolean[] entryArray = Arrays.copyOf(turn.values().toArray(), turn.values().toArray().length, Boolean[].class);

        final String[] sortKeyArray = new String[3];
        final Boolean[] sortEntryArray = new Boolean[3];

        if (keyTurn.equals(turnKeys[0])) {
            for (int i=0; i<3; i++) {
                if (keyArray[i].equals(morningTurnKeys[0])) {
                    sortKeyArray[0] = morningTurnKeys[0];
                    sortEntryArray[0] = entryArray[i];
                } else if (keyArray[i].equals(morningTurnKeys[1])) {
                    sortKeyArray[1] = morningTurnKeys[1];
                    sortEntryArray[1] = entryArray[i];
                } else if (keyArray[i].equals(morningTurnKeys[2])) {
                    sortKeyArray[2] = morningTurnKeys[2];
                    sortEntryArray[2] = entryArray[i];
                }
            }
        } else if (keyTurn.equals(turnKeys[1])) {
            for (int i=0; i<3; i++) {
                if (keyArray[i].equals(afternoonTurnKeys[0])) {
                    sortKeyArray[0] = afternoonTurnKeys[0];
                    sortEntryArray[0] = entryArray[i];
                } else if (keyArray[i].equals(afternoonTurnKeys[1])) {
                    sortKeyArray[1] = afternoonTurnKeys[1];
                    sortEntryArray[1] = entryArray[i];
                } else if (keyArray[i].equals(afternoonTurnKeys[2])) {
                    sortKeyArray[2] = afternoonTurnKeys[2];
                    sortEntryArray[2] = entryArray[i];
                }
            }
        } else {
            for (int i=0; i<3; i++) {
                if (keyArray[i].equals(eveningTurnKeys[0])) {
                    sortKeyArray[0] = eveningTurnKeys[0];
                    sortEntryArray[0] = entryArray[i];
                } else if (keyArray[i].equals(eveningTurnKeys[1])) {
                    sortKeyArray[1] = eveningTurnKeys[1];
                    sortEntryArray[1] = entryArray[i];
                } else if (keyArray[i].equals(eveningTurnKeys[2])) {
                    sortKeyArray[2] = eveningTurnKeys[2];
                    sortEntryArray[2] = entryArray[i];
                }
            }
        }

        Map<String, Boolean> turnTmp = new TreeMap<>();
        for (int i=0; i<3; i++) {
            turnTmp.put(sortKeyArray[i], sortEntryArray[i]);
        }

        return turnTmp;
    }

    // Other methods

    @NonNull
    public Map<String, Boolean> getTranslatedSubscriptions() {
        final Map<String, Boolean> subscriptionMap = new TreeMap<>();
        final String[] subscriptionTranslatedKeys = new String[] {
                ResourceUtils.getStringFromID(R.string.monthly_subscription),
                ResourceUtils.getStringFromID(R.string.quarterly_subscription),
                ResourceUtils.getStringFromID(R.string.six_month_subscription),
                ResourceUtils.getStringFromID(R.string.annual_subscription)
        };

        this.subscription.forEach((key, isAvailable) -> {
            switch (key) {
                case "monthly":
                    subscriptionMap.put(subscriptionTranslatedKeys[0], isAvailable);
                    break;
                case "quarterly":
                    subscriptionMap.put(subscriptionTranslatedKeys[1], isAvailable);
                    break;
                case "sixMonth":
                    subscriptionMap.put(subscriptionTranslatedKeys[2], isAvailable);
                    break;
                case "annual":
                    subscriptionMap.put(subscriptionTranslatedKeys[3], isAvailable);
                    break;
            }
        });

        return subscriptionMap;
    }

    @NotNull
    public static String getSubscriptionFromTranslated(@NotNull final String subscriptionTranslated) {
        final List<String> translatedKeys = new ArrayList<String>() {
            {
                add(ResourceUtils.getStringFromID(R.string.monthly_subscription));
                add(ResourceUtils.getStringFromID(R.string.quarterly_subscription));
                add(ResourceUtils.getStringFromID(R.string.six_month_subscription));
                add(ResourceUtils.getStringFromID(R.string.annual_subscription));
            }
        };
        final String[] keys = new String[] {
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[10],
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[11],
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[12],
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[13]
        };

        return  keys[translatedKeys.indexOf(subscriptionTranslated)];
    }

    public static String getTranslatedFromSubscription(@NotNull final String subscription) {
        final List<String> keys = new ArrayList<String>() {
            {
                add(ResourceUtils.getStringArrayFromID(R.array.gym_field)[10]);
                add(ResourceUtils.getStringArrayFromID(R.array.gym_field)[11]);
                add(ResourceUtils.getStringArrayFromID(R.array.gym_field)[12]);
                add(ResourceUtils.getStringArrayFromID(R.array.gym_field)[13]);
            }
        };
        final String[] translatedKeys = new String[] {
                ResourceUtils.getStringFromID(R.string.monthly_subscription),
                ResourceUtils.getStringFromID(R.string.quarterly_subscription),
                ResourceUtils.getStringFromID(R.string.six_month_subscription),
                ResourceUtils.getStringFromID(R.string.annual_subscription)
        };

        return translatedKeys[keys.indexOf(subscription)];
    }

    /**
     * Init and return a default map of boolean array for set turn database gym's node
     *
     * @return Map inserted of Boolean arrays
     */
    @NonNull
    public static HashMap<String, Map<String, Boolean>> getDefaultGymDatabaseTurn() {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        final String[] keysMorning = ResourceUtils.getStringArrayFromID(R.array.morning_session_name);
        final String[] keysAfternoon = ResourceUtils.getStringArrayFromID(R.array.afternoon_session_name);
        final String[] keysEvening = ResourceUtils.getStringArrayFromID(R.array.evening_session_name);

        return new HashMap<String, Map<String, Boolean>>() {
            {
                put(keys[14], new HashMap<String, Boolean>() {
                    {
                        put(keysMorning[0], true);
                        put(keysMorning[1], true);
                        put(keysMorning[2], true);
                    }
                });
                put(keys[15], new HashMap<String, Boolean>() {
                    {
                        put(keysAfternoon[0], true);
                        put(keysAfternoon[1], true);
                        put(keysAfternoon[2], true);
                    }
                });
                put(keys[16], new HashMap<String, Boolean>() {
                    {
                        put(keysEvening[0], true);
                        put(keysEvening[1], true);
                        put(keysEvening[2], true);
                    }
                });
            }
        };
    }

    /**
     * Init and return a default map of boolean for set subscription gym's node
     *
     * @return Map of Boolean
     */
    @NonNull
    public static Map<String, Boolean> getDefaultGymSubscription() {
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

    @NonNull
    public List<String> getEmptyValues() {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.gym_field);
        final List<String> emptyKeyOfValues = new ArrayList<>();
        final String emptyValue = "null";

        if (this.getName().equals(emptyValue)) emptyKeyOfValues.add(keys[1]);
        if (this.getEmail().equals(emptyValue)) emptyKeyOfValues.add(keys[2]);
        if (this.getImage().equals(ResourceUtils.getURIForResource(R.drawable.default_user))) emptyKeyOfValues.add(keys[3]);
        if (this.getPhone().equals(emptyValue)) emptyKeyOfValues.add(keys[4]);
        if (this.getAddress().equals(emptyValue)) emptyKeyOfValues.add(keys[5]);
        if (this.getPosition().equals(new LatLng(0, 0))) emptyKeyOfValues.add(keys[6]);
        if (this.getSubscribers().isEmpty()) emptyKeyOfValues.add(keys[8]);
        if (this.isSubscriptionNewest.get()) emptyKeyOfValues.add(keys[7]);
        if (this.isTurnNewest.get()) emptyKeyOfValues.add(keys[9]);


        return emptyKeyOfValues;
    }
}
