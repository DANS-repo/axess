package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import java.io.IOException;

public interface FilenameComposer {

    String getMetadataFilename(Database db) throws IOException;

    String getTableDataFilename(Table table);

    String getArchiveFilename(Database db);

    String getMetadataFilenamePattern();

    String getDatabaseFilename(String metadataFilename, String extension);
}
