package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.FilenameComposer;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class SimpleFilenameComposer implements FilenameComposer {

    public String getCsvDirectoryName(File dbFile) {
        return cleanDatabaseName(dbFile).replaceAll("\\.", "_");
    }

    @Override
    public String getCsvDirectoryName(Database db) {
        return getCsvDirectoryName(db.getFile());
    }

    @Override
    public String getCsvDirectoryName(Table table) {
        return getCsvDirectoryName(table.getDatabase().getFile());
    }

    public String getMetadataFilename(File dbFile, String mdName) {
        String basename = cleanDatabaseName(dbFile);
        return String.format("%s.%s.csv", basename, mdName);
    }

    @Override
    public String getMetadataFilename(Database db) throws IOException {
        String mdName = "_metadata";
        while (db.getTableNames().contains(mdName)) {
            mdName = "_" + mdName;
        }
        return getMetadataFilename(db.getFile(), mdName);
    }

    public String getTableDataFilename(File dbFile, String tableName) {
        String basename = cleanDatabaseName(dbFile);
        String rawName = String.format("%s.%s", basename, cleanTableName(tableName));
        return rawName + ".csv";
    }

    @Override
    public String getTableDataFilename(Table table) {
        return getTableDataFilename(table.getDatabase().getFile(), table.getName());
    }

    public String getArchiveFilename(File dbFile) {
        String basename = cleanDatabaseName(dbFile);
        return String.format("%s.csv.zip", basename);
    }

    @Override
    public String getArchiveFilename(Database db) {
        return getArchiveFilename(db.getFile());
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
        return new File(metadataFile.getParent(), String.format("%s.%s.csv", basename, cleanTableName(tableName)));
    }

    private String cleanTableName(String tableName) {
        return tableName.replaceAll("[ \\.]", "_");
    }

    private String cleanDatabaseName(File dbFile) {
        return dbFile.getName().replaceAll(" ", " ");
    }
}
