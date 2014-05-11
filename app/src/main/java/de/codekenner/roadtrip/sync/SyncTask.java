package de.codekenner.roadtrip.sync;

import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.DatasourceTask;
import de.codekenner.roadtrip.storage.TripDataSource;
import org.apache.http.client.HttpClient;

/**
 * Derzeit lediglich ein Marker Interface
 * @param <Result>
 */
public interface SyncTask<Result> extends DatasourceTask<Result> {

}
