package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.acc2csv.FilenameComposer;

import java.io.IOException;

/**
 * Created on 2018-02-13 13:46.
 */
public class SimpleFilenameComposer implements FilenameComposer {

    public String getDabaseMetadataFilename(Database db) throws IOException {
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
}
