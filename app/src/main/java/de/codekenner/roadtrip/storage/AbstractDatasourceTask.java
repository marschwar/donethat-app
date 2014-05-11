package de.codekenner.roadtrip.storage;

import android.content.Context;
import android.util.Log;

/**
 * Created by markus on 03.06.13.
 */
public abstract class AbstractDatasourceTask<Result> implements DatasourceTask<Result> {

    private long startTime;

    private final String name;

    protected AbstractDatasourceTask(String name) {
        this.name = name;
    }

    @Override
    public Result doWithDatasource(TripDataSource dataSource, ProgressCallback callback) throws DataAccessException {
        return this.doWithDatasource(dataSource);
    }

    protected abstract Result doWithDatasource(TripDataSource dataSource) throws DataAccessException;

    @Override
    public void preExectute(Context context) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void postExectute(Context context) {
        Log.i(name, String.format("execution time: %d ms", (System.currentTimeMillis() - startTime)));
    }

    @Override
    public void onSuccess(Context context) {

    }

}
