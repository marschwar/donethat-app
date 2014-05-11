package de.codekenner.roadtrip.sync;

import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.TripDataSource;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import static de.codekenner.roadtrip.sync.Config.URL_GET_TRIP;

/**
 * Created by markus on 31.05.13.
 */
public class SyncTripTask extends SyncTripsTask {

    private final Trip trip;

    public SyncTripTask(Trip trip) {
        this.trip = trip;
    }

    @Override
    protected List<Trip> findOutgoingTrips(TripDataSource dataSource, long latestChange) {
        return Collections.singletonList(trip);
    }

    @Override
    protected JSONObject getIncomingTrips() throws DataAccessException {
        return doGet(URL_GET_TRIP, trip.getUid());
    }

}
