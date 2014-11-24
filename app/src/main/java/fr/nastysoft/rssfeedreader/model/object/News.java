package fr.nastysoft.rssfeedreader.model.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Damien on 21/11/2014.
 */
public class News implements Parcelable {

    private String title;
    private String link;
    private String enclosure;
    private String description;
    private String pubDate;
    private String guid;
    private String storedDate;

    public News() {
    }

    public News(Parcel in) {
        readFromParcel(in);
    }

    public News(String title, String link, String enclosure, String description, String pubDate, String guid) {
        this.title = title;
        this.link = link;
        this.enclosure = enclosure;
        this.description = description;
        this.pubDate = pubDate;
        this.guid = guid;
        this.storedDate = setStoredDate(pubDate);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
        this.storedDate = setStoredDate(pubDate);
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getStoredDate() {
        return storedDate;
    }

    public String setStoredDate(String pubDate) {
        final String OLD_FORMAT = "EEE, dd MMM yyyy HH:mm:ss";
        final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss";

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, Locale.UK);
        Date d = null;
        try {
            d = sdf.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);

        return sdf.format(d);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(enclosure);
        dest.writeString(description);
        dest.writeString(pubDate);
        dest.writeString(guid);
        dest.writeString(storedDate);
    }

    private void readFromParcel(Parcel in) {
        this.title = in.readString();
        this.link = in.readString();
        this.enclosure = in.readString();
        this.description = in.readString();
        this.pubDate = in.readString();
        this.guid = in.readString();
        this.storedDate = in.readString();
    }

    // utilisé pour recréer l'objet
    public static final Parcelable.Creator<News> CREATOR = new Parcelable.Creator<News>() {
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        public News[] newArray(int size) {
            return new News[size];
        }
    };
}