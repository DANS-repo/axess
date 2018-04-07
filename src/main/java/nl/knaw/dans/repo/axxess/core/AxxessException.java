package nl.knaw.dans.repo.axxess.core;

/**
 * Signals an error because of which processing cannot go further.
 */
public class AxxessException extends Exception {

    public AxxessException() {
    }

    public AxxessException(String message) {
        super(message);
    }

    public AxxessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AxxessException(Throwable cause) {
        super(cause);
    }

    public AxxessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
