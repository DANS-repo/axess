package nl.knaw.dans.repo.axxess;

import com.healthmarketscience.jackcess.Database;
import nl.knaw.dans.repo.axxess.acc2csv.AxxessToCsvConverter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private static File baseDirectory = new File("src/test/resources/integration").getAbsoluteFile();

    private static String[][] databases = {
      // File format: V1997 [VERSION_3]   AccessVersion: 07.53
      {"avereest", "avereest.mdb",
        "https://easy.dans.knaw.nl/ui/rest/datasets/61704/files/4917456/content" },

      // File format: V2000 [VERSION_4]   AccessVersion: 08.50
      {"walcheren", "Boedelbestand Walcheren 1755-1855.MDB",
        "https://easy.dans.knaw.nl/ui/rest/datasets/48968/files/2964358/content" },

      // File format: V2007 [VERSION_12]  AccessVersion: 09.50
      {"kohier", "KOHIER1748.accdb",
        "https://easy.dans.knaw.nl/ui/rest/datasets/48078/files/2804052/content" }
    };

    @BeforeAll
    static void beforeAll() throws Exception {
        for (String[] name : databases) {
            assert deleteDirectory(getAcc2csvDir(name[0]));
            assert deleteDirectory(getCsv2accDir(name[0]));
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
        File[] files = dbDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (Database.FileFormat fm : Database.FileFormat.values()) {
                    if (name.toLowerCase().endsWith(fm.getFileExtension())) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (files == null || files.length < 1) throw new IOException("dbFile does not exist: " + name);
        return files[0];
    }

    private static File getZipFile(String name) throws IOException {
        String zipFilename = getDbName(name) + ".csv.zip";
        return new File(getAcc2csvZipDir(name), zipFilename);
    }

    private static File getMetadataFile(String name) throws IOException {
        String metadataFilename = getDbName(name) + "._metadata.csv";
        return  new File(getAcc2csvFilesDir(name), metadataFilename);
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
        for (String name[] : databases) {
            acc2csvZipped(name[0]);
            acc2csvFiled(name[0]);
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
        assertTrue(fileList.contains(getMetadataFile(name)));
        assertTrue(getMetadataFile(name).exists());
    }

    private static File loadFromUrl(String urlString, File file) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        File dir = file.getParentFile();
        assert dir.exists() || dir.mkdirs();
        assert file.exists() || file.createNewFile();

        BufferedInputStream bif = null;
        BufferedOutputStream bof = null;
        System.out.print("Downloading " + file.getName());

        try {
            bif = new BufferedInputStream(conn.getInputStream());
            bof = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            int read = 0;
            int total = 0;
            while ((read = bif.read(buffer, 0, buffer.length)) != -1) {
                bof.write(buffer, 0, read);
                total += read;
                if (total % 100000 == 0) {
                    System.out.print(" |");
                }
            }
            System.out.println("\nDownloaded: " + total + " bytes");
            return file;
        } finally {
            if (bof != null) {
                bof.close();
            }
            if (bif != null) {
                bif.close();
            }
        }
    }


}
