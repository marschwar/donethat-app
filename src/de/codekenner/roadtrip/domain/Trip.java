package de.codekenner.roadtrip.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Trip extends SharedData {
    private String name;
    private String description;
    private Boolean isPublic = Boolean.FALSE;
    private long created = System.currentTimeMillis();
    private long changed = System.currentTimeMillis();
    private List<Note> notes;

    public String getName() {
        return name;
    }

    public Trip() {
        notes = new ArrayList<Note>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getChanged() {
        return changed;
    }

    public void setChanged(long changed) {
        this.changed = changed;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return name;
    }
}
