package nl.knaw.dans.repo.axxess.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface Archiver {

    File archive(List<File> files, boolean compress, File target) throws FileNotFoundException, IOException;
}
