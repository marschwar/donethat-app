package de.codekenner.roadtrip;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import de.codekenner.roadtrip.storage.AuthorizationService;
import de.codekenner.roadtrip.storage.LocalStorageService;
import de.codekenner.roadtrip.storage.RoadTripStorageService;

import java.io.Serializable;

/**
 * Created by markus on 24.05.13.
 */
public class TwitterAuthActivity extends AbstractTripActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_auth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AuthorizationService.INSTANCE.isAuthorized(this)) {
            startMain(false);
            return;
        }
        startOAuth();
    }

    private void startOAuth() {
        // Open the browser
        try {
            final String authUrl = AuthorizationService.INSTANCE.initiateAuthentication(this);
            if (authUrl != null) {
                final WebView webView = (WebView) findViewById(R.id.webView);
                webView.loadUrl(authUrl);
            }
        } catch (Exception e) {
            showMessage(e.getMessage());
            Log.e("OAuath", "geht nicht", e);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final Uri uri = intent.getData();
        if (uri == null || !uri.toString().startsWith(AuthorizationService.CALLBACKURL)) {
            // von irgendwo anders
            return;
        }
        boolean success = false;
        try {
            success = AuthorizationService.INSTANCE.processCallback(this, uri);
        } catch (Exception e) {
            Log.e("TwitterAuth", "Fehler bei der Verarbeitung des Response", e);
        }
        startMain(success);
    }

    private void startMain(boolean loginStatusChanged) {
        final Intent intent = new Intent(this, MainActivity.class);
        if (loginStatusChanged) {
            intent.putExtra("onLogin", Boolean.TRUE);
        }
        startActivity(intent);
    }
}