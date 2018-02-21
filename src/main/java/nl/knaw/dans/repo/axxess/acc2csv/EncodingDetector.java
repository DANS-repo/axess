package nl.knaw.dans.repo.axxess.acc2csv;

import com.healthmarketscience.jackcess.Database;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

public interface EncodingDetector {

    Optional<Charset> detectEncoding(Database db) throws IOException;

}
