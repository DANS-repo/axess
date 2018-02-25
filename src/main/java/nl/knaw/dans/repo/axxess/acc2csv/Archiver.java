package nl.knaw.dans.repo.axxess.acc2csv;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Archiver {

    File archive(List<File> files, boolean compress, File target) throws IOException;
}
