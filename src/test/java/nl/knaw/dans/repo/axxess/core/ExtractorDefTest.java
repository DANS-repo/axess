package nl.knaw.dans.repo.axxess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtractorDefTest {

    @Test
    void neverReturnsNull() throws Exception {
        ExtractorDef ed = new ExtractorDef();
        assertNotNull(ed.getCodex(null));
        assertNotNull(ed.getCsvFormat());
        assertNotNull(ed.getFilenameComposer());
        assertNotNull(ed.getTargetCharset());
        assertNotNull(ed.getTargetDirectory("."));
    }

    @Test
    void returnsAbsoluteTargetDirectory() throws Exception {
        ExtractorDef ed = new ExtractorDef();
        assertTrue(ed.getTargetDirectory("").isAbsolute());
    }
}
