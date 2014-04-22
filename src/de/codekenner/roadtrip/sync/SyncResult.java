package de.codekenner.roadtrip.sync;

/**
 * Created by markus on 03.06.13.
 */
public class SyncResult {
    private int tripsSent = 0;
    private int tripsReceived = 0;

    public int getTripsSent() {
        return tripsSent;
    }

    public void setTripsSent(int tripsSent) {
        this.tripsSent = tripsSent;
    }

    public int getTripsReceived() {
        return tripsReceived;
    }

    public void setTripsReceived(int tripsReceived) {
        this.tripsReceived = tripsReceived;
    }
}
