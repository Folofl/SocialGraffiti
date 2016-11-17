package comp150.socialgraffiti;

import android.location.Location;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Graffiti implements Parcelable {
    //private int mData;

    protected int id;
    protected String username;
    protected String content;
    protected boolean hasPhoto;
    protected String photoString;
    protected int duration;
    protected Location location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhotoString () { return photoString; }

    public void setPhotoString (String photoString) { this.photoString = photoString; }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Location getLocation () { return location; }

    public void setLocation (Location location) {this.location = location;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(username);
        dest.writeString(content);
        dest.writeByte((byte) (hasPhoto ? 1 : 0));
        dest.writeString(photoString);
        dest.writeInt(duration);
        location.writeToParcel(dest, flags);
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
        //mData = in.readInt();
        id = in.readInt();
        username = in.readString();
        content = in.readString();
        hasPhoto = in.readByte() != 0;
        photoString = in.readString();
        duration = in.readInt();
        location = Location.CREATOR.createFromParcel(in);
    }
}