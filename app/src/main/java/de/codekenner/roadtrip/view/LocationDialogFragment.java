package de.codekenner.roadtrip.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import de.codekenner.roadtrip.R;
import de.codekenner.roadtrip.domain.GPSLocation;
import de.codekenner.roadtrip.domain.Location;
import de.codekenner.roadtrip.domain.Note;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by markus on 18.07.13.
 */
public class LocationDialogFragment extends DialogFragment {

    private final NumberFormat numberFormat;

    private final Note note;
    private LocationUpdateListener listener;

    public LocationDialogFragment(Note note) {
        this.note = note;

        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(6);
        numberFormat.setMinimumIntegerDigits(1);
    }

    public static interface LocationUpdateListener {
        void onLocationUpdate(Location location);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (LocationUpdateListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LocationUpdateListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle("Ort manuell ändern");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_location, null);
        final Location location = note.getLocation();

        final EditText txLongitude = (EditText) view.findViewById(R.id.longitude);
        txLongitude.setText((location != null && location.getLongitude() != null) ? numberFormat.format(location.getLongitude()) : null);

        final EditText txLatitude = (EditText) view.findViewById(R.id.latitude);
        txLatitude.setText((location != null && location.getLatitude() != null) ? numberFormat.format(location.getLatitude()) : null);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final Double longitude = parse(txLongitude);
                        final Double latitude = parse(txLatitude);
                        listener.onLocationUpdate((latitude != null && longitude != null) ? new GPSLocation(longitude, latitude) : Location.UNKNOWN);
                    }

                    private Double parse(EditText widget) {
                        try {
                            return numberFormat.parse(widget.getText().toString()).doubleValue();
                        } catch (ParseException ignore) {
                        }
                        return null;
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LocationDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
