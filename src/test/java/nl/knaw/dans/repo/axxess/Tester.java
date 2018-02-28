package nl.knaw.dans.repo.axxess;


import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

import java.io.File;

public class Tester {

    public static void main(String[] args) throws Exception {
        // BigDecimal bd = new BigDecimal(13.7);
        // bd.setScale(0);

        Database db = DatabaseBuilder.create(Database.FileFormat.V2010, new File("testdb.accdb"));
        TableBuilder tableBuilder = new TableBuilder("Table_01");

        ColumnBuilder primCol = new ColumnBuilder("ID")
          .setType(DataType.LONG)
          .setLength(4)
          .setAutoNumber(true);
        tableBuilder.addColumn(primCol);

        ColumnBuilder columnBuilder = new ColumnBuilder("Number_scale2")
          .setType(DataType.NUMERIC)
          .setLength(17)
          .setScale(2)
          .setPrecision(18)
          .setAutoNumber(false)
          .setHyperlink(false)
          .setCompressedUnicode(false)
          .setCalculated(false);
        tableBuilder.addColumn(columnBuilder);

        Table table = tableBuilder.toTable(db);

        Object[] data1 = {1, 1.00};
        table.addRow(data1);

        Object[] data2 = {1, "1.10"};
        table.addRow(data2);

        db.close();
    }
}
