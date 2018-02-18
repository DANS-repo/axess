package nl.knaw.dans.repo.axxess.core;


import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class AxxessTest {

    private static final Map<String, String> FILE_MAP = new HashMap<String, String>() {{
        put("CLIWOC21_97.mdb", "https://easy.dans.knaw.nl/ui/rest/datasets/40826/files/2462445/content");
    }};

    public static File getFile(String filename) throws IOException {
        File file = new File("src/test/test-set", filename);
        if (!file.exists()) {
            String urlString = FILE_MAP.get(filename);
            if (urlString == null) {
                throw new FileNotFoundException("Not found: " + filename);
            }
            file = loadFromUrl(urlString, file);
        }
        return file;
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

    @Test
    public void getSomeFile() throws IOException {
        File file = getFile(FILE_MAP.keySet().iterator().next());
        assert file.exists();
    }
}
