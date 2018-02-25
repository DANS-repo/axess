package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.Axxess;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableDataExtractor {

    private static Logger LOG = LoggerFactory.getLogger(TableDataExtractor.class);


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
                cells.add(Axxess.encode(column.getType(), row.get(column.getName())));
            }
            printer.printRecord(cells);
        }
        printer.close();
        return rowCount;
    }
}
