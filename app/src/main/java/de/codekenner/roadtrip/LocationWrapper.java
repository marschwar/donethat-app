package de.codekenner.roadtrip;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import de.codekenner.roadtrip.domain.GPSLocation;

/**
 * Created by Marvin on 11.05.2014.
 */
public class LocationWrapper {

    private LocationManager locationManager;
    private de.codekenner.roadtrip.domain.Location currentLocation = de.codekenner.roadtrip.domain.Location.UNKNOWN;
    private LocationListener locationListener;

    public LocationWrapper(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        initializeLocation();
        //checkForLocationUpdates();
    }



    public void startSearchingForLocation() {
        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, locationListener
        );
    }

    private void initializeLocation() {
        Location lastKnown = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnown == null) {
            lastKnown = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (lastKnown != null) {
            setCurrentLocation(new GPSLocation(lastKnown.getLongitude(),
                    lastKnown.getLatitude()));
        }
    }

    public de.codekenner.roadtrip.domain.Location getCurrentLocation() {
        return currentLocation;
    }

    public de.codekenner.roadtrip.domain.Location getLocationAndStopListening() {
        // Stop the LocationManager from polling the GPS
        locationManager.removeUpdates(locationListener);

        return getCurrentLocation();
    }

    public void setCurrentLocation(
            de.codekenner.roadtrip.domain.Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    private class MyLocationListener implements LocationListener {
        private static final int TWO_MINUTES = 1000 * 60 * 2;
        private Location bestLocation;
        private int counter = 0;

        public void onLocationChanged(Location location) {
            if (bestLocation == null
                    || isBetterLocation(location, bestLocation)) {
                bestLocation = location;
                setCurrentLocation(new GPSLocation(bestLocation.getLongitude(),
                        bestLocation.getLatitude()));
            }
            if (enoughMeasurements() && isAccurateEnough()) {
                locationManager.removeUpdates(this);
            }
        }

        private boolean enoughMeasurements() {
            return counter++ > 10;
        }

        private boolean isAccurateEnough() {
            return bestLocation.hasAccuracy()
                    && bestLocation.getAccuracy() < 1000;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }

        /**
         * Determines whether one Location reading is better than the current
         * Location fix
         *
         * @param location            The new Location that you want to evaluate
         * @param currentBestLocation The current Location fix, to which you want to compare the
         *                            new one
         */
        protected boolean isBetterLocation(Location location,
                                           Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location,
            // use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must
                // be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                    .getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and
            // accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate
                    && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /**
         * Checks whether two providers are the same
         */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }
    }
}
