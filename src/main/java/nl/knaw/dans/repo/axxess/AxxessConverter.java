package nl.knaw.dans.repo.axxess;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.axxess.core.Archiver;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.EncodingDetector;
import nl.knaw.dans.repo.axxess.core.FilenameComposer;
import nl.knaw.dans.repo.axxess.impl.SimpleEncodingDetector;
import nl.knaw.dans.repo.axxess.impl.ZipArchiver;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AxxessConverter {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "axxess-out";

    private static Logger LOG = LoggerFactory.getLogger(AxxessConverter.class);

    private final MetadataWriter metadataWriter;
    private final TableDataWriter tableDataWriter;

    private EncodingDetector encodingDetector;
    private CSVFormat csvFormat;
    private File targetDirectory;
    private Archiver archiver;
    private boolean archiveResults;
    private boolean compressArchive;

    public AxxessConverter() {
        metadataWriter = new MetadataWriter();
        tableDataWriter = new TableDataWriter();
    }

    public AxxessConverter withTargetDirectory(String targetDirectory) throws IOException {
        this.targetDirectory = new File(targetDirectory).getAbsoluteFile();
        assert this.targetDirectory.exists() || this.targetDirectory.mkdirs();
        if (!this.targetDirectory.canWrite()) {
            throw new IOException("Target directory not writable: " + this.targetDirectory.getAbsolutePath());
        }
        return this;
    }

    public File getTargetDirectory() {
        if (targetDirectory == null) {
            targetDirectory = new File(".", DEFAULT_OUTPUT_DIRECTORY).getAbsoluteFile();
        }
        return targetDirectory;
    }

    public AxxessConverter withFilenameComposer(FilenameComposer filenameComposer) {
        metadataWriter.setFilenameComposer(filenameComposer);
        tableDataWriter.setFilenameComposer(filenameComposer);
        return this;
    }

    public AxxessConverter withEncodingDetector(EncodingDetector detector) {
        this.encodingDetector = detector;
        return this;
    }

    public AxxessConverter withCSVFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return this;
    }

    public AxxessConverter withArchiveResults(boolean archiveResults) {
        this.archiveResults = archiveResults;
        return this;
    }

    public AxxessConverter withCompressArchive(boolean compressArchive) {
        this.compressArchive = compressArchive;
        return this;
    }

    public AxxessConverter withArchiver(Archiver archiver) {
        this.archiver = archiver;
        return this;
    }

    public List<File> convert(String filename) throws AxxessException {
        List<File> csvFiles = new ArrayList<>();
        convert(new File(filename).getAbsoluteFile(), getTargetDirectory(), csvFiles);
        return csvFiles;
    }

    public List<File> convert(File file) throws AxxessException {
        List<File> csvFiles = new ArrayList<>();
        convert(file.getAbsoluteFile(), getTargetDirectory(), csvFiles);
        return csvFiles;
    }

    private void convert(File file, File targetDirectory, List<File> convertedFiles) throws AxxessException {
        if (file.isDirectory()) {
            File td = new File(targetDirectory, file.getName());
            File[] files = file.listFiles();
            if (files == null) return;
            for (File f : files) {
                convert(f, td, convertedFiles);
            }
        } else if (isAccessFile(file)) {
            try {
                List<File> csvFiles = doConvert(file, targetDirectory);
                convertedFiles.addAll(csvFiles);
            } catch (IOException e) {
                LOG.error("While converting: " + file.getAbsolutePath(), e);
            }
        }
    }

    private List<File> doConvert(File file, File targetDirectory) throws IOException, AxxessException {
        List<File> resultFiles = new ArrayList<>();
        LOG.info("Trying to convert {}", file.getAbsolutePath());
        Database db = null;
        try {
            db = DatabaseBuilder.open(file);
            Optional<Charset> maybeCharset = getEncoding(db);
            if (maybeCharset.isPresent()) {
                LOG.info("Setting encoding to '{}' for '{}'", maybeCharset.get(), db.getFile());
                db.setCharset(maybeCharset.get());
            }
            metadataWriter.setTargetDirectory(targetDirectory);
            File mdFile = metadataWriter.writeDatabaseMetadata(db, getCSVFormat(), true);
            tableDataWriter.setTargetDirectory(targetDirectory);
            List<File> tableFiles = tableDataWriter.writeDatabaseData(db, getCSVFormat());
            LOG.info("Converted {} to {}", file.getName(), targetDirectory.getAbsolutePath());
            if (archiveResults) {
                tableFiles.add(mdFile);
                File targetFile = new File(targetDirectory, metadataWriter.getFilenameComposer().getArchiveFilename(db));
                File archived = getArchiver().archive(tableFiles, compressArchive, targetFile);
                LOG.info("Archived {} to {}", file.getName(), archived.getAbsolutePath());
                resultFiles.add(archived);
                for (File f : tableFiles) {
                    assert f.delete();
                }
            } else {
                resultFiles.add(mdFile);
                resultFiles.addAll(tableFiles);
            }
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
        }
        return encodingDetector.detectEncoding(db);
    }

    private CSVFormat getCSVFormat() {
        if (csvFormat == null) {
            csvFormat = CSVFormat.RFC4180;
        }
        return csvFormat;
    }

    private boolean isAccessFile(File file) {
        for (Database.FileFormat fm : Database.FileFormat.values()) {
            if (file.getName().endsWith(fm.getFileExtension())) return true;
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
