package nl.knaw.dans.repo.axxess.impl;

import nl.knaw.dans.repo.axxess.core.Archiver;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiver implements Archiver {

    public static final String EXTENSION = ".zip";
    private static final int BUFFER = 2048;

    private static Logger LOG = LoggerFactory.getLogger(ZipArchiver.class);

    @Override
    public File archive(List<File> files, boolean compress, File target) throws IOException {
        String zipFilename = FilenameUtils.removeExtension(target.getAbsolutePath()) + EXTENSION;
        LOG.info("Zipping to {}", zipFilename);
        FileOutputStream dest = new FileOutputStream(zipFilename);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        if (compress) {
            out.setMethod(ZipOutputStream.DEFLATED);
        } else {
            //out.setMethod(ZipOutputStream.STORED);
            out.setLevel(0);
        }
        for (File file : files) {
            FileInputStream fi = new FileInputStream(file);
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            int count;
            byte[] data = new byte[BUFFER];
            while((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();
        return new File(zipFilename);
    }
}
