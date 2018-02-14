package nl.knaw.dans.repo.ingress.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.ingress.MetadataExtractor;
import nl.knaw.dans.repo.ingress.core.KeyTypeValueMatrix;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Created on 2018-02-14 16:35.
 */
public class MetadataExtractorTest {

    private String databaseFile = "../mdb_testset/Admiraal Evertsen_1815.mdb";
    //private String databaseFile = "../mdb_testset/CLIWOC21_97.mdb";
    //private String databaseFile = "../mdb_testset/ingress_test.accdb";
    //private String databaseFile = "../mdb_testset/axess.accdb";

    @Test
    public void GetExtendedTableMetadata() throws Exception {
        Database db = null;
        try {
            db = DatabaseBuilder.open(new File(databaseFile));
            Table table = db.getTable(db.getTableNames().iterator().next());
            MetadataExtractor extractor = new MetadataExtractor();
            KeyTypeValueMatrix matrix = extractor.getExtendedTableMetadata(db);

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
