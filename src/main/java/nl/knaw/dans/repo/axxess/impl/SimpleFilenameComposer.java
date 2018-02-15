package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.FilenameComposer;

/**
 * Created on 2018-02-13 13:46.
 */
public class SimpleFilenameComposer implements FilenameComposer {

    public String getDabaseMetadataFilename(Database db) {
        String basename = db.getFile().getName();
        return String.format("%s.metadata.csv", basename);
    }

    @Override
    public String getTableMetadataFilename(Table table) {
        String basename = table.getDatabase().getFile().getName();
        return String.format("%s.%s.metadata.csv", basename, table.getName());
    }

    @Override
    public String getTableDataFilename(Table table) {
        String basename = table.getDatabase().getFile().getName();
        return String.format("%s.%s.csv", basename, table.getName());
    }
}
