package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Database;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class MetadataWriter extends AbstractWriter {

    private static Logger LOG = LoggerFactory.getLogger(MetadataWriter.class);

    private final MetadataExtractor extractor;

    public MetadataWriter() {
        extractor = new MetadataExtractor();
    }

    /**
     * Writes metadata of the given database in vertical orientation with {@link CSVFormat#RFC4180} and header
     * <code>"Key", "Type", "Value"</code> as a <code>n x 3</code> <code>.csv</code> file.
     *
     * @param db the database
     * @throws IOException signals a failure in reading or writing
     */
    public void writeDatabaseMetadata(Database db) throws IOException, AxxessException {
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
     * @return the newly created .csv file
     * @throws IOException signals a failure in reading or writing
     */
    public File writeDatabaseMetadata(Database db, CSVFormat format, boolean extended)
      throws IOException, AxxessException {
        String filename = buildPaths(getFilenameComposer().getMetadataFilename(db));
        File file = new File(filename);
        if (file.exists()) {
            throw new AxxessException("File exists: " + file.getAbsolutePath());
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
        extractor.getMetadata(db).printVertical(osw, buildVerticalFormat(format));
        LOG.debug("Wrote metadata: {}", file.getName());
        return file;
    }

    private CSVFormat buildVerticalFormat(CSVFormat format) {
        format = getCsvFormat(format);
        if (format.getHeader() == null || format.getHeader().length != 3) {
            format = format.withHeader("Obj", "Key", "Type", "Value");
        }
        return format;
    }

    private CSVFormat buildHorizontalFormat(CSVFormat format) {
        format = getCsvFormat(format);
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
