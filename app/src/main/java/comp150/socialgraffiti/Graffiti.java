package comp150.socialgraffiti;

import android.location.Location;

import android.os.Parcel;
import android.os.Parcelable;

public class Graffiti implements Parcelable {

    protected String userID;
    protected String content;
    protected boolean hasPhoto;
    protected String photoURL;
    protected int duration;
    protected double lat;
    protected double lon;
    protected long time;

    public String getUser() {
        return userID;
    }
    public void setUser(String userID) {
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasPhoto() {
        return hasPhoto;
    }
    public void setHasPhoto (boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public String getPhotoURL () { return photoURL; }
    public void setPhotoURL (String photoURL) { this.photoURL = photoURL; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setLocation (Location location) {
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();
        this.time = location.getTime();
    }
    public double getLat () { return this.lat; }
    public double getLon () { return this.lon; }
    public long   getTime () { return this.time; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(content);
        dest.writeByte((byte) (hasPhoto ? 1 : 0));
        dest.writeString(photoURL);
        dest.writeInt(duration);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeLong(time);
    }

    public static final Parcelable.Creator<Graffiti> CREATOR
            = new Parcelable.Creator<Graffiti>() {
        public Graffiti createFromParcel(Parcel in) {
            return new Graffiti(in);
        }

        public Graffiti[] newArray(int size) {
            return new Graffiti[size];
        }
    };

    public Graffiti() {
    }

    private Graffiti(Parcel in) {
        userID = in.readString();
        content = in.readString();
        hasPhoto = in.readByte() != 0;
        photoURL = in.readString();
        duration = in.readInt();
        lat = in.readDouble();
        lon = in.readDouble();
        time = in.readLong();
    }
}