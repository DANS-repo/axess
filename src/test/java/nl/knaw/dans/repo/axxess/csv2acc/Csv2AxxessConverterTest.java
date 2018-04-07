package nl.knaw.dans.repo.axxess.csv2acc;

import org.junit.jupiter.api.Test;

public class Csv2AxxessConverterTest {

    @Test
    void reconstruct() throws Exception {
        String file = "axxess-out/Rhijn_1848_mdb";

        Csv2AxxessConverter converter = new Csv2AxxessConverter();
        converter.convert(file);
    }
}
