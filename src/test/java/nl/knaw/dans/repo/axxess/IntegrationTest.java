package nl.knaw.dans.repo.axxess;

import nl.knaw.dans.repo.axxess.acc2csv.AxxessToCsvConverter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private static File baseDirectory = new File("src/test/resources/integration").getAbsoluteFile();

    private static String[] dbDirectories = {
      "kohier"
    };

    @BeforeAll
    static void beforeAll() throws Exception {
        for (String name : dbDirectories) {
            assert deleteDirectory(getAcc2csvDir(name));
            assert deleteDirectory(getCsv2accDir(name));
        }
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        if (directoryToBeDeleted.exists()) {
            File[] allContents = directoryToBeDeleted.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
            return directoryToBeDeleted.delete();
        }
        return true;
    }

    private static File getDbFile(String name) throws IOException {
        File dbDir = FileUtils.getFile(baseDirectory, name, "db");
        File[] files = dbDir.listFiles();
        if (files == null || files.length < 1) throw new IOException("dbFile does not exist: " + name);
        return files[0];
    }

    private static File getZipFile(String name) throws IOException {
        String zipFilename = getDbName(name) + ".csv.zip";
        return new File(getAcc2csvZipDir(name), zipFilename);
    }

    private static File getAcc2csvDir(String name) {
        return FileUtils.getFile(baseDirectory, name, "acc2csv");
    }

    private static File getAcc2csvZipDir(String name) {
        return new File(getAcc2csvDir(name), "zipped");
    }

    private static File getAcc2csvFilesDir(String name) {
        return new File(getAcc2csvDir(name), "files");
    }

    private static File getCsv2accDir(String name) {
        return FileUtils.getFile(baseDirectory, name, "csv2acc");
    }

    private static String getDbName(String name) throws IOException {
        return getDbFile(name).getName();
    }

    @Test
    void acc2csv2acc() throws Exception {
        for (String name : dbDirectories) {
            acc2csvZipped(name);
            acc2csvFiled(name);
        }
    }

    private void acc2csvZipped(String name) throws Exception {
        List<File> fileList = new AxxessToCsvConverter()
          .withTargetDirectory(getAcc2csvZipDir(name))
          .withArchiveResults(true)
          .withCompressArchive(true)
          .withManifest(true)
          .convert(getDbFile(name));
        assertEquals(1, fileList.size());
        assertEquals(getZipFile(name), fileList.get(0));
    }

    private void acc2csvFiled(String name) throws Exception {
        List<File> fileList = new AxxessToCsvConverter()
          .withTargetDirectory(getAcc2csvFilesDir(name))
          .withManifest(true)
          .convert(getDbFile(name));
        assertEquals(20, fileList.size());
    }


}
