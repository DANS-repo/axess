package nl.knaw.dans.repo.axxess.core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public abstract class Converter<T extends Converter> extends Extractor<T> {

    private boolean addManifest;

    private int dbCount;

    public List<File> convert(String filename) throws AxxessException {
        return convert(new File(filename));
    }

    public abstract List<File> convert(File file) throws AxxessException;

    @SuppressWarnings("unchecked")
    public T setIncludeManifest(boolean addManifest) {
        this.addManifest = addManifest;
        return (T) this;
    }

    public boolean isIncludingManifest() {
        return addManifest;
    }

    public int getConvertedDatabaseCount() {
        return dbCount;
    }

    protected void reset() {
        super.reset();
        dbCount = 0;
    }

    protected void increaseDbCount() {
        dbCount++;
    }

    protected void addManifest(List<File> files) throws IOException {
        if (files.isEmpty()) {
            return;
        }
        File directory = files.get(0).getParentFile();
        File manifest = new File(directory, "manifest-sha1.txt");
        PrintWriter out = null;
        try {
            out = new PrintWriter(manifest, "UTF-8");
            for (File file : files) {
                out.println(String.format("%s %s", file.getName(), computeSHA1(file)));
            }
            files.add(manifest);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private String computeSHA1(File file) throws IOException {
        BufferedInputStream buff = null;
        try {
            buff = new BufferedInputStream(new FileInputStream(file));
            return DigestUtils.sha1Hex(buff);
        } finally {
            if (buff != null) {
                buff.close();
            }
        }
    }
}
