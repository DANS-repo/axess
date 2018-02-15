package nl.knaw.dans.repo.axxess;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.AbstractWriter;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

public class TableDataWriter extends AbstractWriter {

    private final TableDataExtractor extractor;

    public TableDataWriter() {
        extractor = new TableDataExtractor();
    }

    public List<File> writeDatabaseData(Database db, CSVFormat format) {
        return null;
    }

    public File writeTableData(Table table, CSVFormat format) throws IOException {
        String filename = buildPaths(getFilenameComposer().getTableDataFilename(table));
        File file = new File(filename);
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
            extractor.getTableData(table, osw, getCsvFormat(format));
        } finally {
            if (osw != null) osw.close();
        }
        return file;
    }
}
