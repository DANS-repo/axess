package nl.knaw.dans.repo.axess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axess.core.FilenameComposer;

/**
 * Created on 2018-02-13 13:46.
 */
public class SimpleFilenameComposer implements FilenameComposer {

    public String getDabaseMetadataFilename(Database db) {
        String basename = db.getFile().getName();
        return String.format("%s.database_meta.csv", basename);
    }

    @Override
    public String getTableMetadataFilename(Table table) {
        String basename = table.getDatabase().getFile().getName();
        return String.format("%s.%s.table_meta.csv", basename, table.getName());
    }
}
