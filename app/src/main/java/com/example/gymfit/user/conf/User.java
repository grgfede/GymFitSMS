package com.example.gymfit.user.conf;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.gymfit.system.conf.GenericUser;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class User extends GenericUser implements Serializable, Parcelable {

    private LatLng location; // TODO: safe delete

    // New value
    private String fullname;
    private String name;
    private String surname;
    private Date dateOfBirthday;
    private String address;
    private String gender;
    private String img;
    private String subscription;
    private List<Map<String, Object>> turns;

    public User (String name, String surname, String phone, String email, String gender, String uid) {
        super(uid, email, phone);
        this.fullname = name + " " + surname;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
    }

    public User(String name, String surname, String gender, Date dateOfBirthday, LatLng location, String address, String phone) {
        this(name, surname, phone, "", gender, "");
        this.location = location;
        this.address = address;
        this.dateOfBirthday = dateOfBirthday;
    }

    public User(String name, String surname, String phone, String email, String gender, String uid, String img, String subscription, List<Map<String, Object>> turns) {
        this(name, surname, phone, email, gender, uid);
        this.img = img;
        this.subscription = subscription;
        this.turns = turns;
    }

    // Constructor with all new values
    public User(String uid, String name, String surname, String email,
                Date dateOfBirthday, String address, String gender, String img, String phone,
                String subscription, List<Map<String, Object>> turns) {
        this(name, surname, phone, email, gender, uid);
        this.dateOfBirthday = dateOfBirthday;
        this.address = address;
        this.img = img;
        this.subscription = subscription;
        this.turns = turns;

    }


    // Get methods

    protected User(Parcel in) {
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
        setSubscription(in.readString());
        setGender(in.readString());
    }

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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getFullname());
        dest.writeString(getName());
        dest.writeString(getSurname());
        dest.writeString(getPhone());
        dest.writeParcelable(location, flags);
        dest.writeString(getAddress());
        dest.writeString(getEmail());
        dest.writeString(getUid());
        dest.writeString(getImg());
        dest.writeString(getSubscription());
        dest.writeString(getGender());
    }

    public String getFullname() {
        return fullname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getGender() {
        return gender;
    }

    public Date getDateOfBirthday() {
        return dateOfBirthday;
    }

    public String getImg(){ return img;}

    public LatLng getLocation(){return location;}

    public String getAddress(){return address;}

    public String getSubscription() {
        return subscription;
    }

    public List<Map<String, Object>> getTurns() {
        return turns;
    }

    // Set methods

    private void setFullname(String fullname) {
        this.fullname = fullname;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setSurname(String surname) {
        this.surname = surname;
    }

    private void setGender(String gender) {
        this.gender = gender;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    private void setImg(String img) {
        this.img = img;
    }

    private void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    private void setTurns(List<Map<String, Object>> turns) {
        this.turns = turns;
    }

}

