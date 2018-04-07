package nl.knaw.dans.repo.axxess.core;

import java.io.File;

public interface ErrorListener {

    void reportWarning(File file, String message, Throwable cause);

    void reportError(File file, String message, Throwable cause);
}
