package nl.knaw.dans.repo.axxess.impl;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.TableDataWriter;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TableDataWriterTest extends AxxessTest {

    @Test
    public void extractTableData() throws Exception {
        File dbFile = getFile("Admiraal Evertsen_1815.mdb");
        Database db = null;
        String rootDir = "target/test-output/data-writer";
        try {
            db = DatabaseBuilder.open(dbFile);
            db.setCharset(Charset.forName("ISO8859-1"));

            Table table = db.getTable(db.getTableNames().iterator().next());
            TableDataWriter writer = new TableDataWriter();
            writer.setRootDirectory(rootDir);
            writer.writeTableData(table, null);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

}
