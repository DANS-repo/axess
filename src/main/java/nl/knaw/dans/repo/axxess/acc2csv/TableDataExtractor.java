package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableDataExtractor {

    private static final String[] textDataArray = {"TEXT", "GUID", "MEMO"};
    private static final List<String> textDataTypes = Arrays.asList(textDataArray);


    public int getTableData(Table table, Appendable out, CSVFormat format) throws IOException {
        List<? extends Column> columns = table.getColumns();
        List<String> columnNames = columns
          .stream()
          .map(Column::getName)
          .collect(Collectors.toList());
        format.withFirstRecordAsHeader();
        CSVPrinter printer = new CSVPrinter(out, format);
        printer.printRecord(columnNames);

        int rowCount = 0;
        for (Row row : table.newCursor().toCursor()) {
            rowCount++;
            List<Object> cells = new ArrayList<>();
            for (Column column : columns) {
                Object value = row.get(column.getName());
                if (value instanceof String) {
                    value = ((String)value).replaceAll("[\r\n]", "\u0000");
                }
                cells.add(value);
            }
            printer.printRecord(cells);
        }
        printer.close();
        return rowCount;
    }
}
