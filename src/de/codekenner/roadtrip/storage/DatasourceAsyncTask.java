/**
 * 
 */
package de.codekenner.roadtrip.storage;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author markus
 * 
 */
public class DatasourceAsyncTask<Result> extends AsyncTask<Void, Integer, Result> implements ProgressCallback {

	private final TripDataSource dataSource;
	private final DatasourceTask<Result> task;

    private ProgressDialog progressDialog;

    private String error = null;

	public DatasourceAsyncTask(Context context, DatasourceTask<Result> task) {
		super();
		this.dataSource = new TripDataSource(context);
		this.task = task;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Gleich geht's weiter");
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		dataSource.close();
        progressDialog.dismiss();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dataSource.open();
        progressDialog.show();
	}

	@Override
	protected Result doInBackground(Void... params) {
        try {
            return task.doWithDatasource(dataSource, this);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Fehler im Async", e);
            error = e.getLocalizedMessage();
            return null;
        }
    }

    public boolean hasError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }

    @Override
    public void publishProgress(Integer progress) {
        super.publishProgress(progress);
    }
}
