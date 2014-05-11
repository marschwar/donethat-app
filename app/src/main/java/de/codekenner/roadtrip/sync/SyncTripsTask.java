package de.codekenner.roadtrip.sync;

import android.util.Log;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.ProgressCallback;
import de.codekenner.roadtrip.storage.TripDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static de.codekenner.roadtrip.sync.Config.URL_GET_TRIPS;
import static de.codekenner.roadtrip.sync.Config.URL_POST_TRIPS;

/**
 * Created by markus on 31.05.13.
 */
public class SyncTripsTask extends AbstractSyncTask<SyncResult> {

    public static final String IS_PUBLIC = "is_public";
    public static final String CONTENT = "content";
    public static final String TITLE = "title";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String LAST_CHANGE = "last_change";

    @Override
    public SyncResult doWithDatasource(TripDataSource dataSource, ProgressCallback callback) throws DataAccessException {
        final SyncResult result = new SyncResult();
        final JSONObject incoming = getIncomingTrips();
        callback.publishProgress(30);

        try {
            final JSONArray incomingTrips = incoming.getJSONArray("trips");
            final long latestChange = incoming.getLong(LAST_CHANGE);
            // Bevor wir schreiben, lesen wir erst einmal unsere eigenen Änderungen
            final List<Trip> outgoingTrips = findOutgoingTrips(dataSource, latestChange);

            final Set<Trip> forNoteSync = new HashSet<Trip>();
            if (incomingTrips != null && incomingTrips.length() > 0) {
                int count = 0;
                for (int i = 0; i < incomingTrips.length(); i++) {
                    final Trip tripSynchronized = syncTrip(dataSource, incomingTrips.getJSONObject(i));
                    if (tripSynchronized != null) {
                        forNoteSync.add(tripSynchronized);
                        count++;
                        callback.publishProgress((int) ((float) 30) / incomingTrips.length() * (i + 1));
                    }
                }
                result.setTripsReceived(count);
                callback.publishProgress(60);
            }
            sendTrips(dataSource, outgoingTrips);
            forNoteSync.addAll(outgoingTrips);
            syncNotesFor(dataSource, forNoteSync);

            result.setTripsSent(outgoingTrips.size());
            callback.publishProgress(100);

            return result;

        } catch (JSONException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    protected JSONObject getIncomingTrips() throws DataAccessException {
        return doGet(URL_GET_TRIPS, getLastSync());
    }

    private void sendTrips(TripDataSource dataSource, Collection<Trip> trips) throws JSONException, DataAccessException {
        if (trips == null || trips.isEmpty()) {
            return;
        }

        final List<JSONObject> tripsAsJson = new LinkedList<JSONObject>();
        for (Trip each : trips) {
            tripsAsJson.add(toJson(each));
        }
        final JSONArray json = new JSONArray(tripsAsJson);
        final JSONArray status = doPost(URL_POST_TRIPS, json);
        Log.d(getClass().getName(), "post returned " + (status == null ? null : status.toString(1)));

    }

    private void syncNotesFor(TripDataSource dataSource, Collection<Trip> trips) throws DataAccessException {
        for (Trip each : trips) {
            new SyncNotesTask(each, getLastSync(), getUserToken(), getBaseUrl()).doWithDatasource(dataSource, null);
        }

    }

    private JSONObject toJson(Trip trip) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put("uid", trip.getUid());
        json.put(TITLE, trip.getName());
        json.put(CONTENT, trip.getDescription());
        if (trip.getPublic() != null) {
            json.put(IS_PUBLIC, trip.getPublic() ? 1 : 0);
        }
        json.put(CREATED, trip.getCreated());
        json.put(UPDATED, trip.getChanged());

        return json;
    }

    protected List<Trip> findOutgoingTrips(TripDataSource dataSource, long latestChange) {
        return dataSource.getTrips(latestChange);
    }

    /**
     * @param dataSource
     * @param incomingTrip
     * @return true, wenn etwas geändert wurde, sonst false
     * @throws org.json.JSONException
     */
    private Trip syncTrip(TripDataSource dataSource, JSONObject incomingTrip) throws JSONException {
        final String uid = incomingTrip.getString("uid");
        Trip trip = dataSource.findTripByUid(uid);
        final long changed = incomingTrip.getLong(UPDATED);
        if (trip == null) {
            // Neu
            trip = new Trip();
            trip.setUid(uid);
            trip.setCreated(incomingTrip.getLong(CREATED));
        } else {
            if (changed <= trip.getChanged()) {
                // Änderungen auf beiden Seiten und ich bin aktueller
                return trip;
            }
        }
        trip.setName(incomingTrip.getString(TITLE));
        trip.setDescription(incomingTrip.getString(CONTENT));
        if (!"null".equals(incomingTrip.getString(IS_PUBLIC))) {
            trip.setPublic(incomingTrip.getInt(IS_PUBLIC) != 0);
        }
        trip.setChanged(changed);
        trip = dataSource.save(trip);

        return trip;
    }

}
