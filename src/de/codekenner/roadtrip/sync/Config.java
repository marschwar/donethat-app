package de.codekenner.roadtrip.sync;

/**
 * Created by markus on 31.05.13.
 */
interface Config {

    static final String URL_GET_TRIPS = "trips/%d";
    static final String URL_POST_TRIPS = "trips";

    static final String URL_GET_NOTES = "notes/%s/%d";
    static final String URL_POST_NOTES = "notes/%s";
    static final String URL_POST_NOTE_IMAGE = "image/%s";

    static final String URL_GET_TRIP = "trip/%s";

}
