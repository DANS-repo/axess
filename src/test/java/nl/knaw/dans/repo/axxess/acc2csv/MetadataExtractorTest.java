package nl.knaw.dans.repo.axxess.acc2csv;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import nl.knaw.dans.repo.axxess.core.Axxess;
import nl.knaw.dans.repo.axxess.core.DefaultCodex;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;
import nl.knaw.dans.repo.axxess.core.ObjectType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


class MetadataExtractorTest {

    @Test
    void getMetadata() throws Exception {
        String databaseFile = "src/test/resources/integration/types/db/all_datatypes.mdb";
        Database database = DatabaseBuilder.open(new File(databaseFile));
        MetadataExtractor metadataExtractor = new MetadataExtractor();

        KeyTypeValueMatrix matrix = metadataExtractor.getMetadata(database);
        // matrix.printVertical(System.out, CSVFormat.RFC4180);
        assertTrue(matrix.getPrefixes().contains(ObjectType.EXTRACTION_METADATA.prefix()));
        assertTrue(matrix.getPrefixes().contains(ObjectType.DATABASE.prefix()));
        assertTrue(matrix.getPrefixes().contains(ObjectType.RELATIONSHIP.prefix(0)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.RELATIONSHIP.prefix(1)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.RELATIONSHIP.prefix(2)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE.prefix(0)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_INDEX.prefix(0, 0)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_INDEX.prefix(0, 1)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_INDEX.prefix(0, 2)));
        for (int i = 0; i < 3; i++) {
            assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_COLUMN.prefix(0, i)));
        }
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE.prefix(1)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_INDEX.prefix(1, 0)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_INDEX.prefix(1, 1)));
        assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_INDEX.prefix(1, 2)));
        // table 'TableDataTypes' has 32 columns
        for (int i = 0; i < 32; i++) {
            assertTrue(matrix.getPrefixes().contains(ObjectType.TABLE_COLUMN.prefix(1, i)));
        }
        assertTrue(matrix.getKeys().contains(Axxess.EM_CONVERSION_DATE));
        assertTrue(matrix.getKeys().contains(Axxess.EM_AXXESS_VERSION));
        assertTrue(matrix.getKeys().contains(Axxess.EM_AXXESS_BUILD));
        assertTrue(matrix.getKeys().contains(Axxess.EM_CODEX));
        assertTrue(matrix.getValues().contains(DefaultCodex.class.getName()));

        //assertEquals("[DB],Filename,TEXT,example.mdb", matrix.getLines().get(8).toString());
    }

    @Test
    void testErrorMessage() throws Exception {
        MetadataExtractor metadataExtractor = new MetadataExtractor();
        metadataExtractor.reportError(new File("foo/bar"), "message, another message", new RuntimeException("bla,foo"));
        List<Throwable> errorList = metadataExtractor.getErrorList();
        for (Throwable t : errorList) {
            //System.out.println(t.getMessage());
            assertTrue(t.getMessage().contains("java.lang.RuntimeException"));
        }
    }
}
