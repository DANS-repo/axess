package nl.knaw.dans.repo.axess;


import com.healthmarketscience.jackcess.Database;
import nl.knaw.dans.repo.axess.core.FilenameComposer;
import nl.knaw.dans.repo.axess.core.KeyTypeValueMatrix;
import nl.knaw.dans.repo.axess.impl.SimpleFilenameComposer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class MetadataWriter {

    private final MetadataExtractor extractor;
    private String rootDirectory;
    private FilenameComposer filenameComposer;

    public MetadataWriter() {
        extractor = new MetadataExtractor();
    }

    public String getRootDirectory() {
        if (rootDirectory == null || "".equals(rootDirectory)) {
            rootDirectory = "root";
        }
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public FilenameComposer getFilenameComposer() {
        if (filenameComposer == null) {
            filenameComposer = new SimpleFilenameComposer();
        }
        return filenameComposer;
    }

    public void setFilenameComposer(FilenameComposer filenameComposer) {
        this.filenameComposer = filenameComposer;
    }

    /**
     * Writes metadata of the given database in vertical orientation with {@link CSVFormat#RFC4180} and header
     * <code>"Key", "Type", "Value"</code> as a <code>n x 3</code> <code>.csv</code> file.
     *
     * @param db the database
     * @throws IOException signals a failure in reading or writing
     */
    public void writeDatabaseMetadata(Database db) throws IOException {
        writeDatabaseMetadata(db, null, true);
    }

    /**
     * Writes metadata of the given database in vertical orientation with the given {@link CSVFormat} as a
     * <code>n x 3</code> <code>.csv</code> file.
     * <p>
     * If <code>format == null</code> the {@link CSVFormat#RFC4180} will be used.
     * </p><p>
     * If <code>format == null</code> or the given format has no header with length 3, the header
     * <code>"Key", "Type", "Value"</code> will be used.
     * </p>
     *
     * @param db     the database
     * @param format the format for the .csv file
     * @throws IOException signals a failure in reading or writing
     */
    public void writeDatabaseMetadata(Database db, CSVFormat format, boolean extended) throws IOException {
        String filename = buildPaths(getFilenameComposer().getDabaseMetadataFilename(db));
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), Charset.forName("UTF-8"));
        extractor.getMetadata(db, extended).printVertical(osw, buildVerticalFormat(format));
    }

    private String buildPaths(String basename) {
        String filename = FilenameUtils.concat(getRootDirectory(), basename);
        File directory = new File(filename).getParentFile();
        assert directory.exists() || directory.mkdirs();
        return filename;
    }

    private CSVFormat buildVerticalFormat(CSVFormat format) {
        if (format == null) {
            format = CSVFormat.RFC4180;
        }
        if (format.getHeader() == null || format.getHeader().length != 3) {
            format = format.withHeader("Obj", "Key", "Type", "Value");
        }
        return format;
    }

    private CSVFormat buildHorizontalFormat(CSVFormat format) {
        if (format == null) {
            format = CSVFormat.RFC4180;
        }
        return format.withFirstRecordAsHeader();
    }

    private CSVFormat buildFormat(CSVFormat format, KeyTypeValueMatrix.Orientation orientation) {
        if (KeyTypeValueMatrix.Orientation.VERTICAL == orientation) {
            return buildVerticalFormat(format);
        } else {
            return buildHorizontalFormat(format);
        }
    }
}
