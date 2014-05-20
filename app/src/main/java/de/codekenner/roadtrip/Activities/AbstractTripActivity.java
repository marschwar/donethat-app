/**
 *
 */
package de.codekenner.roadtrip.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import de.codekenner.roadtrip.R;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.AuthorizationService;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.RoadTripStorageService;
import de.codekenner.roadtrip.sync.SyncService;

/**
 * @author markus
 */
public abstract class AbstractTripActivity extends Activity {
    public static final String PARAM_ID = "id";
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alertDialog = new AlertDialog.Builder(this).setPositiveButton("Ok", null).setCancelable(true).create();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        alertDialog.dismiss();
    }

    public void showMessage(final String message) {
        showMessage(message, null);
    }

    public void showMessage(final String message, DialogInterface.OnDismissListener onDismissListener) {
        alertDialog.setMessage(message);
        alertDialog.setOnDismissListener(onDismissListener);
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem itemSync = menu.findItem(R.id.action_sync);
        if (itemSync != null) {
            itemSync.setVisible(SyncService.instance().canSync(this));
        }

        final MenuItem itemLogout = menu.findItem(R.id.action_logout);
        if (itemLogout != null) {
            itemLogout.setVisible(AuthorizationService.INSTANCE.isAuthorized(this));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                doLogout();
                return true;
            case R.id.action_login:
                doLogin();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initializes the current trip by checking for an id in the intent extras.
     * NOTE! Do not call this prior to {@link #onResume()} or after
     * {@link #onPause()}
     */
    protected Trip loadTripFromParameters() {
        final Bundle b = getIntent().getExtras();
        if (b != null) {
            final Long id = b.getLong(PARAM_ID);
            if (id != null && id > 0) {
                try {
                    return RoadTripStorageService.instance().getTrip(this, id);
                } catch (DataAccessException e) {
                    showMessage(e.getMessage());
                }
            }
        }
        return null;
    }

    protected Note loadNoteFromExtras() {
        final Bundle b = getIntent().getExtras();
        if (b != null) {
            final Long id = b.getLong(PARAM_ID);
            if (id != null && id > 0) {
                try {
                    return RoadTripStorageService.instance().getNote(this, id);
                } catch (DataAccessException e) {
                    showMessage(e.getMessage());
                }
            }
        }
        return null;
    }

    protected void reloadActivity() {
        final Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
    }

    protected void doLogout() {
        AuthorizationService.INSTANCE.resetAuthorization(this);
        invalidateOptionsMenu();
        Toast.makeText(this, "Du bist nun abgemeldet. Synchronisieren ist nicht möglich", Toast.LENGTH_LONG);
    }

    protected void doLogin() {
        startActivity(new Intent(this, TwitterAuthActivity.class));
    }

    public void doLogin(View view) {
        doLogin();
    }
}
