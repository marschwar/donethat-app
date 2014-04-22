package de.codekenner.roadtrip.storage;

/**
 * Created by markus on 03.06.13.
 */
public interface ProgressCallback {
    /**
     *
     * @param progress 0-100
     */
    void publishProgress(Integer progress);
}
