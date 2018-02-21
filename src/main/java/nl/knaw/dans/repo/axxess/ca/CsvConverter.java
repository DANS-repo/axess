package nl.knaw.dans.repo.axxess.ca;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;
import com.healthmarketscience.jackcess.util.ImportUtil;
import nl.knaw.dans.repo.axxess.core.KTV;
import nl.knaw.dans.repo.axxess.core.MetadataParser;
import nl.knaw.dans.repo.axxess.core.XColumn;
import nl.knaw.dans.repo.axxess.core.XDatabase;
import nl.knaw.dans.repo.axxess.core.XTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class CsvConverter {

    public void builtDatabase() throws IOException {
        File file = new File("target/test-output/out/test.accdb");
        File dir = file.getParentFile();
        assert dir.exists() || dir.mkdirs();
        Database db = DatabaseBuilder.create(Database.FileFormat.V2010, file);
        String metadata = "src/test/test-set/csv/CLIWOC21_97.mdb.__metadata.csv";
        XDatabase xdb = MetadataParser.parse(metadata);
        for (XTable xt : xdb.getTables()) {
            String tableName = xt.getString("Table name");
            TableBuilder tableBuilder = new TableBuilder(tableName);
            for (XColumn xc : xt.getColumns()) {
                String columnName = xc.getString("Column name");
                ColumnBuilder columnBuilder = new ColumnBuilder(columnName)
                  .setType(DataType.valueOf(xc.getString("Data type")))
                  .setLength(xc.getInt("Length"))
                  .setAutoNumber(xc.getBool("IsAutoNumber"))
                  .setCompressedUnicode(xc.getBool("IsCompressedUnicode"))
                  .setCalculated(xc.getBool("IsCalculated"))
                  .setHyperlink(xc.getBool("IsHyperlink"));
                for (KTV ktv : xc.getRealProperties("(Property)")) {
                    columnBuilder.putProperty(ktv.getKey(), ktv.getType(), ktv.getValue());
                }
                tableBuilder.addColumn(columnBuilder);
            }
            Table table = tableBuilder.toTable(db);
            if (tableName.equals("CLIWOC21")) {
                String table1 =
                  "target/test-output/test-set/axxes/admiraal/foo/bar/cliwoc/CLIWOC21_97.mdb.CLIWOC21.csv";
                InputStreamReader reader = new InputStreamReader(new FileInputStream(table1), Charset.forName("UTF-8"));
                CSVParser parser = new CSVParser(reader, CSVFormat.RFC4180);
                for (CSVRecord record : parser) {

                    //convert row to objects according to datatype
                    table.addRow(record);
                }
            }
        }
        db.close();

    }
}
