/**
 * 
 */
package de.codekenner.roadtrip.storage;

/**
 * @author markus
 * 
 */
public class DataAccessException extends Exception {

	/**
	 * 
	 */
	public DataAccessException() {
	}

	/**
	 * @param detailMessage
	 */
	public DataAccessException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public DataAccessException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public DataAccessException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
