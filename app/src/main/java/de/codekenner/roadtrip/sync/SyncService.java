package de.codekenner.roadtrip.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.*;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by markus on 29.05.13.
 */
public class SyncService {

    private static final SyncService instance = new SyncService();

    private SyncService() {

    }

    public static SyncService instance() {
        return instance;
    }

    public boolean canSync(Context context) {
        return AuthorizationService.INSTANCE.isAuthorized(context) && isConnected(context);
    }

    /**
     * Prüft, ob eine Internetverbindung existiert
     *
     * @param context
     * @return
     */
    public boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public SyncResult sync(Context context) throws DataAccessException {
        return new TaskRunner<SyncResult>().run(context, new SyncTripsTask());
    }

    public SyncResult sync(Context context, final Trip trip) throws DataAccessException {
        return new TaskRunner<SyncResult>().run(context, new SyncTripTask(trip));
    }

    private long getLatestSyc() {
        return 0L;
    }

    public void deleteAll(Context context) throws DataAccessException {
        new TaskRunner<Boolean>().run(context, new AbstractDatasourceTask<Boolean>("deleteAll") {
            @Override
            protected Boolean doWithDatasource(TripDataSource dataSource) throws DataAccessException {
                dataSource.deleteAll();
                return true;
            }
        });
    }
}
