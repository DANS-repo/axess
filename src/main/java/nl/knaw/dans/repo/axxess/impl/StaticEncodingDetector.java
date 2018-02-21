package nl.knaw.dans.repo.axxess.impl;

import com.healthmarketscience.jackcess.Database;
import nl.knaw.dans.repo.axxess.core.EncodingDetector;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

public class StaticEncodingDetector implements EncodingDetector {

    private final Charset encoding;

    public StaticEncodingDetector(String charset) {
        encoding = Charset.forName(charset);
    }

    @Override
    public Optional<Charset> detectEncoding(Database db) throws IOException {
        return Optional.of(encoding);
    }
}
