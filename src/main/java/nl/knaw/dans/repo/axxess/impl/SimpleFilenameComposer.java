package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.FilenameComposer;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class SimpleFilenameComposer implements FilenameComposer {

    public String getMetadataFilename(Database db) throws IOException {
        String mdName = "_metadata";
        while (db.getTableNames().contains(mdName)) {
            mdName = "_" + mdName;
        }
        String basename = db.getFile().getName();
        return String.format("%s.%s.csv", basename, mdName);
    }

    @Override
    public String getTableDataFilename(Table table) {
        String basename = table.getDatabase().getFile().getName();
        return String.format("%s.%s.csv", basename, table.getName());
    }

    @Override
    public String getArchiveFilename(Database db) {
        String basename = db.getFile().getName();
        return String.format("%s.csv.zip", basename);
    }

    @Override
    public boolean isMetadataFilename(File file) {
        return Pattern.matches(".*\\.[_]*metadata\\.csv", file.getName());
    }

    @Override
    public String getNewDatabaseFilename(String metadataFilename, String extension) {
        return metadataFilename.replaceAll("\\.[_]*metadata.csv", extension);
    }

    @Override
    public File getTableDataFileFor(File metadataFile, String tableName) {
        if (!isMetadataFilename(metadataFile)) {
            throw new IllegalArgumentException("Not a metadata file: " + metadataFile.getAbsolutePath());
        }
        String basename = metadataFile.getName().replaceAll("\\.[_]*metadata.csv", "");
        return new File(metadataFile.getParent(), String.format("%s.%s.csv", basename, tableName));
    }
}
