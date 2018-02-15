package nl.knaw.dans.repo.axxess.core;


import nl.knaw.dans.repo.axxess.impl.SimpleFilenameComposer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public abstract class AbstractWriter {


    private String rootDirectory;
    private FilenameComposer filenameComposer;

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

    protected String buildPaths(String basename) {
        String filename = FilenameUtils.concat(getRootDirectory(), basename);
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
