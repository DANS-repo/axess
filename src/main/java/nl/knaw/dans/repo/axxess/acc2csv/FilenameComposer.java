package nl.knaw.dans.repo.axxess.acc2csv;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import java.io.IOException;

public interface FilenameComposer {

    String getDabaseMetadataFilename(Database db) throws IOException;

    String getTableDataFilename(Table table);

    String getArchiveFilename(Database db);
}
