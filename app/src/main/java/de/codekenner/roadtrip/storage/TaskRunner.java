/**
 *
 */
package de.codekenner.roadtrip.storage;

import android.content.Context;

/**
 * @author markus
 */
public class TaskRunner<Result> {

    public Result run(Context context, DatasourceTask<Result> task)
            throws DataAccessException {
        final DatasourceAsyncTask<Result> asyncTask = new DatasourceAsyncTask<Result>(
                context, task);
        Result result = null;
        try {
            task.preExectute(context);
            asyncTask.execute();

            result = asyncTask.get();
        } catch (Exception e) {
            throw new DataAccessException(e);
        } finally {
            task.postExectute(context);
        }
        if (asyncTask.hasError()) {
            throw new DataAccessException(asyncTask.getError());
        }
        task.onSuccess(context);
        return result;
    }
}
