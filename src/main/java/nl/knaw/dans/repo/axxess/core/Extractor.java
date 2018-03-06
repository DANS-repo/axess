package nl.knaw.dans.repo.axxess.core;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Extractor} extracts (meta)data from an access database.
 *
 * @param <T> the implementing class
 */
public abstract class Extractor<T extends Extractor> implements Codex.Listener {

    private static Logger LOG = LoggerFactory.getLogger(Extractor.class);

    private ExtractorDef extractorDef = new ExtractorDef();

    private List<Throwable> errorList = new ArrayList<>();
    private List<Throwable> warningList = new ArrayList<>();

    /**
     * Convenience call for getting all settings.
     *
     * @return all settings
     */
    public ExtractorDef getExtractorDef() {
        return extractorDef;
    }

    /**
     * Convenience call for setting all settings.
     *
     * @param extractorDef all settings
     */
    public void setExtractorDef(ExtractorDef extractorDef) {
        this.extractorDef = extractorDef;
    }

    /**
     * Get the default output directory.
     *
     * @return default output directory
     */
    public String getDefaultOutputDirectory() {
        return "axxess-out";
    }

    /**
     * Set the target directory for outputting result files.
     *
     * @param targetDirectory directory for outputting result files
     * @return this for chaining method calls
     * @throws IOException for file system errors
     */
    public T withTargetDirectory(String targetDirectory) throws IOException {
        return withTargetDirectory(new File(targetDirectory));
    }

    /**
     * Set the target directory for outputting result files.
     *
     * @param targetDirectory directory for outputting result files
     * @return this for chaining method calls
     * @throws IOException for file system errors
     */
    @SuppressWarnings("unchecked")
    public T withTargetDirectory(File targetDirectory) throws IOException {
        extractorDef.setTargetDirectory(targetDirectory);
        return (T) this;
    }

    /**
     * Get the directory for outputting result files.
     *
     * @return directory for outputting result files
     */
    public File getTargetDirectory() {
        return extractorDef.getTargetDirectory(getDefaultOutputDirectory());
    }

    /**
     * Set the {@link FilenameComposer} to use. A filename composer determines the names of converted files.
     * Default is {@link nl.knaw.dans.repo.axxess.impl.SimpleFilenameComposer}.
     *
     * @param filenameComposer composer to use
     * @return this for chaining method calls
     */
    @SuppressWarnings("unchecked")
    public T withFilenameComposer(FilenameComposer filenameComposer) {
        extractorDef.setFilenameComposer(filenameComposer);
        return (T) this;
    }

    /**
     * Get the {@link FilenameComposer} in use.
     *
     * @return {@link FilenameComposer} in use
     */
    public FilenameComposer getFilenameComposer() {
        return extractorDef.getFilenameComposer();
    }

    /**
     * Set the encoding of the result files to the given character set.
     * Default output encoding is <code>UTF-8</code>.
     * See also: <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">encoding
     * .doc</a>
     *
     * @param charsetName the name of the character set for output encoding
     * @return this for chaining method calls
     */
    @SuppressWarnings("unchecked")
    public T withTargetEncoding(String charsetName) {
        if (charsetName == null || charsetName.isEmpty()) {
            return (T) this;
        } else {
            return withTargetEncoding(Charset.forName(charsetName));
        }
    }

    /**
     * Set the encoding of the result files to the given character set.
     * Default output encoding is <code>UTF-8</code>.
     * See also: <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">encoding
     * .doc</a>
     *
     * @param charset the character set for output encoding
     * @return this for chaining method calls
     */
    @SuppressWarnings("unchecked")
    public T withTargetEncoding(Charset charset) {
        extractorDef.setTargetCharset(charset);
        return (T) this;
    }

    /**
     * Get the character set for output encoding in use.
     *
     * @return character set for output encoding in use
     */
    public Charset getTargetCharset() {
        return extractorDef.getTargetCharset();
    }

    /**
     * Use the given {@link Codex} for translation of values.
     * Default is {@link DefaultCodex}.
     *
     * @param codex {@link Codex} to use
     * @return this for chaining method calls
     */
    @SuppressWarnings("unchecked")
    public T withCodex(Codex codex) {
        extractorDef.setCodex(codex);
        return (T) this;
    }

    /**
     * Get the {@link Codex} in use.
     *
     * @return {@link Codex} in use
     */
    public Codex getCodex() {
        return extractorDef.getCodex(this);
    }

    /**
     * Use the given {@link CSVFormat} for output csv files.
     * Default is {@link CSVFormat#RFC4180}.
     *
     * @param formatName name of the {@link CSVFormat}
     * @return this for chaining method calls
     */
    @SuppressWarnings("unchecked")
    public T withCSVFormat(String formatName) {
        if (formatName == null || formatName.isEmpty()) {
            return (T) this;
        } else {
            return withCSVFormat(CSVFormat.valueOf(formatName));
        }
    }

    /**
     * Use the given {@link CSVFormat} for output csv files.
     * Default is {@link CSVFormat#RFC4180}.
     *
     * @param csvFormat {@link CSVFormat}
     * @return this for chaining method calls
     */
    @SuppressWarnings("unchecked")
    public T withCSVFormat(CSVFormat csvFormat) {
        extractorDef.setCsvFormat(csvFormat);
        return (T) this;
    }

    /**
     * Get the {@link CSVFormat} in use.
     *
     * @return {@link CSVFormat} in use
     */
    public CSVFormat getCSVFormat() {
        return extractorDef.getCsvFormat();
    }

    /**
     * Get the number of errors during conversion.
     *
     * @return number of errors during conversion
     */
    public int getErrorCount() {
        return errorList.size();
    }

    /**
     * Get the number of warnings during conversion.
     *
     * @return number of warnings during conversion
     */
    public int getWarningCount() {
        return warningList.size();
    }

    /**
     * List errors during conversion.
     *
     * @return errors during conversion
     */
    public List<Throwable> getErrorList() {
        return errorList;
    }

    /**
     * List warnings during conversion.
     *
     * @return warnings during conversion
     */
    public List<Throwable> getWarningList() {
        return warningList;
    }

    /**
     * Not for public use.
     *
     * @param message warning message
     * @param cause   warning cause. may be <code>null</code>
     */
    @Override
    public void reportWarning(String message, Throwable cause) {
        message = message + ", @" + Thread.currentThread().getStackTrace()[2];
        warningList.add(new Throwable(message, cause));
    }

    /**
     * Not for public use.
     *
     * @param message error message
     * @param cause   error cause. may be <code>null</code>
     */
    @Override
    public void reportError(String message, Throwable cause) {
        message = message + ", @" + Thread.currentThread().getStackTrace()[2];
        errorList.add(new Throwable(message, cause));
    }

    protected void reset() {
        errorList.clear();
        warningList.clear();
    }

    protected File buildPaths(String dirName, String filename) {
        File file = FileUtils.getFile(getTargetDirectory().getAbsolutePath(), dirName, filename);
        File directory = file.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return file;
    }

}
