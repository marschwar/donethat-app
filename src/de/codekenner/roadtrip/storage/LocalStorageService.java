package de.codekenner.roadtrip.storage;

import android.content.Context;
import android.content.SharedPreferences;
import de.codekenner.roadtrip.sync.SyncTask;

import java.util.Set;

/**
 * Dieser Service verwaltet die privaten Einstellungen
 * Created by markus on 03.06.13.
 */
public class LocalStorageService {

    private static final String KEY_SCREEN_NAME = "screen_name";
    private static final String KEY_NAME = "name";
    private static final String KEY_USER_KEY = "userKey";
    private static final String KEY_USER_SECRET = "userSecret";
    private static final String KEY_LAST_SYNC = "userSecret";

    public String getUserToken(Context context) {
        return getString(context, KEY_USER_KEY);
    }

    public String getUserName(Context context) {
        return getString(context, KEY_NAME);
    }

    public String getScreenName(Context context) {
        return getString(context, KEY_SCREEN_NAME);
    }

    private String getString(Context context, String key) {
        final SharedPreferences prefs = getSharedPreferences(context);
        return prefs == null ? null : prefs.getString(key, null);
    }

    private Long getLong(Context context, String key, long defaultValue) {
        final SharedPreferences prefs = getSharedPreferences(context);
        return prefs == null ? null : prefs.getLong(key, defaultValue);
    }

    protected void put(Context ctx, String key, String value) {
        final SharedPreferences settings = getSharedPreferences(ctx);
        final SharedPreferences.Editor editor = settings.edit();

        if (value != null) {
            editor.putString(key, value);
        } else {
            editor.remove(key);
        }

        editor.commit();
    }

    protected void put(Context ctx, String key, Long value) {
        final SharedPreferences settings = getSharedPreferences(ctx);
        final SharedPreferences.Editor editor = settings.edit();

        if (value != null) {
            editor.putLong(key, value);
        } else {
            editor.remove(key);
        }

        editor.commit();
    }


    public void saveAuthData(Context ctx, String userKey, String userSecret, String name, String screenName) {
        final SharedPreferences settings = getSharedPreferences(ctx);
        final SharedPreferences.Editor editor = settings.edit();
        if (userKey != null) {
            editor.putString(KEY_USER_KEY, userKey);
        } else {
            editor.remove(KEY_USER_KEY);
        }
        if (userSecret != null) {
            editor.putString(KEY_USER_SECRET, userSecret);
        } else {
            editor.remove(KEY_USER_SECRET);
        }
        if (name != null) {
            editor.putString(KEY_NAME, name);
        } else {
            editor.remove(KEY_NAME);
        }
        if (screenName != null) {
            editor.putString(KEY_SCREEN_NAME, screenName);
        } else {
            editor.remove(KEY_SCREEN_NAME);
        }

        editor.commit();
    }

    public void resetSuccessfulSync(Context context) {
        final SharedPreferences sharedPreferences = getSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final Set<String> keys = sharedPreferences.getAll().keySet();
        for (String each : keys) {
            if (each.startsWith(KEY_LAST_SYNC)) {
                editor.remove(each);
            }
        }
        editor.commit();
    }

    public long getLastSuccessfulSync(Context context, SyncTask task) {
        final String key = KEY_LAST_SYNC + "_" + task.getClass().getSimpleName();
        final Long value = getLong(context, key, 0L);

        return value == null ? 0L : value;
    }

    public void saveLastSuccessfulSync(Context context, SyncTask task, long timestamp) {
        final String key = KEY_LAST_SYNC + "_" + task.getClass().getSimpleName();
        put(context, key, timestamp);

    }

    private SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences("roadtrip", Context.MODE_PRIVATE);
    }
}
