package nl.knaw.dans.repo.axxess.acc2csv;


import nl.knaw.dans.repo.axxess.core.FilenameComposer;
import nl.knaw.dans.repo.axxess.impl.SimpleFilenameComposer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public abstract class AbstractWriter {


    private String targetDirectory;
    private FilenameComposer filenameComposer;

    public String getTargetDirectory() {
        if (targetDirectory == null || "".equals(targetDirectory)) {
            targetDirectory = "root";
        }
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        setTargetDirectory(targetDirectory.getAbsolutePath());
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
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

    protected String buildPaths(String basename) {
        String filename = FilenameUtils.concat(getTargetDirectory(), basename);
        File directory = new File(filename).getParentFile();
        assert directory.exists() || directory.mkdirs();
        return filename;
    }

    protected CSVFormat getCsvFormat(CSVFormat format) {
        if (format == null) {
            format = CSVFormat.RFC4180;
        }
        return format;
    }
}
