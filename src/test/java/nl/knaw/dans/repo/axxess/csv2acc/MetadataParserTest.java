package nl.knaw.dans.repo.axxess.csv2acc;

import nl.knaw.dans.repo.axxess.core.Axxess;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XColumn;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XDatabase;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataParserTest {

    @Test
    void parse() throws Exception {
        String filename = "src/test/resources/integration/kohier/acc2csv/files/KOHIER1748.accdb._metadata.csv";
        XDatabase xdb = MetadataParser.parse(filename);

        for (XTable xt : xdb.getTables()) {
            String tableName = xt.getString(Axxess.TABLE_NAME);
            System.out.println("Table: " + tableName + " columns: " + xt.getColumns().size());
            for (XColumn xc : xt.getColumns()) {
                String columnName = xc.getString(Axxess.C_NAME);
                System.out.println("\tColumn: " + columnName);
            }
        }
    }
}
