package nl.knaw.dans.repo.axxess.acc2csv;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.Converter;
import nl.knaw.dans.repo.axxess.impl.SimpleEncodingDetector;
import nl.knaw.dans.repo.axxess.impl.StaticEncodingDetector;
import nl.knaw.dans.repo.axxess.impl.ZipArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Converts ms access databases to csv files.
 */
public class Axxess2CsvConverter extends Converter<Axxess2CsvConverter> {

    /**
     * Default location for output.
     */
    public static final String DEFAULT_OUTPUT_DIRECTORY = "work/axxess-csv-out";

    private static Logger LOG = LoggerFactory.getLogger(Axxess2CsvConverter.class);

    private final MetadataExtractor metadataWriter;
    private final TableDataExtractor tableDataWriter;

    private EncodingDetector encodingDetector;
    private boolean extractMetadata = true;
    private Archiver archiver;
    private boolean archiveResults;
    private boolean compressArchive;

    /**
     * Constructs a new {@link Axxess2CsvConverter}.
     */
    public Axxess2CsvConverter() {
        metadataWriter = new MetadataExtractor();
        tableDataWriter = new TableDataExtractor();
    }

    /**
     * Use the given {@link EncodingDetector} for detection of the encoding of the source database(s).
     * With a <code>null</code> parameter will reset encoding detection to default.
     * Default is {@link SimpleEncodingDetector}.
     *
     * @param detector {@link EncodingDetector} to use
     * @return this for chaining method calls
     */
    public Axxess2CsvConverter withEncodingDetector(EncodingDetector detector) {
        this.encodingDetector = detector;
        return this;
    }

    /**
     * Force reading databases with the given character set (encoding).
     * Encoding of access database files after 2000 is (most likely) UTF-16LE. access '97 (V1997) encoding is not
     * properly detected so Axxess enforces ISO8859-1. If you think your database has another encoding you can set
     * the character set.
     * See also: <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">encoding
     * .doc</a>
     *
     * @param charsetName canonical Name for java.nio API
     * @return this for chaining method calls
     */
    public Axxess2CsvConverter withForceSourceEncoding(String charsetName) {
        if (charsetName == null || charsetName.isEmpty()) {
            return withEncodingDetector(null);
        } else {
            return withEncodingDetector(new StaticEncodingDetector(charsetName));
        }
    }

    /**
     * Extract metadata about database, relations, queries, tables, indexes and columns.
     * Default is <code>true</code>. If Access files are damaged or corrupt try with this
     * option set to <code>false</code>. It may well be that table data can still be rescued and converted
     * to csv.
     *
     * @param extractMetadata <code>true</code> for extracting metadata, <code>false</code> to skip this step
     * @return this for chaining method calls
     */
    public Axxess2CsvConverter setExtractMetadata(boolean extractMetadata) {
        this.extractMetadata = extractMetadata;
        return this;
    }

    /**
     * Zip or archive the result files in one file per database converted.
     * Default <code>false</code>.
     *
     * @param archiveResults <code>true</code> for archiving (zipping).
     * @return this for chaining method calls
     * @see #withArchiver(Archiver)
     * @see #setCompressArchive(boolean)
     */
    public Axxess2CsvConverter setArchiveResults(boolean archiveResults) {
        this.archiveResults = archiveResults;
        return this;
    }

    /**
     * If {@link #setArchiveResults(boolean)} is set to <code>true</code> determines if archived files will be
     * compressed.
     * Default <code>false</code>.
     *
     * @param compressArchive <code>true</code> if archived files should be compressed.
     * @return this for chaining method calls
     * @see #setArchiveResults(boolean)
     * @see #withArchiver(Archiver)
     */
    public Axxess2CsvConverter setCompressArchive(boolean compressArchive) {
        this.compressArchive = compressArchive;
        return this;
    }

    /**
     * Use the given {@link Archiver} when archiving result files.
     * Default archiver is {@link ZipArchiver}.
     *
     * @param archiver {@link Archiver} to use
     * @return this for chaining method calls
     * @see #setArchiveResults(boolean)
     * @see #setCompressArchive(boolean)
     */
    public Axxess2CsvConverter withArchiver(Archiver archiver) {
        this.archiver = archiver;
        return this;
    }

    @Override
    public String getDefaultOutputDirectory() {
        return DEFAULT_OUTPUT_DIRECTORY;
    }

    @Override
    public List<File> convert(File file) throws AxxessException {
        reset();
        List<File> resultFiles = new ArrayList<>();
        try {
            convert(file.getAbsoluteFile(), getTargetDirectory(), resultFiles, false);
        } catch (IOException e) {
            throw new AxxessException("Exception during conversion of " + file.getAbsolutePath(), e);
        }
        return resultFiles;
    }

    private void convert(File file, File targetDirectory, List<File> resultFiles, boolean updateTarget)
      throws IOException, AxxessException {
        if (!file.exists()) {
            LOG.warn("File not found: {}", file);
            return;
        }
        if (file.isDirectory()) {
            if (updateTarget) {
                targetDirectory = new File(targetDirectory, file.getName());
            }
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                convert(f, targetDirectory, resultFiles, true);
            }
        } else if (isAccessFile(file)) {
            try {
                resultFiles.addAll(doConvert(file, targetDirectory));
            } catch (Exception e) {
                LOG.error("While converting: " + file.getAbsolutePath(), e);
                reportError("File: " + file.getAbsolutePath(), e);
                if (e instanceof AxxessException) {
                    throw e;
                }
            }
        } else {
            LOG.debug("File is not an access file: {}", file);
        }
    }

    private List<File> doConvert(File file, File targetDirectory) throws IOException, AxxessException {
        List<File> resultFiles = new ArrayList<>();
        List<File> csvFiles = new ArrayList<>();
        LOG.info("Trying to convert {}", file.getAbsolutePath());
        Database db = null;
        try {
            db = DatabaseBuilder.open(file);
            Optional<Charset> maybeCharset = getEncoding(db);
            if (maybeCharset.isPresent()) {
                LOG.info("Setting encoding to '{}' for '{}'", maybeCharset.get(), db.getFile());
                db.setCharset(maybeCharset.get());
            }
            if (extractMetadata) {
                metadataWriter.setExtractorDef(getExtractorDef().copy());
                metadataWriter.withTargetDirectory(targetDirectory);
                File mdFile = metadataWriter.writeDatabaseMetadata(db);
                csvFiles.add(mdFile);
            }
            tableDataWriter.setExtractorDef(getExtractorDef().copy());
            tableDataWriter.withTargetDirectory(targetDirectory);
            List<File> tableFiles = tableDataWriter.writeDatabaseData(db);
            csvFiles.addAll(tableFiles);
            LOG.info("Converted {} to {}", file.getName(), targetDirectory.getAbsolutePath());

            if (isIncludingManifest()) {
                addManifest(csvFiles);
            }

            if (archiveResults) {
                File targetFile =
                  new File(targetDirectory, getFilenameComposer().getArchiveFilename(db));
                File archived = getArchiver().archive(csvFiles, compressArchive, targetFile);
                LOG.info("Archived {} to {}", file.getName(), archived.getAbsolutePath());
                resultFiles.add(archived);
            } else {
                resultFiles = csvFiles;
            }
            increaseDbCount();
            return resultFiles;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private Optional<Charset> getEncoding(Database db) throws IOException {
        if (encodingDetector == null) {
            encodingDetector = new SimpleEncodingDetector();
            LOG.debug("Using EncodingDetector {}", encodingDetector);
        }
        return encodingDetector.detectEncoding(db);
    }

    private boolean isAccessFile(File file) {
        for (Database.FileFormat fm : Database.FileFormat.values()) {
            if (file.getName().toLowerCase().endsWith(fm.getFileExtension())) {
                return true;
            }
        }
        return false;
    }

    private Archiver getArchiver() {
        if (archiver == null) {
            archiver = new ZipArchiver();
        }
        return archiver;
    }


}
