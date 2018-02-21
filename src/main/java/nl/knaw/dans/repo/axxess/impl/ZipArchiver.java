package nl.knaw.dans.repo.axxess.impl;

import nl.knaw.dans.repo.axxess.acc2csv.Archiver;
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
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiver implements Archiver {

    public static final String EXTENSION = ".zip";
    private static final int BUFFER = 2048;

    private static Logger LOG = LoggerFactory.getLogger(ZipArchiver.class);

    private CRC32 crc32;

    public ZipArchiver() {
        crc32 = new CRC32();
    }

    @Override
    public File archive(List<File> files, boolean compress, File target) throws IOException {
        String zipFilename = FilenameUtils.removeExtension(target.getAbsolutePath()) + EXTENSION;
        LOG.info("Zipping to {}", zipFilename);
        ZipOutputStream out = null;
        try {
            FileOutputStream dest = new FileOutputStream(zipFilename);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            if (compress) {
                out.setMethod(ZipOutputStream.DEFLATED);
            } else {
                out.setMethod(ZipOutputStream.STORED);
            }
            for (File file : files) {
                BufferedInputStream origin = null;
                try {
                    FileInputStream fi = new FileInputStream(file);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(file.getName());
                    if (!compress) {
                        entry.setSize(file.length());
                        entry.setCrc(computeCRC32(file));
                    }
                    out.putNextEntry(entry);
                    int count;
                    byte[] data = new byte[BUFFER];
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    if (origin != null) {
                        origin.close();
                    }
                }
            }

        } finally {
            if (out != null) {
                out.close();
            }
        }
        return new File(zipFilename);
    }

    private long computeCRC32(File file) throws IOException {
        crc32.reset();
        BufferedInputStream origin = null;
        try {
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, BUFFER);
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                crc32.update(data, 0, count);
            }
            return crc32.getValue();
        } finally {
            if (origin != null) {
                origin.close();
            }
        }
    }
}
