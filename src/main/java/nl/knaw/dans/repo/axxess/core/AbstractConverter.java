package nl.knaw.dans.repo.axxess.core;

import nl.knaw.dans.repo.axxess.impl.SimpleFilenameComposer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConverter<T extends AbstractConverter> implements Codex.Listener {

    private Codex codex;
    private File targetDirectory;
    private CSVFormat csvFormat;
    private FilenameComposer filenameComposer;
    private boolean addManifest;

    private int dbCount;
    private List<Throwable> errorList = new ArrayList<>();
    private List<Throwable> warningList = new ArrayList<>();

    public abstract String getDefaultOutputDirectory();

    public List<File> convert(String filename) throws AxxessException {
        return convert(new File(filename));
    }

    @SuppressWarnings("unchecked")
    public T withCodex(Codex codex) {
        this.codex = codex;
        return (T) this;
    }

    public abstract List<File> convert(File file) throws AxxessException;

    public T withTargetDirectory(String targetDirectory) throws IOException {
        return withTargetDirectory(new File(targetDirectory));
    }

    @SuppressWarnings("unchecked")
    public T withTargetDirectory(File targetDirectory) throws IOException {
        this.targetDirectory = targetDirectory.getAbsoluteFile();
        assert this.targetDirectory.exists() || this.targetDirectory.mkdirs();
        if (!this.targetDirectory.canWrite()) {
            throw new IOException("Target directory not writable: " + this.targetDirectory.getAbsolutePath());
        }
        return (T) this;
    }

    public File getTargetDirectory() {
        if (targetDirectory == null) {
            targetDirectory = new File(".", getDefaultOutputDirectory()).getAbsoluteFile();
        }
        return targetDirectory;
    }

    @SuppressWarnings("unchecked")
    public T withFilenameComposer(FilenameComposer filenameComposer) {
        this.filenameComposer = filenameComposer;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withCSVFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setIncludeManifest(boolean addManifest) {
        this.addManifest = addManifest;
        return (T) this;
    }

    public int getConvertedDatabaseCount() {
        return dbCount;
    }

    public int getErrorCount() {
        return errorList.size();
    }

    public int getWarningCount() {
        return warningList.size();
    }

    public List<Throwable> getErrorList() {
        return errorList;
    }

    public List<Throwable> getWarningList() {
        return warningList;
    }

    @Override
    public void reportWarning(String message, Throwable cause) {
        message = message + ", @" + Thread.currentThread().getStackTrace()[2];
        warningList.add(new Throwable(message, cause));
    }

    @Override
    public void reportError(String message, Throwable cause) {
        message = message + ", @" + Thread.currentThread().getStackTrace()[2];
        errorList.add(new Throwable(message, cause));
    }

    protected void reset() {
        errorList.clear();
        dbCount = 0;
    }

    protected void addError(Throwable t) {
        errorList.add(t);
    }

    protected void increaseDbCount() {
        dbCount++;
    }

    protected Codex getCodex() {
        if (codex == null) {
            codex = new DefaultCodex(this);
        }
        return codex;
    }

    protected FilenameComposer getFilenameComposer() {
        if (filenameComposer == null) {
            filenameComposer = new SimpleFilenameComposer();
        }
        return filenameComposer;
    }


    protected CSVFormat getCSVFormat() {
        if (csvFormat == null) {
            csvFormat = CSVFormat.RFC4180;
        }
        return csvFormat;
    }

    protected boolean includeManifest() {
        return addManifest;
    }

    protected void addManifest(List<File> files, File targetDirectory) throws IOException {
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
