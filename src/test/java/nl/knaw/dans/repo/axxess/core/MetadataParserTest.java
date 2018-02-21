package nl.knaw.dans.repo.axxess.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetadataParserTest {

    private String cliwocMeta = "src/test/test-set/csv/CLIWOC21_97.mdb.__metadata.csv";

    @Test
    void parseCliwoc() throws Exception {
        XDatabase xdb = MetadataParser.parse(cliwocMeta);

        assertEquals("CLIWOC21_97.mdb", xdb.getString("Filename"));
        assertEquals("", xdb.getString("Password"));
        assertNull(xdb.getString("Not a key"));

        assertTrue(xdb.getBool("(User defined) ReplicateProject"));
        assertEquals(302206860L, xdb.getLong("(User defined) Telephone number"));
        assertEquals(13, xdb.getInt("Table count"));

        String tableNameStr =
          "CLIWOC21,Geodata,Lookup_ES_WindDirection,Lookup_ES_WindForce,Lookup_FR_WindDirection,Lookup_FR_WindForce," +
            "Lookup_NL_WindDirection,Lookup_NL_WindForce,Lookup_UK_WindDirection,Lookup_UK_WindForce," +
            "Magnetic_Declinations,ShipLogbookID,Weather";
        List<String> tablenNames = Arrays.asList(tableNameStr.split(KTV.CSV_DELIMITER));
        assertLinesMatch(tablenNames, xdb.getList("Table names"));

        assertEquals(0, xdb.getRelationships().size());
        assertEquals(0, xdb.getQueries().size());
        assertEquals(13, xdb.getTables().size());

        XTable xt = xdb.getTables().get(0);
        assertEquals(143, xt.getInt("Column count"));

        XColumn xc1 = xt.getColumns().get(1);
        assertEquals("InstAbbr", xc1.getString("Column name"));
        assertEquals("Abbreviation of the Institute where the original data is stored",
          xc1.getString("(Property) Description"));
    }
}
