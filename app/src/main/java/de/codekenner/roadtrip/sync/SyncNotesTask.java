package de.codekenner.roadtrip.sync;

import de.codekenner.roadtrip.domain.GPSLocation;
import de.codekenner.roadtrip.domain.Location;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.ProgressCallback;
import de.codekenner.roadtrip.storage.TripDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static de.codekenner.roadtrip.sync.Config.URL_GET_NOTES;
import static de.codekenner.roadtrip.sync.Config.URL_POST_NOTES;
import static de.codekenner.roadtrip.sync.Config.URL_POST_TRIPS;

/**
 * Created by markus on 31.05.13.
 */
public class SyncNotesTask extends AbstractSyncTask<SyncResult> {

    public static final String CONTENT = "content";
    public static final String TITLE = "title";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String WITH_IMAGE = "with_image";
    public static final String IMAGE_CHANGED = "image_changed";
    public static final String NOTE_DATE = "note_date";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String UID = "uid";
    public static final String NULL = "null";
    public static final String LAST_CHANGE = "last_change";

    private final Trip trip;

    public SyncNotesTask(Trip trip, long lastSync, String userToken, String baseUrl) {
        this.trip = trip;
        setLastSync(lastSync);
        setUserToken(userToken);
        setBaseUrl(baseUrl);
    }

    @Override
    public SyncResult doWithDatasource(TripDataSource dataSource, ProgressCallback callback) throws DataAccessException {
        final JSONObject incoming = doGet(URL_GET_NOTES, trip.getUid(), getLastSync());
        try {
            final JSONArray incomingNotes = incoming.getJSONArray("notes");
            final long latestChange = incoming.getLong(LAST_CHANGE);

            // Bevor wir schreiben, lesen wir erst einmal unsere eigenen Änderungen
            final List<Note> outgoingNotes = dataSource.getNotes(trip, latestChange);

            if (incomingNotes != null && incomingNotes.length() > 0) {
                for (int i = 0; i < incomingNotes.length(); i++) {
                    syncNote(dataSource, incomingNotes.getJSONObject(i));
                }
            }
            sendNotes(dataSource, outgoingNotes);

            return null;

        } catch (JSONException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void sendNotes(TripDataSource dataSource, Collection<Note> notes) throws JSONException, DataAccessException {
        if (notes != null && !notes.isEmpty()) {
            final List<JSONObject> notesAsJson = new LinkedList<JSONObject>();
            for (Note each : notes) {
                notesAsJson.add(toJson(each));
            }
            final JSONArray json = new JSONArray(notesAsJson);
            doPost(URL_POST_NOTES, json, trip.getUid());

            for (Note each : notes) {
                if (each.isWithImage() && getLastSync() < each.getImageChanged()) {
                    new SendNoteImageTask(each, getLastSync(), getUserToken(), getBaseUrl()).doWithDatasource(dataSource, null);
                }
            }
        }
    }

    private JSONObject toJson(Note note) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(UID, note.getUid());
        json.put(TITLE, note.getName());
        json.put(CONTENT, note.getText());
        json.putOpt(LONGITUDE, note.getLocation().getLongitude());
        json.putOpt(LATITUDE, note.getLocation().getLatitude());
        json.put(WITH_IMAGE, note.isWithImage() ? 1 : 0);
        json.put(IMAGE_CHANGED, note.getImageChanged());
        json.put(NOTE_DATE, note.getDate().getTimeInMillis());
        json.put(CREATED, note.getCreated());
        json.put(UPDATED, note.getChanged());

        return json;
    }

    /**
     * @param dataSource
     * @param incomingNote
     * @return true, wenn etwas geändert wurde, sonst false
     * @throws org.json.JSONException
     */
    private boolean syncNote(TripDataSource dataSource, JSONObject incomingNote) throws JSONException {
        final String uid = incomingNote.getString(UID);
        Note note = dataSource.findNoteByUid(uid);
        final long changed = incomingNote.getLong(UPDATED);
        if (note == null) {
            // Neu
            note = new Note();
            note.setUid(uid);
            note.setCreated(incomingNote.getLong(CREATED));
        } else {
            if (changed < note.getChanged()) {
                // Änderungen auf beiden Seiten und ich bin aktueller
                return false;
            }
        }
        note.setTripId(trip.getId());
        note.setName(incomingNote.getString(TITLE));
        note.setText(incomingNote.getString(CONTENT));
        note.setWithImage(!NULL.equals(incomingNote.getString(WITH_IMAGE)) && incomingNote.getInt(WITH_IMAGE) == 1);
        if (NULL.equals(incomingNote.getString(LONGITUDE)) || "null".equals(incomingNote.getString(LATITUDE))) {
            note.setLocation(GPSLocation.UNKNOWN);
        } else {
            note.setLocation(new GPSLocation(incomingNote.getDouble(LONGITUDE), incomingNote.getDouble(LATITUDE)));
        }
        if (incomingNote.getLong(NOTE_DATE) > 0) {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(incomingNote.getLong(NOTE_DATE));
            note.setDate(cal);
        }
        note.setImageChanged(NULL.equals(incomingNote.getString(IMAGE_CHANGED)) ? 0 : incomingNote.getLong(IMAGE_CHANGED));
        note.setChanged(changed);
        note = dataSource.save(note);

        return true;
    }

}
