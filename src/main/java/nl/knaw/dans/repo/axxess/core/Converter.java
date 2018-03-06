package nl.knaw.dans.repo.axxess.core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A {@link Converter} converts files.
 *
 * @param <T> the implementing class
 */
public abstract class Converter<T extends Converter> extends Extractor<T> {

    private boolean addManifest;

    private int dbCount;

    /**
     * Convert the given file or files in the given directory. In case of access to csv conversion
     * will look for files with extension <code>.mdb</code> or <code>.accdb</code>. In case of csv
     * to access conversion will look for files with pattern <code>*._metadata.csv</code> and accompanying
     * csv files with table data.
     *
     * @param filename name of file or directory to convert
     * @return list with result files
     * @throws AxxessException for unrecoverable errors
     * @see #getDatabaseCount()
     * @see Extractor#getErrorCount()
     * @see Extractor#getWarningCount()
     * @see Extractor#getErrorList()
     * @see Extractor#getWarningList()
     */
    public List<File> convert(String filename) throws AxxessException {
        return convert(new File(filename));
    }

    /**
     * Convert the given file or files in the given directory. In case of access to csv conversion
     * will look for files with extension <code>.mdb</code> or <code>.accdb</code>. In case of csv
     * to access conversion will look for files with pattern <code>*._metadata.csv</code> and accompanying
     * csv files with table data.
     *
     * @param file file or directory to convert
     * @return list with result files
     * @throws AxxessException for unrecoverable errors
     * @see #getDatabaseCount()
     * @see Extractor#getErrorCount()
     * @see Extractor#getWarningCount()
     * @see Extractor#getErrorList()
     * @see Extractor#getWarningList()
     */
    public abstract List<File> convert(File file) throws AxxessException;

    /**
     * Create and include a <code>manifest-sha1.txt</code> file with checksums of newly created files.
     * Default <code>false</code>.
     *
     * @param addManifest <code>true</code> for including a manifest
     * @return this for chaining method calls
     * @see #isIncludingManifest()
     */
    @SuppressWarnings("unchecked")
    public T setIncludeManifest(boolean addManifest) {
        this.addManifest = addManifest;
        return (T) this;
    }

    /**
     * Is this converter including a manifest.
     *
     * @return <code>true</code> if a <code>manifest-sha1.txt</code> is included, <code>false</code> otherwise.
     * @see #setIncludeManifest(boolean)
     */
    public boolean isIncludingManifest() {
        return addManifest;
    }

    /**
     * Get the number of converted databases.
     *
     * @return the number of converted databases
     */
    public int getDatabaseCount() {
        return dbCount;
    }

    protected void reset() {
        super.reset();
        dbCount = 0;
    }

    protected void increaseDbCount() {
        dbCount++;
    }

    protected void addManifest(List<File> files) throws IOException {
        if (files.isEmpty()) {
            return;
        }
        File directory = files.get(0).getParentFile();
        File manifest = new File(directory, "manifest-sha1.txt");
        PrintWriter out = null;
        try {
            out = new PrintWriter(manifest, "UTF-8");
            for (File file : files) {
                out.println(String.format("%s %s", file.getName(), computeSHA1(file)));
            }
            files.add(manifest);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private String computeSHA1(File file) throws IOException {
        BufferedInputStream buff = null;
        try {
            buff = new BufferedInputStream(new FileInputStream(file));
            return DigestUtils.sha1Hex(buff);
        } finally {
            if (buff != null) {
                buff.close();
            }
        }
    }
}
