/**
 *
 */
package de.codekenner.roadtrip.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import de.codekenner.roadtrip.domain.GPSLocation;
import de.codekenner.roadtrip.domain.Location;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;

import java.util.*;

import static de.codekenner.roadtrip.storage.RoadTripDatabaseOpenHelper.*;

/**
 * Data access object to access trip information
 *
 * @author markus
 */
public class TripDataSource {
    // Database fields
    private SQLiteDatabase database;
    private RoadTripDatabaseOpenHelper dbHelper;

    public TripDataSource(Context context) {
        dbHelper = new RoadTripDatabaseOpenHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        database = null;
    }

    /**
     * Create or update a trip
     *
     * @param trip
     * @return the saved trip guaranteed to have an id
     */
    public Trip save(Trip trip) {
        ContentValues values = new ContentValues();
        values.put(TRIP_NAME, trip.getName());
        values.put(TRIP_DESCRIPTION, trip.getDescription());
        values.put(CREATED, trip.getCreated());
        values.put(CHANGED, trip.getChanged());
        values.put(TRIP_PUBLIC, trip.getPublic() ? 1 : 0);

        if (trip.getId() == null) {
            if (trip.getUid() == null) {
                final String uid = UUID.randomUUID().toString();
                values.put(UID, uid);
                trip.setUid(uid);
            } else {
                values.put(UID, trip.getUid());
            }
            final long insertId = database
                    .insertOrThrow(TRIP_TABLE_NAME, null, values);
            trip.setId(insertId);
        } else {
            String[] args = {trip.getId().toString()};
            database.update(TRIP_TABLE_NAME, values, ID + " = ?", args);
        }

        return trip;
    }

    /**
     * Create or update a note
     *
     * @param note
     * @return the saved note guaranteed to have an id
     */
    public Note save(Note note) {

        // load the trip first to possibly update the start and end date
        final Trip trip = getTrip(note.getTripId());

        ContentValues values = new ContentValues();
        values.put(NOTE_NAME, note.getName());
        values.put(FK_TRIP_ID, note.getTripId());
        values.put(NOTE_DESCRIPTION, note.getText());
        values.put(NOTE_DATE, asDBValue(note.getDate()));
        values.put(NOTE_IMAGE, asDBValue(note.isWithImage()));
        values.put(NOTE_LOCATION_LONG, note.getLocation().getLongitude());
        values.put(NOTE_LOCATION_LAT, note.getLocation().getLatitude());
        values.put(CREATED, note.getCreated());
        values.put(CHANGED, note.getChanged());
        values.put(NOTE_IMAGE_CHANGED, note.getImageChanged());

        if (note.getId() == null) {
            // new here
            values.put(UID, note.getUid() == null ? UUID.randomUUID().toString() : note.getUid());
            final long insertId = database
                    .insertOrThrow(NOTE_TABLE_NAME, null, values);
            note.setId(insertId);
        } else {
            database.update(NOTE_TABLE_NAME, values, ID + " = " + note.getId(),
                    null);
        }

        if (trip == null) {
            throw new IllegalArgumentException("unable to find trip");
        }
        trip.setChanged(System.currentTimeMillis());
        save(trip);

        return note;
    }

    private static long asDBValue(Calendar cal) {
        return cal == null ? -1 : cal.getTimeInMillis();
    }

    private static int asDBValue(boolean b) {
        return b ? 1 : 0;
    }

    private static Calendar calendarFromDBValue(Long l) {
        if (l != null && l > 0) {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(l);
            return cal;
        }
        return null;
    }

    private static boolean booleanFromDBValue(Integer i) {
        return i != null && i == 1;
    }

    public List<Trip> getTrips() {
        List<Trip> trips = new ArrayList<Trip>();

        Cursor cursor = null;
        try {
            cursor = database.query(TRIP_TABLE_NAME, TRIP_COLUMNS, null, null,
                    null, null, CHANGED + " desc");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Trip trip = cursorToTrip(cursor);
                trips.add(trip);
                cursor.moveToNext();
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return trips;
    }

    public List<Trip> getTrips(long changedSince) {
        List<Trip> trips = new ArrayList<Trip>();

        Cursor cursor = null;
        try {
            final String[] args = new String[]{String.valueOf(changedSince)};
            cursor = database.query(TRIP_TABLE_NAME, TRIP_COLUMNS, CHANGED + " > ?", args,
                    null, null, CHANGED + " desc");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Trip trip = cursorToTrip(cursor);
                trips.add(trip);
                cursor.moveToNext();
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return trips;
    }

    public List<Note> getNotes(Trip trip) {
        List<Note> notes = new ArrayList<Note>();

        Cursor cursor = null;
        try {
            String[] args = {trip.getId().toString()};
            cursor = database.query(NOTE_TABLE_NAME, NOTE_COLUMNS, FK_TRIP_ID
                    + " = ?", args, null, null, NOTE_DATE + " desc");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Note note = cursorToNote(cursor);
                notes.add(note);
                cursor.moveToNext();
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return notes;
    }

    public List<Note> getNotes(Trip trip, long changedSince) {
        List<Note> notes = new ArrayList<Note>();

        Cursor cursor = null;
        try {
            final String where = FK_TRIP_ID + " = ? and " + CHANGED + " > ?";
            final String[] args = {trip.getId().toString(), String.valueOf(changedSince)};
            cursor = database.query(NOTE_TABLE_NAME, NOTE_COLUMNS, where, args, null, null, CHANGED + " desc");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Note note = cursorToNote(cursor);
                notes.add(note);
                cursor.moveToNext();
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return notes;
    }


    public Trip getTrip(long id) {

        Cursor cursor = null;
        try {
            String[] args = {String.valueOf(id)};
            cursor = database.query(TRIP_TABLE_NAME, TRIP_COLUMNS, ID + " = ?",
                    args, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToTrip(cursor);
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return null;
    }

    public Trip findTripByUid(String uid) {

        Cursor cursor = null;
        try {
            String[] args = {uid};
            cursor = database.query(TRIP_TABLE_NAME, TRIP_COLUMNS, UID + " = ?",
                    args, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToTrip(cursor);
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return null;
    }

    private Trip cursorToTrip(Cursor cursor) {
        final Trip trip = new Trip();

        int i = 0;
        trip.setId(cursor.getLong(i++));
        trip.setUid(cursor.getString(i++));
        trip.setName(cursor.getString(i++));
        trip.setDescription(cursor.getString(i++));
        trip.setCreated(cursor.getLong(i++));
        trip.setChanged(cursor.getLong(i++));
        trip.setPublic(cursor.getInt(i++) > 0);

        return trip;
    }

    private Note cursorToNote(Cursor cursor) {
        final Note note = new Note();

        int i = 0;
        note.setId(cursor.getLong(i++));
        note.setTripId(cursor.getLong(i++));
        note.setUid(cursor.getString(i++));
        note.setName(cursor.getString(i++));
        note.setText(cursor.getString(i++));
        note.setDate(calendarFromDBValue(cursor.getLong(i++)));
        note.setWithImage(booleanFromDBValue(cursor.getInt(i++)));
        final Double longitude = cursor.getDouble(i++);
        final Double latitude = cursor.getDouble(i++);

        note.setLocation(longitude == 0d && latitude == 0D ? Location.UNKNOWN
                : new GPSLocation(longitude, latitude));
        note.setChanged(cursor.getLong(i++));
        note.setImageChanged(cursor.getLong(i++));
        return note;
    }

    public Note getNote(Long id) {

        Cursor cursor = null;
        try {
            String[] args = {String.valueOf(id)};
            cursor = database.query(NOTE_TABLE_NAME, NOTE_COLUMNS, ID + " = ?",
                    args, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToNote(cursor);
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return null;
    }

    public Note findNoteByUid(String uid) {

        Cursor cursor = null;
        try {
            String[] args = {String.valueOf(uid)};
            cursor = database.query(NOTE_TABLE_NAME, NOTE_COLUMNS, UID + " = ?",
                    args, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToNote(cursor);
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return null;
    }

    public Note findNoteWithImage(Trip trip) {
        Cursor cursor = null;
        try {
            final String where = String.format(Locale.US,
                    "%s = %d and %s = %d", FK_TRIP_ID, trip.getId(),
                    NOTE_IMAGE, asDBValue(true));
            cursor = database.query(NOTE_TABLE_NAME, NOTE_COLUMNS, where, null,
                    null, null, NOTE_IMAGE_CHANGED + " DESC LIMIT 1");

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToNote(cursor);
            }
        } finally {
            // Make sure to close the cursor
            if (cursor != null) {
                cursor.close();
            }

        }
        return null;
    }

    public void deleteNote(Note note) {
        String[] args = {note.getId().toString()};
        database.delete(NOTE_TABLE_NAME, ID + " = ?", args);
    }

    public void deleteAll() {
        dbHelper.onUpgrade(database, 0, 0);
    }
}
