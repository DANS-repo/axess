package nl.knaw.dans.repo.axxess.core;

/**
 * Signals a minor exception or warning.
 */
public class AxxessCheckedException extends Exception {

    public AxxessCheckedException(String message) {
        super(message);
    }
}
