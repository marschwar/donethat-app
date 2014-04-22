package de.codekenner.roadtrip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.codekenner.roadtrip.domain.Location;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.MediaStorage;
import de.codekenner.roadtrip.storage.RoadTripStorageService;
import de.codekenner.roadtrip.view.LocationDialogFragment;

import java.io.FileNotFoundException;

public class NoteDetailActivity extends AbstractTripActivity implements LocationDialogFragment.LocationUpdateListener {

    private static final int SELECT_PHOTO = 87214376;

    private transient DisplayMetrics dm = null;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(getColumnCount() > 1 ? R.layout.activity_note_detail_multi_column
                : R.layout.activity_note_detail);
    }

    private void initView() {
        final LinearLayout viewContainer = (LinearLayout) findViewById(R.id.view_container);
        viewContainer.removeViews(1, viewContainer.getChildCount() - 1);

        if (currentNote != null) {

            setTitle(currentNote.getName());

            final LinearLayout headlineContainer = (LinearLayout) viewContainer
                    .findViewById(R.id.headline_container);
            headlineContainer.removeAllViews();

            final String headline = currentNote.getName();
            final String subheadline = DateFormat.getLongDateFormat(this)
                    .format(currentNote.getDate().getTime())
                    + " - "
                    + currentNote.getLocation().getName();

            if (getColumnCount() > 1) {
                final int columnWidth = getScreenWidth() / getColumnCount();

                addHeadline(columnWidth, headlineContainer, headline,
                        subheadline);
                headlineContainer.measure(0, 0);
                addImage(
                        headlineContainer,
                        columnWidth - (2 * getPadding()),
                        getAvailableScreenHeight()
                                - headlineContainer.getMeasuredHeight());
                headlineContainer.measure(0, 0);
                addTextView(headlineContainer, viewContainer,
                        currentNote.getText(),
                        headlineContainer.getMeasuredHeight());
            } else {
                addHeadline(0, headlineContainer, headline, subheadline);
                final int maxWidth = getScreenWidth() - (2 * getPadding());
                addImage(headlineContainer, maxWidth,
                        getAvailableScreenHeight());
                viewContainer.addView(createTextView(currentNote.getText()));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentNote = loadNoteFromExtras();
        initView();
    }

    private void addImage(final LinearLayout headlineContainer,
                          final int maxWidth, final int maxHeight) {
        if (currentNote.isWithImage()) {
            final Bitmap bitmap = new MediaStorage().loadNoteImage(currentNote);
            if (bitmap != null) {
                final ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(bitmap);
                imageView.setMaxWidth(maxWidth);
                imageView.setMaxHeight(maxHeight);
                imageView.setAdjustViewBounds(true);
                headlineContainer.addView(imageView, LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private int getScreenWidth() {
        return getDisplayMetrics().widthPixels;

    }

    private DisplayMetrics getDisplayMetrics() {
        if (dm == null) {
            dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm;
    }

    private int getColumnCount() {
        return getResources().getInteger(R.integer.column_count);
    }

    private void addTextView(LinearLayout targetContainer,
                             final LinearLayout viewContainer, String text, int offset) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        final DisplayMetrics displayMetrics = getDisplayMetrics();
        String currentText = text.toString().trim();

        final TextView textView = createTextView(text);
        int screenWidth = displayMetrics.widthPixels;
        int availableHeight = getAvailableScreenHeight() - offset;
        textView.setWidth(screenWidth / getColumnCount());
        targetContainer.addView(textView);
        textView.measure(0, 0);

        final int measuredHeight = textView.getMeasuredHeight();
        if (measuredHeight > availableHeight) {
            currentText = trimText(currentText, availableHeight, measuredHeight);
            textView.setText(currentText);
            final String remainder = (currentText == null || currentText
                    .isEmpty()) ? text : text
                    .substring(currentText.length() + 1);
            addTextView(viewContainer, viewContainer, remainder, 0);
        }
    }

    private int getAvailableScreenHeight() {
        return getDisplayMetrics().heightPixels
                - getResources().getInteger(R.integer.menu_bar_height);

    }

    private TextView createTextView(String text) {
        final float density = getDisplayMetrics().density;

        final TextView textView = new TextView(this);
        textView.setTextSize(10 * density);
        final int padding = getPadding();
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(text);

        return textView;
    }

    private int getPadding() {
        return (int) (10 * getDisplayMetrics().density);
    }

    /**
     * Trim the given text to the amount that will fit in the available space
     *
     * @param text
     * @param availableHeight
     * @param measuredHeight
     * @return
     */
    private String trimText(String text, int availableHeight,
                            final int measuredHeight) {

        if (availableHeight <= 0) {
            // no space available means no text
            return null;
        }
        if (text != null && !text.trim().isEmpty()) {
            text = text.substring(
                    0,
                    (int) Math.floor(text.length()
                            * ((double) availableHeight / measuredHeight)));
            final int endOfSentence = text.lastIndexOf('.');
            if (endOfSentence > -1 && endOfSentence > text.length() * 0.9) {
                text = text.substring(0, endOfSentence + 1);
            } else {
                final int lastWhiteSpace = text.lastIndexOf(' ');
                text = lastWhiteSpace > -1 ? text.substring(0, lastWhiteSpace)
                        : text;
            }
        }
        return text;
    }

    private void addHeadline(int columnWidth, final LinearLayout parent,
                             String headline, String subheadline) {
        final float density = getDisplayMetrics().density;
        final int padding = (int) (10 * density);

        final TextView v1 = new TextView(this);
        v1.setWidth(columnWidth);
        v1.setTextSize(15 * density);
        v1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v1.setPadding(padding, padding, padding, 0);
        v1.setText(headline);
        parent.addView(v1);
        if (subheadline != null) {
            final TextView v2 = new TextView(this);
            v2.setWidth(columnWidth);
            v2.setTextSize(8 * density);
            v2.setPadding(padding, 0, padding, padding);
            v2.setText(subheadline);
            parent.addView(v2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, TripNotesActivity.class);
                intent.putExtra(PARAM_ID, currentNote.getTripId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_edit:
                doEdit();
                return true;
            case R.id.action_update_location:
                doUpdateLocation();
                return true;
            case R.id.action_add_image:
                doAddImage();
                return true;
            case R.id.action_delete:
                doDelete();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void doUpdateLocation() {

        final LocationDialogFragment fragment = new LocationDialogFragment(currentNote);
        fragment.show(getFragmentManager(), "LocationDialog");
    }

    @Override
    public void onLocationUpdate(Location location) {
        currentNote.setLocation(location);
        try {
            RoadTripStorageService.instance().saveNote(this, currentNote);
            showMessage("Der Ort wurde aktualisiert. Neuer Standort ist "
                    + location.getName());
            initView();
        } catch (DataAccessException e) {
            showMessage(
                    "Fehler beim Speichern der Notiz\n" + e.getMessage());
        }
    }

    private void doEdit() {
        final Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(PARAM_ID, currentNote.getId());
        startActivity(intent);
    }

    private void doDelete() {
        final long tripId = currentNote.getTripId();
        final String message = "Bist du dir ganz sicher, dass du " + currentNote.getName() + " löschen möchstest?";
        new AlertDialog.Builder(this).setPositiveButton("Ok", new OnDeleteListener()).setCancelable(true).setMessage(message).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    onImageSelected(intent);
                }
        }
    }

    private void onImageSelected(Intent intent) {
        try {
            final Bitmap selectedImage = decodeUri(intent.getData());
            if (selectedImage != null) {
                currentNote = RoadTripStorageService.instance().assignBitmap(
                        this, currentNote, selectedImage);
            }
        } catch (FileNotFoundException e) {
            showMessage("Datei nicht gefunden.\n" + e.getMessage());
        } catch (DataAccessException e) {
            showMessage("Fehler beim speichern.\n" + e.getMessage());
        }
    }

    private void doAddImage() {
        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 800;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                getContentResolver().openInputStream(selectedImage), null, o2);

    }

    private class OnDeleteListener implements DialogInterface.OnClickListener {

        public void onClick(DialogInterface dialog, int which) {
            try {
                final NoteDetailActivity that = NoteDetailActivity.this;
                RoadTripStorageService.instance().deleteNote(that, currentNote);
                final long tripId = currentNote.getTripId();
                final Intent parent = new Intent(that, TripNotesActivity.class);
                parent.putExtra(PARAM_ID, tripId);
                NavUtils.navigateUpTo(that, parent);
            } catch (DataAccessException e) {
                showMessage("Ein Fehler ist aufgetreten:\n" + e.getMessage());
            }
        }
    }
}
