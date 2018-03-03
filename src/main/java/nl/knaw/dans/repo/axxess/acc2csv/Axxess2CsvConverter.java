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

public class Axxess2CsvConverter extends Converter<Axxess2CsvConverter> {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "axxess-csv-out";

    private static Logger LOG = LoggerFactory.getLogger(Axxess2CsvConverter.class);

    private final MetadataExtractor metadataWriter;
    private final TableDataExtractor tableDataWriter;

    private EncodingDetector encodingDetector;
    private Archiver archiver;
    private boolean archiveResults;
    private boolean compressArchive;

    public Axxess2CsvConverter() {
        metadataWriter = new MetadataExtractor();
        tableDataWriter = new TableDataExtractor();
    }

    public Axxess2CsvConverter withEncodingDetector(EncodingDetector detector) {
        this.encodingDetector = detector;
        return this;
    }

    public Axxess2CsvConverter withForceSourceEncoding(String charsetName) {
        if (charsetName == null || charsetName.isEmpty()) {
            return withEncodingDetector(null);
        } else {
            return withEncodingDetector(new StaticEncodingDetector(charsetName));
        }
    }

    public Axxess2CsvConverter setArchiveResults(boolean archiveResults) {
        this.archiveResults = archiveResults;
        return this;
    }

    public Axxess2CsvConverter setCompressArchive(boolean compressArchive) {
        this.compressArchive = compressArchive;
        return this;
    }

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
            convert(file.getAbsoluteFile(), getTargetDirectory(), resultFiles);
        } catch (IOException e) {
            throw new AxxessException("Exception during conversion of " + file.getAbsolutePath(), e);
        }
        return resultFiles;
    }

    private void convert(File file, File targetDirectory, List<File> resultFiles) throws IOException, AxxessException {
        if (!file.exists()) {
            LOG.warn("File not found: {}", file);
            return;
        }
        if (file.isDirectory()) {
            File td;
            if (file.getCanonicalPath().equals(targetDirectory.getCanonicalPath())) {
                td = targetDirectory;
            } else {
                td = new File(targetDirectory, file.getName());
            }
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                convert(f, td, resultFiles);
            }
        } else if (isAccessFile(file)) {
            try {
                resultFiles.addAll(doConvert(file, targetDirectory));
            } catch (IOException e) {
                LOG.error("While converting: " + file.getAbsolutePath(), e);
                reportError("File: " + file.getAbsolutePath(), e);
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
            metadataWriter.setExtractorDef(getExtractorDef().copy());
            metadataWriter.withTargetDirectory(targetDirectory);
            File mdFile = metadataWriter.writeDatabaseMetadata(db);
            csvFiles.add(mdFile);
            tableDataWriter.setExtractorDef(getExtractorDef().copy());
            tableDataWriter.withTargetDirectory(targetDirectory);
            List<File> tableFiles = tableDataWriter.writeDatabaseData(db, getCSVFormat(), getCodex());
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
                // for (File f : csvFiles) {
                //     f.delete();
                // }
                // if (csvFiles.size() > 0) {
                //     csvFiles.get(0).getParentFile().delete();
                // }
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
