package de.codekenner.roadtrip.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.codekenner.roadtrip.R;
import de.codekenner.roadtrip.storage.AuthorizationService;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.LocalStorageService;
import de.codekenner.roadtrip.storage.RoadTripStorageService;
import de.codekenner.roadtrip.sync.SyncResult;
import de.codekenner.roadtrip.sync.SyncService;

public class WelcomeActivity extends AbstractTripActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Skip activity if trips already exist
        try {
            if (hasTrips()) {
                startActivity(new Intent(this, TripsActivity.class));
            }
        } catch (DataAccessException e) {
            showMessage(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final boolean isAuthorized = AuthorizationService.INSTANCE.isAuthorized(this);
        final Bundle b = getIntent().getExtras();
        if (b != null) {
            // Dies ist immer dann gesetzt, wenn der Benutzer neu angemeldet wurde
            if (b.containsKey("onLogin")) {
                // Menü neu aufbauen
                invalidateOptionsMenu();
            }
        }

        if (isAuthorized) {
            ((TextView) findViewById(R.id.tx_login_header)).setText("@" + new LocalStorageService().getScreenName(this));
            final String message = getResources().getString(R.string.hero_login_description_logged_in, new LocalStorageService().getUserName(this));
            ((TextView) findViewById(R.id.tx_login_description)).setText(message);
        } else {
            ((TextView) findViewById(R.id.tx_login_header)).setText(R.string.hero_login_title);
            ((TextView) findViewById(R.id.tx_login_description)).setText(getResources().getString(R.string.hero_login_description));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem loginItem = menu.findItem(R.id.action_login);
        final MenuItem logoutItem = menu.findItem(R.id.action_logout);
        if (AuthorizationService.INSTANCE.isAuthorized(this)) {
            loginItem.setVisible(false);
            logoutItem.setTitle("@" + new LocalStorageService().getScreenName(this) + " abmelden");
            logoutItem.setTitleCondensed("Abmelden");
        } else {
            logoutItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.action_sync);
        item.setVisible(SyncService.instance().canSync(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                doSyncEverything(false);
                return true;
            case R.id.action_sync_force:
                doSyncEverything(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doSyncEverything(boolean force) {
        final SyncService service = SyncService.instance();
        if (force) {
            new LocalStorageService().resetSuccessfulSync(this);
        }
        if (service.isConnected(this)) {
            try {
                final SyncResult result = service.sync(this);
                showMessage(String.format("Synchronisation erfolgreich.\n%d Reisen übermittelt und %d Reisen empfangen.",
                        result.getTripsSent(), result.getTripsReceived()));
                startActivity(new Intent(this, AllTripsActivity.class));
            } catch (DataAccessException e) {
                showMessage(e.getMessage());
            }
        }
    }

    public void createTrip(View view) {
        startActivity(new Intent(this, EditTripActivity.class));
    }

    // TODO Adapt the Twitter Auth Activity way
    public void onAuth(View view) {
        startActivity(new Intent(this,
                TwitterAuthActivity.class));
    }

    private boolean hasTrips() throws DataAccessException {
        return !(RoadTripStorageService.instance().getTrips(this).isEmpty());
    }

}
