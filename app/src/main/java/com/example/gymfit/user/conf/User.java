package com.example.gymfit.user.conf;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public class User extends GenericUser implements Serializable, Parcelable {
    private String fullname;
    private String name;
    private String surname;
    private Date dateOfBirthday;
    private LatLng location; // TODO: refact code removing this useless attribute
    private String address;
    private String gender;
    private String img;
    private String[] subscription; // 0 - GymID, 1 - subscriptionType
    private List<Map<String, Object>> turns; // Map at index 0 - turnDate (as Timestamp), Map at index 1 - turnVale (morningFirst, afternoonSecond...)

    public User (@NonNull final String name, @NonNull final String surname, @NonNull final String phone,
                 @NonNull final String email, @NonNull final String gender, @NonNull final String uid) {
        super(uid, email, phone);
        this.fullname = name + " " + surname;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
    }

    public User(@NonNull final String name, @NonNull final String surname, @NonNull final String gender, @NonNull final Date dateOfBirthday,
                @NonNull final LatLng location, @NonNull final String address, String phone) {
        this(name, surname, phone, "", gender, "");
        this.location = location;
        this.address = address;
        this.dateOfBirthday = dateOfBirthday;
    }

    public User(@NonNull final String name, @NonNull final String surname, @NonNull final String phone,
                @NonNull final String email, @NonNull final String gender, @NonNull final String uid, @NonNull final String img, @NonNull final String[] subscription,
                @NonNull final List<Map<String, Object>> turns) {
        this(name, surname, phone, email, gender, uid);
        this.img = img;
        this.subscription = subscription;
        this.turns = turns;
    }

    // Constructor with all new values
    public User(@NonNull final String uid, @NonNull final String name, @NonNull final String surname, @NonNull final String email,
                @NonNull final Date dateOfBirthday, @NonNull final String address, @NonNull final String gender, @NonNull final String img,
                @NonNull final String phone, @NonNull final String[] subscription, @NonNull final List<Map<String, Object>> turns) {
        this(name, surname, phone, email, gender, uid);
        this.dateOfBirthday = dateOfBirthday;
        this.address = address;
        this.img = img;
        this.subscription = subscription;
        this.turns = turns;
    }


    // Get methods

    protected User(@NonNull final Parcel in) {
        super("", "", "");
        setFullname(in.readString());
        setName(in.readString());
        setSurname(in.readString());
        setPhone(in.readString());
        location = in.readParcelable(LatLng.class.getClassLoader());
        setAddress(in.readString());
        setEmail(in.readString());
        setUid(in.readString());
        setImg(in.readString());
        in.readStringArray(this.subscription);
        setGender(in.readString());
    }

    @NonNull
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(getFullname());
        dest.writeString(getName());
        dest.writeString(getSurname());
        dest.writeString(getPhone());
        dest.writeParcelable(location, flags);
        dest.writeString(getAddress());
        dest.writeString(getEmail());
        dest.writeString(getUid());
        dest.writeString(getImg());
        dest.writeStringArray(getSubscription());
        dest.writeString(getGender());
    }

    @NonNull
    public String getFullname() {
        return fullname;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getSurname() {
        return surname;
    }

    @NonNull
    public String getGender() {
        return gender;
    }

    public Date getDateOfBirthday() {
        return dateOfBirthday;
    }

    @NonNull
    public String getImg(){ return img;}

    @NonNull
    public LatLng getLocation(){return location;}

    @NonNull
    public String getAddress(){return address;}

    @NonNull
    public String[] getSubscription() {
        return subscription;
    }

    @NonNull
    public List<Map<String, Object>> getTurns() {
        return turns;
    }

    // Set methods

    public void setFullname(@NonNull final String fullname) {
        this.fullname = fullname;
    }

    public void setName(@NonNull final String name) {
        this.name = name;
    }

    public void setSurname(@NonNull final String surname) {
        this.surname = surname;
    }

    public void setGender(@NonNull final String gender) {
        this.gender = gender;
    }

    public void setAddress(@NonNull final String address) {
        this.address = address;
    }

    public void setLocation(@NonNull final LatLng location) {
        this.location = location;
    }

    public void setDateOfBirthday(@NonNull final Date dateOfBirthday) {
        this.dateOfBirthday = dateOfBirthday;
    }

    public void setImg(@NonNull final String img) {
        this.img = img;
    }

    public void setSubscription(@NonNull final String[] subscription) {
        this.subscription = subscription;
    }

    public void setTurns(@NonNull final List<Map<String, Object>> turns) {
        this.turns = turns;
    }

    public void setTurn(@NonNull final Map<String, Object> turn) {
        this.turns.add(turn);
    }

    public void removeTurn(@NonNull final Map<String, Object> turn) {
        this.turns.remove(turn);
    }

    // Other methods

    @NonNull
    public List<String> getEmptyValues() {
        final String[] keys = ResourceUtils.getStringArrayFromID(R.array.user_field);
        final List<String> emptyKeyOfValues = new ArrayList<>();
        final String emptyValue = "null";

        if (this.getName().equals(emptyValue)) emptyKeyOfValues.add(keys[1]);
        if (this.getSurname().equals(emptyValue)) emptyKeyOfValues.add(keys[2]);
        if (this.getDateOfBirthday().equals(new GregorianCalendar(1900, Calendar.JANUARY, 1).getTime())) emptyKeyOfValues.add(keys[4]);
        if (this.getEmail().equals(emptyValue)) emptyKeyOfValues.add(keys[5]);
        if (this.getGender().equals(emptyValue)) emptyKeyOfValues.add(keys[6]);
        if (this.getPhone().equals(emptyValue)) emptyKeyOfValues.add(keys[7]);
        if (this.getImg().equals(ResourceUtils.getURIForResource(R.drawable.default_user))) emptyKeyOfValues.add(keys[8]);
        if (this.getAddress().equals(emptyValue)) emptyKeyOfValues.add(keys[9]);
        if (this.getSubscription()[0].equals(emptyValue)) emptyKeyOfValues.add(keys[10]);
        if (this.getTurns().isEmpty()) emptyKeyOfValues.add(keys[11]);

        return emptyKeyOfValues;
    }

}

