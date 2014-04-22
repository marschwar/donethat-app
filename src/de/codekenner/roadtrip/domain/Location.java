package de.codekenner.roadtrip.domain;

public interface Location {
	public static final Location UNKNOWN = new UnknownLocation();

	public static final class UnknownLocation implements Location {
		private UnknownLocation() {
		}

		@Override
		public String getName() {
			return "unknown";
		}

		@Override
		public Double getLongitude() {
			return null;
		}

		@Override
		public Double getLatitude() {
			return null;
		}
	}

	String getName();

	Double getLongitude();

	Double getLatitude();

}