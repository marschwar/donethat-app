package de.codekenner.roadtrip.sync;

import android.graphics.Bitmap;
import de.codekenner.roadtrip.domain.GPSLocation;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.MediaStorage;
import de.codekenner.roadtrip.storage.ProgressCallback;
import de.codekenner.roadtrip.storage.TripDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static de.codekenner.roadtrip.sync.Config.URL_GET_NOTES;
import static de.codekenner.roadtrip.sync.Config.URL_POST_NOTES;
import static de.codekenner.roadtrip.sync.Config.URL_POST_NOTE_IMAGE;

/**
 * Created by markus on 31.05.13.
 */
public class SendNoteImageTask extends AbstractSyncTask<SyncResult> {

    private final Note note;

    public SendNoteImageTask(Note note, long lastSync, String userToken, String baseUrl) {
        this.note = note;
        setLastSync(lastSync);
        setUserToken(userToken);
        setBaseUrl(baseUrl);
    }

    @Override
    public SyncResult doWithDatasource(TripDataSource dataSource, ProgressCallback callback) throws DataAccessException {
        final MediaStorage mediaStorage = new MediaStorage();
        final Bitmap bitmap = mediaStorage.loadNoteImage(note);

        doPost(URL_POST_NOTE_IMAGE, bitmap, note.getUid());

        return null;
    }
}
