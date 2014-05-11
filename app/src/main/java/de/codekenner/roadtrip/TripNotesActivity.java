package de.codekenner.roadtrip;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.MediaStorage;
import de.codekenner.roadtrip.storage.RoadTripStorageService;
import de.codekenner.roadtrip.sync.SyncResult;
import de.codekenner.roadtrip.sync.SyncService;

import java.util.List;
import java.util.Random;

public class TripNotesActivity extends AbstractTripActivity {

    private List<Trip> allTrips;

    private Trip currentTrip;

    private List<Note> currentNotes;

    private Random imageShuffle;

    private boolean imageMemoryHack = false;

    public TripNotesActivity() {
        super();
        imageShuffle = new Random();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_notes);

        // Dies muss immer in der Main Activity passieren
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        imageMemoryHack = sharedPref.getBoolean("pref_memoryhack", false);

        try {
            allTrips = RoadTripStorageService.instance().getTrips(this);
            if (allTrips.isEmpty()) {
                finish();
                startActivity(new Intent(this, MainActivity.class));
                return;
            }
        } catch (DataAccessException e) {
            showMessage(e.getMessage());
        }


        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        final ArrayAdapter<Trip> spinnerAdapter = new ArrayAdapter<Trip>(this, R.layout.spinner_dropdown_item);
        spinnerAdapter.addAll(allTrips);


        actionBar.setListNavigationCallbacks(spinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                final Trip selectedTrip = allTrips.get(itemPosition);
                if (selectedTrip != null && !selectedTrip.equals(currentTrip)) {
                    currentTrip = selectedTrip;
                    loadNotes();
                    initBlocks();
                }
                return false;
            }
        });

        currentTrip = loadTripFromParameters();
        if (currentTrip == null) {
            currentTrip = allTrips.get(0);
        } else {
            // find index of current trip
            for (int i = 0; i < allTrips.size(); i++) {
                if (allTrips.get(i).getUid().equals(currentTrip.getUid())) {
                    actionBar.setSelectedNavigationItem(i);
                }
            }
        }
        setTitle(null);
    }

    private void loadNotes() {
        try {
            currentNotes = RoadTripStorageService.instance().getNotes(this,
                    currentTrip);
        } catch (DataAccessException e) {
            showMessage(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadNotes();
        initBlocks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                doNewNote();
                return true;
            case R.id.action_edit:
                doEditTrip();
                return true;
            case R.id.action_sync:
                doSyncTrip();
                return true;
            case android.R.id.home:
                startActivity(new Intent(this, AllTripsActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doSyncTrip() {
        final SyncService service = SyncService.instance();
        if (service.isConnected(this)) {
            try {
                final SyncResult result = service.sync(this, currentTrip);
                showMessage(String.format("Synchronisation erfolgreich.\n%d Reisen übermittelt und %d Reisen empfangen.",
                        result.getTripsSent(), result.getTripsReceived()));
            } catch (DataAccessException e) {
                showMessage(e.getMessage());
            }
        }
    }

    private void doNewNote() {
        final Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.PARAM_TRIP_ID, currentTrip.getId());
        startActivity(intent);
    }

    private void doEditTrip() {
        final Intent intent = new Intent(this, EditTripActivity.class);
        intent.putExtra("id", currentTrip.getId());
        startActivity(intent);
    }

    private void initBlocks() {
        final ViewGroup container = (ViewGroup) findViewById(R.id.block_container);
        container.removeAllViews();

        int idx = 0;
        if (currentNotes != null) {
            int imgCountHack = 0;
            for (Note note : currentNotes) {
                idx = idx + 1;

                final View block = getLayoutInflater().inflate(getBlockLayout(note, idx), null);
                final TextView headline = (TextView) block
                        .findViewById(R.id.block_headline);
                headline.setText(note.getName());

                final TextView subheadline = (TextView) block
                        .findViewById(R.id.block_subheadline);
                subheadline.setText(DateFormat.getLongDateFormat(this).format(
                        note.getDate().getTime()));

                final TextView details = (TextView) block
                        .findViewById(R.id.block_text);
                details.setText(note.getText());

                if (note.isWithImage()) {
                    imgCountHack++;
                    if (imageMemoryHack && imgCountHack > 10) {
                        // noop - wir zeigen nur die letzten 10 Bilder
                    } else {
                        final Bitmap bitmap = new MediaStorage()
                                .loadNoteImage(note);
                        if (bitmap != null) {
                            final ImageView img = (ImageView) block
                                    .findViewById(R.id.block_image);
                            img.setImageBitmap(bitmap);
                        }
                    }
                }

                block.setOnClickListener(new NoteSelectionListener(this, note));

                container.addView(block);
            }
        }
    }

    private int getBlockLayout(Note note, int idx) {
        return note.isWithImage() ? R.layout.dynamic_block_image_bg
                : R.layout.dynamic_block;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trip_notes, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.action_sync);
        item.setVisible(SyncService.instance().canSync(this));
        return true;
    }

    private static class NoteSelectionListener implements OnClickListener {

        private final Context context;

        private final Note note;

        public NoteSelectionListener(Context context, Note note) {
            super();
            this.context = context;
            this.note = note;
        }

        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(context, NoteDetailActivity.class);
            intent.putExtra(PARAM_ID, note.getId());

            context.startActivity(intent);
        }

    }

}
