package de.codekenner.roadtrip;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.RoadTripStorageService;

public class EditTripActivity extends AbstractTripActivity {
    public static final String STATE_PUBLIC = "public";
    public static final String STATE_DESCRIPTION = "description";
    public static final String STATE_TITLE = "title";

    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        currentTrip = loadTripFromParameters();
        if (currentTrip != null) {
            setTitle(currentTrip.getName());
        }
        updateViewFromModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_TITLE, getNameField().getText().toString());
        outState.putString(STATE_DESCRIPTION, getDescriptionField().getText().toString());
        outState.putBoolean(STATE_PUBLIC, getPublicField().isChecked());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            getNameField().setText(savedInstanceState.getString(STATE_TITLE));
            getDescriptionField().setText(savedInstanceState.getString(STATE_DESCRIPTION));
            getPublicField().setChecked(savedInstanceState.getBoolean(STATE_PUBLIC));
        }
    }


    private void updateViewFromModel() {
        if (currentTrip != null) {
            getNameField().setText(currentTrip.getName());
            getDescriptionField().setText(currentTrip.getDescription());
            getPublicField().setChecked(currentTrip.getPublic());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save_trip:
                doSave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doSave() {
        final Trip trip = currentTrip != null ? currentTrip : new Trip();
        trip.setName(getNameField().getText().toString());
        trip.setDescription(getDescriptionField().getText().toString());
        trip.setPublic(getPublicField().isChecked());

        try {
            currentTrip = RoadTripStorageService.instance()
                    .saveTrip(this, trip);
            final Intent intent = new Intent(this, AllTripsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (DataAccessException e) {
            showMessage(
                    "Leider ist ein Fehler aufgetreten.\n" + e.getMessage());
        }
    }

    private TextView getNameField() {
        return (EditText) findViewById(R.id.trip_name);
    }

    private TextView getDescriptionField() {
        return (EditText) findViewById(R.id.trip_description);
    }

    private CheckBox getPublicField() {
        return (CheckBox) findViewById(R.id.chk_public);
    }

}
