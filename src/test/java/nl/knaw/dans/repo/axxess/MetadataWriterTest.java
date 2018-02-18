package nl.knaw.dans.repo.axxess;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.axxess.core.AxxessTest;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MetadataWriterTest extends AxxessTest {


    @Test
    public void writesDatabaseMetadata() throws Exception {
        File dbFile = getFile("Admiraal Evertsen_1815.mdb");
        Database db = null;
        String rootDir = "target/test-output/metadata-writer";
        try {
            db = DatabaseBuilder.open(dbFile);
            MetadataWriter writer = new MetadataWriter();
            writer.setTargetDirectory(rootDir);
            File file = writer.writeDatabaseMetadata(db, CSVFormat.DEFAULT.withDelimiter(','), true);
            assertThat(file.exists(), is(true));

        } finally {
            if (db != null) {
                db.close();
            }
        }

    }


}
