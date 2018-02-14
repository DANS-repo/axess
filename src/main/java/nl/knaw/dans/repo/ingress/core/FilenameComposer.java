package nl.knaw.dans.repo.ingress.core;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public interface FilenameComposer {

  String getDabaseMetadataFilename(Database db);

  String getTableMetadataFilename(Table table);
}
