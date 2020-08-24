package com.mafdy.onthemove.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by SBP on 7/6/2018.
 */

@Entity
public class Status implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double latitude;
    private double longitude;
    private double locationaccuracy;
    private String locationaddress;
    private String activity;
    private String transition;
    private Calendar datetime;
    private String destinationname;
    private String destinationid;
    private String destinationaddress;


    public Status()
    {
        super();

    }

    @Ignore
    protected Status(Parcel in) {
        id = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        locationaccuracy = in.readDouble();
        activity = in.readString();
        transition = in.readString();
        destinationname = in.readString();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(in.readLong());
        datetime = c;
        destinationid = in.readString();
        locationaddress = in.readString();
        destinationaddress = in.readString();
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(locationaccuracy);
        dest.writeString(activity);
        dest.writeString(transition);
        dest.writeString(destinationname);
        dest.writeLong(datetime.getTimeInMillis());
        dest.writeString(destinationid);
        dest.writeString(locationaddress);
        dest.writeString(destinationaddress);
    }

    @Ignore
    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLocationaccuracy() {
        return locationaccuracy;
    }

    public void setLocationaccuracy(double locationaccuracy) {
        this.locationaccuracy = locationaccuracy;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }


    public String getDestinationname() {
        return destinationname;
    }

    public void setDestinationname(String destinationname) {
        this.destinationname = destinationname;
    }

    public Calendar getDatetime() {
        return datetime;
    }

    public void setDatetime(Calendar datetime) {
        this.datetime = datetime;
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }



    public String getDestinationid() {
        return destinationid;
    }

    public void setDestinationid(String destinationid) {
        this.destinationid = destinationid;
    }

    public String getLocationaddress() {
        return locationaddress;
    }

    public void setLocationaddress(String locationaddress) {
        this.locationaddress = locationaddress;
    }

    public String getDestinationaddress() {
        return destinationaddress;
    }

    public void setDestinationaddress(String destinationaddress) {
        this.destinationaddress = destinationaddress;
    }
}
