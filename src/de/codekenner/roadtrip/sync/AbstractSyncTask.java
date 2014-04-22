package de.codekenner.roadtrip.sync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import de.codekenner.roadtrip.SettingsActivity;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.LocalStorageService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by markus on 31.05.13.
 */
public abstract class AbstractSyncTask<Result> implements SyncTask<Result> {

    private static final String LOG_TAG = AbstractSyncTask.class.getName();

    private final LocalStorageService localStorageService;
    private long lastSync;
    private String userToken = null;

    private String baseUrl = null;

    protected AbstractSyncTask() {
        this.localStorageService = new LocalStorageService();
    }

    @Override
    public void preExectute(Context context) {
        lastSync = localStorageService.getLastSuccessfulSync(context, this);
        userToken = localStorageService.getUserToken(context);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        baseUrl = sharedPref.getString(SettingsActivity.KEY_SYNC_SERVER, "");
    }

    @Override
    public void postExectute(Context context) {

    }

    @Override
    public void onSuccess(Context context) {
        localStorageService.saveLastSuccessfulSync(context, this, System.currentTimeMillis());
    }

    protected JSONObject doGet(String urlTemplate, Object... params) throws DataAccessException {
        HttpURLConnection con = null;
        try {
            con = createConnection(urlTemplate, params);
            final JSONObject result = new JSONObject(readStream(con.getInputStream()));
            Log.d(LOG_TAG, result.toString());
            return result;
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    protected JSONArray doPost(String urlTemplate, Bitmap image, Object... params) throws DataAccessException {
        HttpURLConnection con = null;
        try {
            con = createConnection(urlTemplate, params);
            con.setRequestProperty("Content-Type", "image/jpeg");
            con.setDoOutput(true);
            image.compress(Bitmap.CompressFormat.JPEG, 100, con.getOutputStream());
            final JSONArray result = new JSONArray(readStream(con.getInputStream()));
            Log.d(LOG_TAG, result.toString());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    protected JSONArray doPost(String urlTemplate, JSONArray data, Object... params) throws DataAccessException {
        HttpURLConnection con = null;
        try {
            con = createConnection(urlTemplate, params);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            writeStream(con.getOutputStream(), data);
            final JSONArray result = new JSONArray(readStream(con.getInputStream()));
            Log.d(LOG_TAG, result.toString());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    private HttpURLConnection createConnection(String urlTemplate, Object[] params) throws IOException, DataAccessException {
        String u = String.format(urlTemplate, params);
        URL url = new URL(getBaseUrl() + u);
        HttpURLConnection con = (HttpURLConnection) url
                .openConnection();
        if (userToken == null) {
            throw new DataAccessException(("Du bist nicht angemeldet"));
        }
        con.setRequestProperty("X-Auth-Token", userToken);
        con.setRequestProperty("Accept", "application/json");

        return con;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = null;
        final StringBuilder result = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    private void writeStream(OutputStream out, JSONArray data) throws IOException {
        byte[] outputBytes = data.toString().getBytes("UTF-8");
        out.write(outputBytes);
    }

    protected long getLastSync() {
        return lastSync;
    }

    protected void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    protected String getUserToken() {
        return userToken;
    }

    protected void setUserToken(String userToken) {
        this.userToken = userToken;
    }

}
