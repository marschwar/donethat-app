package de.codekenner.roadtrip.Activities;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import de.codekenner.roadtrip.DoneThatApplication;
import de.codekenner.roadtrip.R;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.RoadTripStorageService;

public class EditNoteActivity_old extends AbstractTripActivity {

    public static final String PARAM_TRIP_ID = "trip_id";
    public static final String STATE_DATE = "date";
    public static final String STATE_DESCRIPTION = "description";
    public static final String STATE_TITLE = "title";

    private Note currentNote;

    private Long currentTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note_old);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        currentNote = loadNoteFromExtras();

        final Bundle b = getIntent().getExtras();
        if (b != null) {
            currentTripId = b.getLong(PARAM_TRIP_ID);
            if (currentTripId == null) {
                throw new IllegalStateException(PARAM_TRIP_ID
                        + " Parameter fehlt");
            }
        } else {
            throw new IllegalStateException(PARAM_TRIP_ID + " Parameter fehlt");
        }
        if (currentNote == null) {
            currentNote = createNote();
        } else {
            setTitle(currentNote.getName());
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
        outState.putString(STATE_TITLE, getTitleField().getText().toString());
        outState.putString(STATE_DESCRIPTION, getTextField().getText().toString());
        outState.putString(STATE_DATE, getDateField().getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            getTitleField().setText(savedInstanceState.getString(STATE_TITLE));
            getTextField().setText(savedInstanceState.getString(STATE_DESCRIPTION));
            getDateField().setText(savedInstanceState.getString(STATE_DATE));
        }
    }

    private void updateViewFromModel() {
        getTitleField().setText(currentNote.getName());
        getTextField().setText(currentNote.getText());
        getDateField().setText(
                DateFormat.getDateFormat(this).format(
                        currentNote.getDate().getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            //case R.id.action_save:
                //    doSave();
                //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onDateFieldClicked(View view) {
        final Calendar cal = currentNote.getDate();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        final Calendar date = currentNote.getDate();
                        date.set(Calendar.YEAR, year);
                        date.set(Calendar.MONTH, monthOfYear);
                        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        getDateField().setText(
                                DateFormat.getDateFormat(EditNoteActivity_old.this)
                                        .format(date.getTime()));
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.show();
    }

    private void doSave() {
        final Note note = currentNote;
        note.setName(getTitleField().getText().toString());
        note.setText(getTextField().getText().toString());

        try {
            currentNote = RoadTripStorageService.instance()
                    .saveNote(this, note);
            finish();
        } catch (DataAccessException e) {
            showMessage(
                    "Leider ist ein Fehler aufgetreten.\n" + e.getMessage());
        }
    }

    private Note createNote() {
        final Note note = new Note(currentTripId);
        //note.setLocation(((DoneThatApplication) getApplication()).getLocationWrapper().getCurrentLocation());
        return note;
    }

    private TextView getDateField() {
        return (TextView) findViewById(R.id.editDate);
    }

    private TextView getTitleField() {
        return (EditText) findViewById(R.id.trip_name);
    }

    private TextView getTextField() {
        return (EditText) findViewById(R.id.trip_description);
    }
}
