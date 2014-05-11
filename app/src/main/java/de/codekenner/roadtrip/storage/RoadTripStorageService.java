/**
 *
 */
package de.codekenner.roadtrip.storage;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;

/**
 * @author markus
 */
public class RoadTripStorageService {
    private static final RoadTripStorageService instance = new RoadTripStorageService();

    public static RoadTripStorageService instance() {
        return instance;
    }

    public Trip getTrip(Context context, final long id)
            throws DataAccessException {
        return new TaskRunner<Trip>().run(context, new AbstractDatasourceTask<Trip>("getTripById") {

            @Override
            public Trip doWithDatasource(TripDataSource dataSource) {
                return dataSource.getTrip(id);
            }
        });
    }

    public Note getNote(Context context, final long id)
            throws DataAccessException {
        return new TaskRunner<Note>().run(context, new AbstractDatasourceTask<Note>("getNoteById") {

            @Override
            public Note doWithDatasource(TripDataSource dataSource) {
                return dataSource.getNote(id);
            }
        });
    }

    public void deleteNote(final Context context, final Note note)
            throws DataAccessException {
        new TaskRunner<Boolean>().run(context, new AbstractDatasourceTask<Boolean>("deleteNote") {

            @Override
            public Boolean doWithDatasource(TripDataSource dataSource) throws DataAccessException {
                if (note == null) {
                    return Boolean.FALSE;
                }
                final Trip trip = dataSource.getTrip(note.getTripId());

                if (note.isWithImage()) {
                    new MediaStorage().deleteNoteImage(note);
                }
                dataSource.deleteNote(note);
                trip.setChanged(now());
                dataSource.save(trip);
                return Boolean.TRUE;
            }
        });
    }

    public Note saveNote(Context context, final Note note)
            throws DataAccessException {
        return new TaskRunner<Note>().run(context, new AbstractDatasourceTask<Note>("saveNote") {

            @Override
            public Note doWithDatasource(TripDataSource dataSource) {
                note.setChanged(now());
                return dataSource.save(note);
            }
        });
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public Trip saveTrip(Context context, final Trip trip)
            throws DataAccessException {

        return new TaskRunner<Trip>().run(context, new AbstractDatasourceTask<Trip>("saveTrip") {

            @Override
            public Trip doWithDatasource(TripDataSource dataSource) {
                trip.setChanged(now());
                return dataSource.save(trip);
            }
        });
    }

    public List<Note> getNotes(Context context, final Trip trip)
            throws DataAccessException {
        return new TaskRunner<List<Note>>().run(context,
                new AbstractDatasourceTask<List<Note>>("getNotes") {

                    @Override
                    public List<Note> doWithDatasource(TripDataSource dataSource) {
                        return dataSource.getNotes(trip);
                    }
                });
    }

    public List<Trip> getTrips(Context context) throws DataAccessException {
        return new TaskRunner<List<Trip>>().run(context,
                new AbstractDatasourceTask<List<Trip>>("getTrips") {

                    @Override
                    public List<Trip> doWithDatasource(TripDataSource dataSource) {
                        return dataSource.getTrips();
                    }
                });
    }

    public Note assignBitmap(Context context, final Note note,
                             final Bitmap bitmap) throws DataAccessException {
        return new TaskRunner<Note>().run(context, new AbstractDatasourceTask<Note>("assignBitmap") {

            @Override
            public Note doWithDatasource(TripDataSource dataSource) {
                new MediaStorage().saveNoteImage(note, bitmap);
                note.setWithImage(true);
                note.setImageChanged(now());
                note.setChanged(now());

                final Trip trip = dataSource.getTrip(note.getTripId());
                trip.setChanged(now());
                dataSource.save(trip);

                return dataSource.save(note);
            }
        });
    }

    /**
     * Find a random image from a given trip
     *
     * @param context
     * @param trip
     * @return
     * @throws de.codekenner.roadtrip.storage.DataAccessException
     */
    private Bitmap findAnyImage(Context context, final Trip trip)
            throws DataAccessException {
        return new TaskRunner<Bitmap>().run(context,
                new AbstractDatasourceTask<Bitmap>("findAnyImage") {

                    @Override
                    public Bitmap doWithDatasource(TripDataSource dataSource) {

                        final Note noteWithImage = dataSource
                                .findNoteWithImage(trip);
                        return (noteWithImage != null) ? new MediaStorage()
                                .loadNoteImage(noteWithImage) : null;
                    }
                });
    }

    public Bitmap getImage(Context context, final Trip trip)
            throws DataAccessException {
        return new TaskRunner<Bitmap>().run(context,
                new AbstractDatasourceTask<Bitmap>("getImage") {

                    @Override
                    public Bitmap doWithDatasource(TripDataSource dataSource) {
                        final Note noteWithImage = dataSource.findNoteWithImage(trip);
                        return (noteWithImage == null) ? null : new MediaStorage()
                                .loadNoteImage(noteWithImage);
                    }
                });
    }

}
