package de.codekenner.roadtrip.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class RoadTripDatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "roadtrip.db";
    private static final int DATABASE_VERSION = 9;

    static final String ID = "_id";
    /**
     * an automatically created unique identifier across devices
     */
    static final String UID = "uid";
    static final String FK_TRIP_ID = "trip_id";
    /**
     * Der Timestamp der letzten Änderung
     */
    static final String CREATED = "created";
    static final String CHANGED = "changed";

    static final String TRIP_TABLE_NAME = "trip";
    static final String TRIP_NAME = "name";
    static final String TRIP_DESCRIPTION = "description";
    static final String TRIP_PUBLIC = "is_public";
    static final String[] TRIP_COLUMNS = {ID, UID, TRIP_NAME,
            TRIP_DESCRIPTION, CREATED, CHANGED, TRIP_PUBLIC};

    static final String NOTE_TABLE_NAME = "note";
    static final String NOTE_NAME = "name";
    static final String NOTE_DESCRIPTION = "description";
    static final String NOTE_DATE = "note_date";
    static final String NOTE_IMAGE = "image";
    static final String NOTE_IMAGE_CHANGED = "image_changed";
    static final String NOTE_LOCATION_LONG = "longitude";
    static final String NOTE_LOCATION_LAT = "latitude";
    static final String[] NOTE_COLUMNS = {ID, FK_TRIP_ID, UID, NOTE_NAME,
            NOTE_DESCRIPTION, NOTE_DATE, NOTE_IMAGE, NOTE_LOCATION_LONG,
            NOTE_LOCATION_LAT, CREATED, CHANGED, NOTE_IMAGE_CHANGED};

    private static final String TABLE_CREATE_TRIP = String.format(
            "CREATE TABLE %s (" + "%s integer primary key autoincrement, "
                    + "%s text not null, %s text not null, %s text, "
                    + "%s integer, %s integer, %s integer);", TRIP_TABLE_NAME, ID, UID,
            TRIP_NAME, TRIP_DESCRIPTION, CREATED, CHANGED, TRIP_PUBLIC);

    private static final String TABLE_CREATE_NOTE = String
            .format("CREATE TABLE %s ("
                    + "%s integer primary key autoincrement, "
                    + "%s integer not null, %s text not null, "
                    + "%s text not null, %s text, "
                    + "%s integer not null, %s integer not null, %s real, %s real" +
                    ", %s integer, %s integer, %s integer);",
                    NOTE_TABLE_NAME, ID, FK_TRIP_ID, UID, NOTE_NAME,
                    NOTE_DESCRIPTION, NOTE_DATE, NOTE_IMAGE,
                    NOTE_LOCATION_LONG, NOTE_LOCATION_LAT, CREATED, CHANGED, NOTE_IMAGE_CHANGED);

    private static final String CREATE_INDEX_TRIP_UID = String.format("CREATE UNIQUE INDEX IF NOT EXISTS uidx_trip_uid on %s(%s)", TRIP_TABLE_NAME, UID);
    private static final String CREATE_INDEX_NOTE_UID = String.format("CREATE UNIQUE INDEX IF NOT EXISTS uidx_note_uid on %s(%s)", NOTE_TABLE_NAME, UID);
    private static final String CREATE_INDEX_NOTE_IMAGE = String.format("CREATE INDEX IF NOT EXISTS idx_note_img on %s(%s DESC)", NOTE_TABLE_NAME, NOTE_IMAGE);

    public RoadTripDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_TRIP);
        db.execSQL(TABLE_CREATE_NOTE);
        db.execSQL(CREATE_INDEX_TRIP_UID);
        db.execSQL(CREATE_INDEX_NOTE_UID);
        db.execSQL(CREATE_INDEX_NOTE_IMAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Remove when relatively stable
        Log.w(getClass().getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + NOTE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TRIP_TABLE_NAME);
        onCreate(db);
    }

}
