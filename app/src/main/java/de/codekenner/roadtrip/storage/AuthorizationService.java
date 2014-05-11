package de.codekenner.roadtrip.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
//import oauth.signpost.OAuthProvider;
//import oauth.signpost.basic.DefaultOAuthProvider;
//import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

/**
 * Created by markus on 24.05.13.
 */
public class AuthorizationService {

    public static final AuthorizationService INSTANCE = new AuthorizationService();


    private static final String LOG_TAG = "AuthorizationService";
    public final static String consumerKey = "MOVE_TO_ENV";
    public final static String consumerSecret = "MOVE_TO_ENV";
    public static final String CALLBACKURL = "app://twitter";

    private final OAuthProvider httpOauthprovider = new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token", "https://api.twitter.com/oauth/authorize");
    private final CommonsHttpOAuthConsumer httpOauthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
    private final LocalStorageService localStorageService;

    private AuthorizationService() {
        localStorageService = new LocalStorageService();
    }

    public boolean isAuthorized(Context context) {
        return localStorageService.getUserToken(context) != null;
    }

    public String initiateAuthentication(Context ctx) throws ExecutionException, InterruptedException {
        final AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    return httpOauthprovider.retrieveRequestToken(httpOauthConsumer, CALLBACKURL);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Fehler beim erzeugen der AuthURL", e);
                    return null;
                }
            }
        };
        task.execute();

        return task.get();
    }

    public void resetAuthorization(Context context) {
        localStorageService.saveAuthData(context, null, null, null, null);
    }

    /**
     * @param callbackUri Die URL die von Twitter als aufgerufen wurde. Wenn alles geklappt hat, dann enthält diese
     *                    das User Token
     * @return true, wenn der Benutzer erfolgreich angemeldet werden konnte. Sonst false
     */
    public boolean processCallback(Context ctx, Uri callbackUri) {

        if (callbackUri == null || !callbackUri.toString().startsWith(CALLBACKURL)) {
            // Callback kam nicht von Twitter
            return false;
        }
        final String verifier = callbackUri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);

        try {
            // this will populate token and token_secret in consumer
            final AsyncTask<String, Integer, Boolean> task = new AsyncTask<String, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(String... params) {
                    try {
                        httpOauthprovider.retrieveAccessToken(httpOauthConsumer, params[0]);
                        return true;
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Fehler beim verarbeiten der Callback URL", e);
                        return false;
                    }
                }
            }.execute(verifier);

            // Auf Ergebnis warten
            final Boolean success = task.get();
            if (success) {

                final String userKey = httpOauthConsumer.getToken();
                final String userSecret = httpOauthConsumer.getTokenSecret();

                final JSONObject data = loadUserData(ctx);
                final String name = data.getString("name");
                final String screenName = data.getString("screen_name");

                // Save user_key and user_secret in user preferences and return
                localStorageService.saveAuthData(ctx, userKey, userSecret, name, screenName);

            }
            return success;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Fehler bei der Verarbeitung des Response", e);
            return false;
        }

    }

    private JSONObject loadUserData(Context context) {
        final AsyncTask<String, Integer, JSONObject> task = new AsyncTask<String, Integer, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... params) {
                try {
                    final HttpGet get = new HttpGet("https://api.twitter.com/1.1/account/verify_credentials.json");
                    final HttpParams httpParams = new BasicHttpParams();
                    HttpProtocolParams.setUseExpectContinue(httpParams, false);
                    get.setParams(httpParams);
                    // sign the request to authenticate
                    httpOauthConsumer.sign(get);
                    DefaultHttpClient client = new DefaultHttpClient();
                    final String responsex = client.execute(get, new BasicResponseHandler());
                    Log.i(LOG_TAG, responsex);
                    return new JSONObject(responsex);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Fehler beim Holen der Benutzerdaten", e);
                    return null;
                }
            }
        }.execute();
        try {
            return task.get();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Fehler beim Holen der Benutzerdaten", e);
            return null;
        }
    }

}
