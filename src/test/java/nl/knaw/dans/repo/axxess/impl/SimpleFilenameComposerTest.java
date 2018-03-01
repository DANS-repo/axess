package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleFilenameComposerTest {

    private static Database database;
    private static SimpleFilenameComposer composer;

    @BeforeAll
    static void beforeAll() throws Exception {
        String databaseFile = "src/test/resources/integration/types/db/all_datatypes.mdb";
        database = DatabaseBuilder.open(new File(databaseFile));
        composer = new SimpleFilenameComposer();
    }

    @AfterAll
    static void afterAll() throws Exception {
        database.close();
    }

    @Test
    void returnsCorrectFilenames() throws Exception {
        //@formatter:off
        String metadataFilename =       "all_datatypes.mdb._metadata.csv";
        String tableFilename =          "all_datatypes.mdb.smallTable.csv";
        String zipFilename =            "all_datatypes.mdb.csv.zip";
        String newDatabaseFilename =    "all_datatypes.mdb.ext";
        //@formatter:on

        assertEquals(metadataFilename, composer.getMetadataFilename(database));
        assertEquals(tableFilename, composer.getTableDataFilename(database.getTable("smallTable")));
        assertEquals(zipFilename, composer.getArchiveFilename(database));
        assertEquals(newDatabaseFilename,
          composer.getNewDatabaseFilename("all_datatypes.mdb.____metadata.csv", ".ext"));
        assertEquals(tableFilename,
          composer.getTableDataFileFor(new File(metadataFilename), "smallTable").getName());
    }
}
