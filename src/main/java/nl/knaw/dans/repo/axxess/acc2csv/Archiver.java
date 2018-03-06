package nl.knaw.dans.repo.axxess.acc2csv;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * An {@link Archiver} is capable of zipping or archiving files.
 */
public interface Archiver {

    /**
     * Zip or archive the collection of <code>files</code> to the <code>target</code> file.
     * The {@link File} returned may have another file extension then the original <code>target</code> file.
     *
     * @param files    files to archive
     * @param compress use compression or not
     * @param target   name and location of the target file
     * @return target file, may have another extension
     * @throws IOException for write errors
     */
    File archive(List<File> files, boolean compress, File target) throws IOException;
}
