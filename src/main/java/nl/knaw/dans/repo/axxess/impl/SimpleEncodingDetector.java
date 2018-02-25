package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import nl.knaw.dans.repo.axxess.acc2csv.EncodingDetector;
import nl.knaw.dans.repo.axxess.acc2csv.MetadataExtractor;
import nl.knaw.dans.repo.axxess.core.KTV;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;
import nl.knaw.dans.repo.axxess.core.ObjectType;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

public class SimpleEncodingDetector implements EncodingDetector {

    private static Logger LOG = LoggerFactory.getLogger(SimpleEncodingDetector.class);

    private final MetadataExtractor metadataExtractor;
    private final TikaEncodingDetector tikaEncodingDetector;

    public SimpleEncodingDetector() {
        metadataExtractor = new MetadataExtractor();
        tikaEncodingDetector = new TikaEncodingDetector();
    }

    @Override
    public Optional<Charset> detectEncoding(Database db) throws IOException {
        String tikaGuess = getTikaGuess(db);
        LOG.debug("Encoding. Tika detected '{}', for {}", tikaGuess, db.getFile().getName());

        KeyTypeValueMatrix matrix = metadataExtractor.getDatabaseMetadata(db);
        KTV ff = matrix.get(ObjectType.DATABASE, "File format");
        Database.FileFormat fileFormat = ff == null ? null : (Database.FileFormat) ff.getValue();
        KTV cs = matrix.get(ObjectType.DATABASE, "Charset");
        Charset charset = cs == null ? null : (Charset) cs.getValue();
        LOG.debug("Encoding. Jackcess detected '{}', for {}", charset, db.getFile().getName());

        if (Database.FileFormat.V1997 == fileFormat) {
            return Optional.of(Charset.forName("ISO8859-1")); // also known as Latin-1
        }
        // More recent versions of ms access will probably have UTF-16LE, which is correctly detected by jackcess.
        return Optional.empty();
    }

    public String getTikaGuess(Database db) throws IOException {
        // always returns windows-1252.
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(db.getFile());
            return tikaEncodingDetector.guessEncoding(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}
