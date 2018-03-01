package nl.knaw.dans.repo.axxess.core;

import nl.knaw.dans.repo.axxess.impl.SimpleFilenameComposer;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ExtractorDef {

    private File targetDirectory;
    private Charset targetCharset;
    private FilenameComposer filenameComposer;
    private Codex codex;
    private CSVFormat csvFormat;

    public File getTargetDirectory(String defaultOutput) {
        if (targetDirectory == null) {
            targetDirectory = new File(defaultOutput).getAbsoluteFile();
        }
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) throws IOException {
        this.targetDirectory = targetDirectory.getAbsoluteFile();
        assert this.targetDirectory.exists() || this.targetDirectory.mkdirs();
        if (!this.targetDirectory.canWrite()) {
            throw new IOException("Target directory not writable: " + this.targetDirectory.getAbsolutePath());
        }
    }

    public Charset getTargetCharset() {
        if (targetCharset == null) {
            targetCharset = Charset.forName("UTF-8");
        }
        return targetCharset;
    }

    public void setTargetCharset(Charset targetCharset) {
        this.targetCharset = targetCharset;
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

    public Codex getCodex(Codex.Listener listener) {
        if (codex == null) {
            codex = new DefaultCodex(listener);
        }
        return codex;
    }

    public void setCodex(Codex codex) {
        this.codex = codex;
    }

    public CSVFormat getCsvFormat() {
        if (csvFormat == null) {
            csvFormat = CSVFormat.RFC4180;
        }
        return csvFormat;
    }

    public void setCsvFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
    }
}
