package de.codekenner.roadtrip;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.codekenner.roadtrip.domain.SharedData;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.AuthorizationService;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.LocalStorageService;
import de.codekenner.roadtrip.storage.RoadTripStorageService;
import de.codekenner.roadtrip.sync.SyncResult;
import de.codekenner.roadtrip.sync.SyncService;

public class AllTripsActivity extends AbstractTripActivity {

    private List<Trip> trips = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_trips);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.all_trips, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(SyncService.instance().canSync(this));

        final MenuItem loginItem = menu.findItem(R.id.action_login);
        loginItem.setVisible(!AuthorizationService.INSTANCE.isAuthorized(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                doCreate();
                return true;
            case R.id.action_sync:
                doSyncEverything(false);
                return true;
            case R.id.action_sync_force:
                doSyncEverything(true);
                return true;
            case R.id.action_factory_reset:
                doFactoryReset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doFactoryReset() {
        try {
            SyncService.instance().deleteAll(this);
            new LocalStorageService().resetSuccessfulSync(this);
            showMessage("Alles zurückgesetzt.");
            final Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (DataAccessException e) {
            showMessage(e.getMessage());
        }
    }

    private void doSyncEverything(boolean force) {
        if (force) {
            new LocalStorageService().resetSuccessfulSync(this);
        }
        final SyncService service = SyncService.instance();
        if (service.isConnected(this)) {
            try {
                final SyncResult result = service.sync(this);
                showMessage(String.format("Synchronisation erfolgreich.\n%d Reisen übermittelt und %d Reisen empfangen.",
                        result.getTripsSent(), result.getTripsReceived()), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refresh();
                    }
                });

            } catch (DataAccessException e) {
                showMessage(e.getMessage());
            }
        }
    }

    private void doCreate() {
        startActivity(new Intent(this, EditTripActivity.class));
    }

    private void refresh() {
        trips = null;
        initBlocks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final boolean isAuthorized = AuthorizationService.INSTANCE.isAuthorized(this);
        if (!isAuthorized && getTrips().isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        initBlocks();
    }

    private void initBlocks() {
        final ViewGroup container = (ViewGroup) findViewById(R.id.block_container);
        container.removeAllViews();
        final List<Trip> allTrips = getTrips();
        for (Trip trip : allTrips) {
            int layout = R.layout.dynamic_block;
            Bitmap anyImage = null;
            try {
                anyImage = RoadTripStorageService.instance().getImage(this, trip);
            } catch (DataAccessException e) {
                showMessage(e.getMessage());
            }
            if (anyImage != null) {
                layout = R.layout.dynamic_block_image_bg;
            }

            final View block = getLayoutInflater().inflate(layout, null);
            final TextView headline = (TextView) block
                    .findViewById(R.id.block_headline);
            headline.setText(trip.getName());

            final TextView subheadline = (TextView) block
                    .findViewById(R.id.block_subheadline);
            subheadline.setVisibility(View.INVISIBLE);

            final TextView details = (TextView) block
                    .findViewById(R.id.block_text);
            details.setText(trip.getDescription() == null ? "" : trip
                    .getDescription());

            if (anyImage != null) {
                ((ImageView) block.findViewById(R.id.block_image))
                        .setImageBitmap(anyImage);
            }

            block.setOnClickListener(new TripSelectedListener(this, trip));

            container.addView(block);
        }
    }

    private List<Trip> getTrips() {
        if (trips == null) {
            try {
                trips = RoadTripStorageService.instance().getTrips(this);
            } catch (DataAccessException e) {
                trips = Collections.emptyList();
                showMessage(
                        "Fehler beim Laden der Reisen\n" + e.getMessage());
            }
        }
        return trips;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Force reload on return
        trips = null;
    }

    private static class TripSelectedListener implements OnClickListener {

        private final Context context;

        private final SharedData trip;

        public TripSelectedListener(Context context, SharedData trip) {
            super();
            this.context = context;
            this.trip = trip;
        }

        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(context, TripNotesActivity.class);
            intent.putExtra("id", trip.getId());
            context.startActivity(intent);
        }

    }

}
