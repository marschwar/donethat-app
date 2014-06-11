package de.codekenner.roadtrip.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.codekenner.roadtrip.DoneThatApplication;
import de.codekenner.roadtrip.R;
import de.codekenner.roadtrip.domain.Location;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.RoadTripStorageService;

public class EditNoteActivity extends Activity {

    private Note currentNote;
    private Long tripID;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // Load Trip
        tripID = getIntent().getLongExtra("ID_TRIP", -1);
        assert (tripID != -1);

        // Load Note
        Long noteID = getIntent().getLongExtra("ID_NOTE", -1);
        if (noteID != -1) {
            currentNote = loadNoteFromID(noteID);
        } else {
            currentNote = createNewNoteForTrip(tripID);
        }
        assert (currentNote != null);

        // Set Date in EditText
        TextView dateView = (TextView) findViewById(R.id.editDate);
        Calendar date = currentNote.getDate();
        this.selectedDate = date;

        final int year = date.get(Calendar.YEAR);
        final int month = date.get(Calendar.MONTH);
        final int day = date.get(Calendar.DAY_OF_MONTH);

        setDate(year, month, day);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog mDatePicker = new DatePickerDialog(EditNoteActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        setDate(selectedyear, selectedmonth, selectedday);
                    }
                }, year, month, day);

                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        // Set-Up Switch mechanics
        Switch locationSwitch = (Switch) findViewById(R.id.switchLocation);
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                EditText editLocation = (EditText) findViewById(R.id.editLocation);

                if (isChecked) {
                    editLocation.setVisibility(View.INVISIBLE);

                    // Enable GPS
                    ((DoneThatApplication) getApplication()).startSearchingForLocation();
                } else {

                    editLocation.setVisibility(View.VISIBLE);
                    editLocation.setText(((DoneThatApplication) getApplication()).getLocationAndStopListening().toString());
                }
            }
        });


        // Set-Up manual Location picker
        // TODO


        // Enable GPS
        ((DoneThatApplication) getApplication()).startSearchingForLocation();
    }

    private Note createNewNoteForTrip(Long id) {
        Note newNote = new Note();
        newNote.setTripId(id);

        return newNote;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save_node) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDate(int year, int month, int day) {
        TextView dateView = (TextView) findViewById(R.id.editDate);

        Calendar date = Calendar.getInstance();
        date.set(year, month, day);

        dateView.setText(date.getTime().toString());
        this.selectedDate = date;
    }

    protected Note loadNoteFromID(Long id) {
        if (id != null && id > 0) {
            try {
                return RoadTripStorageService.instance().getNote(this, id);
            } catch (DataAccessException e) {
                Log.e("EditNoteActivity", e.getMessage());
            }
        }
        return null;
    }

    private void saveNote() {
        final Note note = currentNote;
        note.setName(((EditText) findViewById(R.id.editTitle)).getText().toString());
        note.setText(((EditText) findViewById(R.id.editDescription)).getText().toString());
        note.setDate(this.selectedDate);

        note.setLocation(((DoneThatApplication) getApplication()).getLocationAndStopListening());

        try {
            currentNote = RoadTripStorageService.instance().saveNote(this, note);
            finish();
        } catch (DataAccessException e) {
            Log.e("EditNoteActivity", "Leider ist ein Fehler aufgetreten.\n" + e.getMessage());
        }
    }
}
