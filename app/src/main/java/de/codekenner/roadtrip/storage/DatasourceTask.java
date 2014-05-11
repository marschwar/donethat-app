package de.codekenner.roadtrip.storage;

import android.content.Context;

public interface DatasourceTask<Result> {

    Result doWithDatasource(TripDataSource dataSource, ProgressCallback callback) throws DataAccessException;

    void preExectute(Context context);

    void postExectute(Context context);

    void onSuccess(Context context);

}
