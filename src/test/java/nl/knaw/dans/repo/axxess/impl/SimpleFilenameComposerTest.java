package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.TableBuilder;
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
        File dbFile = new File("target/example db.mdb");
        dbFile.getParentFile().mkdirs();
        dbFile.createNewFile();
        database = DatabaseBuilder.create(Database.FileFormat.V2010, dbFile);
        new TableBuilder("a simple table")
          .addColumn(new ColumnBuilder("a simple column")
            .setType(DataType.TEXT))
          .toTable(database);
        composer = new SimpleFilenameComposer();
    }

    @AfterAll
    static void afterAll() throws Exception {
        database.close();
        new File("target/example db.mdb").delete();
    }

    @Test
    void returnsCorrectFilenames() throws Exception {
        //@formatter:off
        String csvDirectoryName =       "example db_mdb";
        String metadataFilename =       "example db.mdb._metadata.csv";
        String tableFilename =          "example db.mdb.a_simple_table.csv";
        String zipFilename =            "example db.mdb.csv.zip";
        String newDatabaseFilename =    "example db.mdb.ext";
        //@formatter:on

        assertEquals(csvDirectoryName, composer.getCsvDirectoryName(database));
        assertEquals(metadataFilename, composer.getMetadataFilename(database));
        assertEquals(tableFilename, composer.getTableDataFilename(database.getTable("a simple table")));
        assertEquals(zipFilename, composer.getArchiveFilename(database));
        assertEquals(newDatabaseFilename,
          composer.getNewDatabaseFilename("example db.mdb.____metadata.csv", ".ext"));
        assertEquals(tableFilename,
          composer.getTableDataFileFor(new File(metadataFilename), "a simple table").getName());
    }
}
