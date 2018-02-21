package nl.knaw.dans.repo.axxess.acc2csv;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.impl.SimpleEncodingDetector;
import nl.knaw.dans.repo.axxess.impl.StaticEncodingDetector;
import nl.knaw.dans.repo.axxess.impl.ZipArchiver;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AxxessToCsvConverter {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "axxess-out";

    private static Logger LOG = LoggerFactory.getLogger(AxxessToCsvConverter.class);

    private final MetadataWriter metadataWriter;
    private final TableDataWriter tableDataWriter;

    private EncodingDetector encodingDetector;
    private CSVFormat csvFormat;
    private File targetDirectory;
    private Archiver archiver;
    private boolean archiveResults;
    private boolean compressArchive;
    private boolean addManifest;

    private int dbCount;
    private List<Throwable> errorList = new ArrayList<>();

    public AxxessToCsvConverter() {
        metadataWriter = new MetadataWriter();
        tableDataWriter = new TableDataWriter();
    }

    public AxxessToCsvConverter withTargetDirectory(String targetDirectory) throws IOException {
        return withTargetDirectory(new File(targetDirectory));
    }

    public AxxessToCsvConverter withTargetDirectory(File targetDirectory) throws IOException {
        this.targetDirectory = targetDirectory.getAbsoluteFile();
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

    public AxxessToCsvConverter withFilenameComposer(FilenameComposer filenameComposer) {
        metadataWriter.setFilenameComposer(filenameComposer);
        tableDataWriter.setFilenameComposer(filenameComposer);
        return this;
    }

    public AxxessToCsvConverter withEncodingDetector(EncodingDetector detector) {
        this.encodingDetector = detector;
        return this;
    }

    public AxxessToCsvConverter withForceSourceEncoding(String charsetName) {
        return withEncodingDetector(new StaticEncodingDetector(charsetName));
    }

    public AxxessToCsvConverter withCSVFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return this;
    }

    public AxxessToCsvConverter withArchiveResults(boolean archiveResults) {
        this.archiveResults = archiveResults;
        return this;
    }

    public AxxessToCsvConverter withCompressArchive(boolean compressArchive) {
        this.compressArchive = compressArchive;
        return this;
    }

    public AxxessToCsvConverter withArchiver(Archiver archiver) {
        this.archiver = archiver;
        return this;
    }

    public AxxessToCsvConverter withManifest(boolean addManifest) {
        this.addManifest = addManifest;
        return this;
    }

    public List<File> convert(String filename) throws AxxessException {
        reset();
        List<File> csvFiles = new ArrayList<>();
        convert(new File(filename).getAbsoluteFile(), getTargetDirectory(), csvFiles);
        return csvFiles;
    }

    public List<File> convert(File file) throws AxxessException {
        reset();
        List<File> csvFiles = new ArrayList<>();
        convert(file.getAbsoluteFile(), getTargetDirectory(), csvFiles);
        return csvFiles;
    }

    public int getConvertedDatabaseCount() {
        return dbCount;
    }

    public List<Throwable> getErrorList() {
        return errorList;
    }

    private void reset() {
        errorList.clear();
        dbCount = 0;
    }

    private void convert(File file, File targetDirectory, List<File> convertedFiles) throws AxxessException {
        if (file.isDirectory()) {
            File td = new File(targetDirectory, file.getName());
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                convert(f, td, convertedFiles);
            }
        } else if (isAccessFile(file)) {
            try {
                List<File> csvFiles = doConvert(file, targetDirectory);
                convertedFiles.addAll(csvFiles);
            } catch (IOException e) {
                LOG.error("While converting: " + file.getAbsolutePath(), e);
                errorList.add(e);
            }
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
            metadataWriter.setTargetDirectory(targetDirectory);
            File mdFile = metadataWriter.writeDatabaseMetadata(db, getCSVFormat(), true);
            csvFiles.add(mdFile);
            tableDataWriter.setTargetDirectory(targetDirectory);
            List<File> tableFiles = tableDataWriter.writeDatabaseData(db, getCSVFormat());
            csvFiles.addAll(tableFiles);
            LOG.info("Converted {} to {}", file.getName(), targetDirectory.getAbsolutePath());

            if (addManifest) {
                addManifest(csvFiles, targetDirectory);
            }

            if (archiveResults) {
                File targetFile =
                  new File(targetDirectory, metadataWriter.getFilenameComposer().getArchiveFilename(db));
                File archived = getArchiver().archive(csvFiles, compressArchive, targetFile);
                LOG.info("Archived {} to {}", file.getName(), archived.getAbsolutePath());
                resultFiles.add(archived);
                for (File f : csvFiles) {
                    assert f.delete();
                }
            } else {
                resultFiles = csvFiles;
            }
            dbCount++;
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
            if (file.getName().endsWith(fm.getFileExtension())) {
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

    private void addManifest(List<File> files, File targetDirectory) throws IOException {
        File manifest = new File(targetDirectory, "manifest-sha1.txt");
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
