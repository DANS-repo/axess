package nl.knaw.dans.repo.axxess.ac;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.AxxessTest;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.Charset;

public class TableDataExtractorTest extends AxxessTest {

    @Test
    public void extractTableData() throws Exception {
        File dbFile = getFile("CLIWOC21_97.mdb");
        Database db = null;
        //String rootDir = "target/test-output/metadata-writer";
        try {
            db = DatabaseBuilder.open(dbFile);
            db.setCharset(Charset.forName("ISO8859-1"));
            Table table = db.getTable(db.getTableNames().iterator().next());
            Appendable out = System.out;
            CSVFormat format = CSVFormat.RFC4180;
            TableDataExtractor tde = new TableDataExtractor();
            tde.getTableData(table, out, format);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
