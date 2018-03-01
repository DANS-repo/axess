package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import java.io.File;
import java.io.IOException;

/**
 * Composes file names.
 */
public interface FilenameComposer {

    /**
     * Get a file name for the metadata file of the given {@link Database}.
     *
     * @param db {@link Database} under scrutiny
     * @return a file name for the metadata file
     * @throws IOException for database read exceptions
     */
    String getMetadataFilename(Database db) throws IOException;

    /**
     * Check if a given file is a metadata file according to this {@link FilenameComposer}.
     *
     * @param file file who's name is being inspected
     * @return <code>true</code> if the given file has a metadata file name, <code>false</code> otherwise
     */
    boolean isMetadataFilename(File file);

    /**
     * Get a file name for the file with extracted data of the given {@link Table}.
     *
     * @param table {@link Table} who's data is being extracted
     * @return a file name for the extracted data
     */
    String getTableDataFilename(Table table);

    /**
     * Get a file name for the archive file.
     *
     * @param db {@link Database} who's data is being archived
     * @return a file name for the archive file
     */
    String getArchiveFilename(Database db);

    /**
     * Get a new database file name in accordance with the given metadata file name and extension.
     *
     * @param metadataFilename file name as issued by {@link #getMetadataFilename(Database)}
     * @param extension        extension for the new database file name
     * @return new database file name
     */
    String getNewDatabaseFilename(String metadataFilename, String extension);

    /**
     * Get the file with table data for the given metadataFile and tableName.
     *
     * @param metadataFile the metadata file
     * @param tableName    the table name
     * @return the file with table data
     */
    File getTableDataFileFor(File metadataFile, String tableName);

}
