package nl.knaw.dans.repo.axxess.acc2csv;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class TableDataExtractorTest {

    @Test
    void readTableData() throws Exception {
        String database = "src/test/resources/integration/rhijn/db/Rhijn_1848.mdb";
        Database db = DatabaseBuilder.open(new File(database));

        TableDataExtractor extractor = new TableDataExtractor();
        extractor.writeDatabaseData(db);

        db.close();
    }
}
