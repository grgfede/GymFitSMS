package com.example.gymfit.gym.conf;

import androidx.annotation.NonNull;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.database.core.utilities.Tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Gym extends GenericUser implements Serializable {
    private String address;
    private String name;
    private String image;
    private final Double[] positionArray;
    private final List<String> subscribers;
    private final Map<String, Boolean> subscription;
    private final Map<String, Boolean[]> turns;

    public Gym(String uid, String email, String phone, String name, String address, List<String> subscribers, LatLng position, String image) {
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

    public Map<String, Boolean[]> getTurns() {
        return turns;
    }

    public Map<String, Boolean> getSubscription() {
        return subscription;
    }

    public List<String> getSubscribers() {
        return subscribers;
    }

    public String getImage() {
        return image;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getPosition() {
        return new LatLng(this.positionArray[0], this.positionArray[1]);
    }

    public String getName() {
        return name;
    }

    // Set methods

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.positionArray[0] = position.latitude;
        this.positionArray[1] = position.longitude;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSubscription(Map<String, Boolean> subscription) {
        this.subscription.clear();
        this.subscription.putAll(subscription);
    }

    public void setTurn(String key, int position, boolean value) {
        Objects.requireNonNull(this.turns.get(key))[position] = value;
    }

    public void setTurns(Map<String, Boolean[]> turns) {
        this.turns.clear();
        this.turns.putAll(turns);
    }

    public void setSubscribers(List<String> subscribers) {
        this.subscribers.clear();
        this.subscribers.addAll(subscribers);
    }

    // Remove methods

    public void removeSubscriber(String subscriber) {
        this.subscribers.remove(subscriber);
    }

    // Update methods

    public void updateSubscription(String key, Boolean value) {
        this.subscription.replace(key, value);
    }

    // Sort methods

    public static Map<String, Boolean> sortValueTurn(@NonNull final String keyTurn, @NonNull HashMap<String, Boolean> turn) {
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
    public static String getSubscriptionFromTranslated(@NotNull String subscriptionTranslated) {
        final String[] subscriptionTranslatedKeys = new String[] {
                ResourceUtils.getStringFromID(R.string.monthly_subscription),
                ResourceUtils.getStringFromID(R.string.quarterly_subscription),
                ResourceUtils.getStringFromID(R.string.six_month_subscription),
                ResourceUtils.getStringFromID(R.string.annual_subscription)
        };
        final String[] subscriptionKeys = new String[] {
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[10],
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[11],
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[12],
                ResourceUtils.getStringArrayFromID(R.array.gym_field)[13]
        };

        if (subscriptionTranslated.equals(subscriptionTranslatedKeys[0])) {
            return subscriptionKeys[0];
        } else if (subscriptionTranslated.equals(subscriptionTranslatedKeys[1])) {
            return subscriptionKeys[1];
        } else if (subscriptionTranslated.equals(subscriptionTranslatedKeys[2])) {
            return subscriptionKeys[2];
        } else {
            return subscriptionKeys[3];
        }
    }

}
