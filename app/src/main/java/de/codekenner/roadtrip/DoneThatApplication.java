/**
 *
 */
package de.codekenner.roadtrip;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import de.codekenner.roadtrip.domain.GPSLocation;

/**
 * @author markus
 */
public class DoneThatApplication extends android.app.Application {

    private LocationWrapper locationWrapper;
    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        locationWrapper = new LocationWrapper(this);

        // Create Volley queue
        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public void startSearchingForLocation() {
        locationWrapper.startSearchingForLocation();
    }

    public de.codekenner.roadtrip.domain.Location getLocationAndStopListening() {
        return locationWrapper.getLocationAndStopListening();
    }
}
