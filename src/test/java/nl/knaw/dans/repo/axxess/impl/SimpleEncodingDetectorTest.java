package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.axxess.core.AxxessTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;

public class SimpleEncodingDetectorTest extends AxxessTest {

    @Test
    public void detect() throws Exception {
        SimpleEncodingDetector sed = new SimpleEncodingDetector();
        File dbFile = getFile("axess.accdb");
        Database db = null;
        try {
            db = DatabaseBuilder.open(dbFile);

            Optional<Charset> mbCharset = sed.detectEncoding(db);


        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
