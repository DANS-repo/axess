package nl.knaw.dans.repo.ingress.impl;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.ingress.MetadataWriter;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

import java.io.File;

public class MetadataWriterTest {

    //private String databaseFile = "../mdb_testset/Admiraal Evertsen_1815.mdb";
    //private String databaseFile = "../mdb_testset/CLIWOC21_97.mdb";
    //private String databaseFile = "../mdb_testset/ingress_test.accdb";
    //private String databaseFile = "../mdb_testset/axess.accdb";
    private String databaseFile = "../mdb_testset/MonthlySalesReports.accdb";

    @Test
    public void writesDatabaseMetadata() throws Exception {
        File dbFile = new File(databaseFile);
        System.out.println(dbFile.getCanonicalPath());
        Database db = null;
        try {
            db = DatabaseBuilder.open(new File(databaseFile));
            MetadataWriter writer = new MetadataWriter();
            writer.setRootDirectory("target/test-output/two");
            writer.writeDatabaseMetadata(db, CSVFormat.DEFAULT.withDelimiter(','), true);

        } finally {
            if (db != null) {
                db.close();
            }
        }

    }


}
