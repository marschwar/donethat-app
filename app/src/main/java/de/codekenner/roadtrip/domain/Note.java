package de.codekenner.roadtrip.domain;

import java.util.Calendar;

public class Note extends SharedData {

    private long tripId;
    private Location location;
    private String name;
    private String text;
    private Calendar date;
    private long created;
    private long updated;
    private boolean withImage;
    private long changed;
    private long imageChanged;

    public Note() {
        location = Location.UNKNOWN;
        date = Calendar.getInstance();
        final long now = System.currentTimeMillis();
        created = now;
        updated = now;
    }

    public Note(long tripId) {
        this();
        this.tripId = tripId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public boolean isWithImage() {
        return withImage;
    }

    public void setWithImage(boolean withImage) {
        this.withImage = withImage;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public long getChanged() {
        return changed;
    }

    public void setChanged(long changed) {
        this.changed = changed;
    }

    public long getImageChanged() {
        return imageChanged;
    }

    public void setImageChanged(long imageChanged) {
        this.imageChanged = imageChanged;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
