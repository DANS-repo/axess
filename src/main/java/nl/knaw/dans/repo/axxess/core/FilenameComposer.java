package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public interface FilenameComposer {

    String getDabaseMetadataFilename(Database db);

    String getTableDataFilename(Table table);

    String getArchiveFilename(Database db);
}
