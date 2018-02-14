package nl.knaw.dans.repo.axess.core;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public interface FilenameComposer {

    String getDabaseMetadataFilename(Database db);

    String getTableMetadataFilename(Table table);
}
