package de.codekenner.roadtrip.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import de.codekenner.roadtrip.domain.Note;

public class MediaStorage {

	private static final int IMAGE_MAX_SIZE = 500;

	public void saveNoteImage(Note note, Bitmap bitmap) {
		final File f = getNoteImageFile(note.getUid());
		if (f.exists()) {
			f.delete();
		}

		OutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			final Bitmap resizedBitmap = resizeImage(bitmap, IMAGE_MAX_SIZE,
					IMAGE_MAX_SIZE);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
			fOut.flush();
		} catch (IOException e) {
		} finally {
			if (fOut != null) {

				try {
					fOut.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	private Bitmap resizeImage(Bitmap source, int maxHeight, int maxWidth) {

		final int width = source.getWidth();
		float scaleWidth = ((float) maxWidth) / width;
		final int height = source.getHeight();
		float scaleHeight = ((float) maxHeight) / height;

		float scale = Math.min(1f, Math.max(scaleWidth, scaleHeight));

		// CREATE A MATRIX FOR THE MANIPULATION

		Matrix matrix = new Matrix();

		// RESIZE THE BIT MAP
		matrix.postScale(scale, scale);

		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height,
				matrix, false);

		return resizedBitmap;

	}

	public Bitmap loadNoteImage(Note note) {
		return loadNoteImage(note.getUid());
	}

    public Bitmap loadNoteImage(String uid) {
        final File f = getNoteImageFile(uid);
        if (f.exists()) {
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        }
        return null;
    }

    public void deleteNoteImage(Note note) {
        final File f = getNoteImageFile(note.getUid());
        if (f.exists()) {
            f.delete();
        }
    }
	/**
	 * Create a file Uri for saving an image or video
	 * 
	 * @param type
	 * @return
	 */
	private static File getNoteImageFile(String uid) {
		final File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"roadtrip");

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				throw new RuntimeException("Das Verzeichnis " + mediaStorageDir
						+ " konnte nicht erstellt werden.");
			}
		}
		return new File(mediaStorageDir, uid + ".jpg");

	}
}
