package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.Codex;
import nl.knaw.dans.repo.axxess.core.Extractor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableDataExtractor extends Extractor<TableDataExtractor> {

    private static Logger LOG = LoggerFactory.getLogger(TableDataExtractor.class);

    public List<File> writeDatabaseData(Database db)
      throws IOException, AxxessException {
        List<File> convertedFiles = new ArrayList<>();
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            convertedFiles.add(writeTableData(table));
        }
        return convertedFiles;
    }

    public File writeTableData(Table table) throws IOException, AxxessException {
        String dirName = getFilenameComposer().getCsvDirectoryName(table);
        String filename = getFilenameComposer().getTableDataFilename(table);
        File file = buildPaths(dirName, filename);
        if (file.exists()) {
            throw new AxxessException("File exists: " + file.getAbsolutePath());
        }
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(file), getTargetCharset());
            int rowCount = getTableData(table, osw);
            LOG.debug("Wrote {} records to {}", rowCount, file.getName());
        } finally {
            if (osw != null) {
                osw.close();
            }
        }
        return file;
    }

    public int getTableData(Table table, Appendable out) throws IOException {
        List<? extends Column> columns = table.getColumns();
        List<String> columnNames = columns
          .stream()
          .map(Column::getName)
          .collect(Collectors.toList());
        CSVPrinter printer = new CSVPrinter(out, getCSVFormat().withFirstRecordAsHeader());
        printer.printRecord(columnNames);

        Codex currentCodex = getCodex();
        int rowCount = 0;
        for (Row row : table.newCursor().toCursor()) {
            rowCount++;
            List<Object> cells = new ArrayList<>();
            for (Column column : columns) {
                cells.add(currentCodex.encode(column.getType(), row.get(column.getName())));
            }
            printer.printRecord(cells);
        }
        printer.close();
        return rowCount;
    }
}
