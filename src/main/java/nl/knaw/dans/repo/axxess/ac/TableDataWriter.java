package nl.knaw.dans.repo.axxess.ac;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.AbstractWriter;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TableDataWriter extends AbstractWriter {

    private static Logger LOG = LoggerFactory.getLogger(TableDataWriter.class);

    private final TableDataExtractor extractor;

    public TableDataWriter() {
        extractor = new TableDataExtractor();
    }

    public List<File> writeDatabaseData(Database db, CSVFormat format) throws IOException, AxxessException {
        List<File> convertedFiles = new ArrayList<>();
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            convertedFiles.add(writeTableData(table, format));
        }
        return convertedFiles;
    }

    public File writeTableData(Table table, CSVFormat format) throws IOException, AxxessException {
        String filename = buildPaths(getFilenameComposer().getTableDataFilename(table));
        File file = new File(filename);
        if (file.exists()) {
            throw new AxxessException("File exists: " + file.getAbsolutePath());
        }
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
            int rowCount = extractor.getTableData(table, osw, getCsvFormat(format));
            LOG.debug("Wrote {} records to {}", rowCount, file.getName());
        } finally {
            if (osw != null) {
                osw.close();
            }
        }
        return file;
    }
}
