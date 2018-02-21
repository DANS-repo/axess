package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import nl.knaw.dans.repo.axxess.core.AxxessTest;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
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
            writer.setTargetDirectory(rootDir);
            writer.writeTableData(table, null);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Test
    public void readHeader() throws Exception {
        File dbFile = getFile("Admiraal Evertsen_1815.mdb");
        InputStream ins = new FileInputStream(dbFile);
        // byte[] bytes = new byte[32];
        // IOUtils.read(ins, bytes, 0, 32);
        // String str = new String(bytes, "ISO8859-9");
        //
        // System.out.println(str);
        TikaEncodingDetector detector = new TikaEncodingDetector();
        System.out.println(detector.guessEncoding(ins));


        Charset encoding = Charset.forName("windows-1252");
        System.out.println(encoding.aliases());
    }


}
