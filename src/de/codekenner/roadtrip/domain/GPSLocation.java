/**
 * 
 */
package de.codekenner.roadtrip.domain;

import java.util.Locale;

/**
 * @author markus
 * 
 */
public class GPSLocation implements Location {
	private final double longitude;
	private final double latitude;

	public GPSLocation(double longitude, double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	@Override
	public String getName() {
		return String.format(Locale.ENGLISH, "%1.5f, %1.5f", longitude,
				latitude);
	}

}
