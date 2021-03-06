package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;

import java.io.File;
import java.util.Collection;

/**
 * A {@link Codex} is capable of translating java objects, to a suitable string representation
 * and back again.
 * <p>
 * The contract realised by implementations of this interface for any Object alpha
 * that is of DataType.X: </p>
 * <pre>
 * Object beta = codex.decode(DataType.X, codex.encode(DataType.X, alpha).toString());
 * // and
 * beta.equals(alpha);
 * </pre>
 */
public interface Codex {

    /**
     * Encode the given <code>value</code> in accordance with the given type.
     *
     * @param type  the {@link DataType} of the value
     * @param value the object to be encoded
     * @return Object suitable for serialization to String
     */
    Object encode(DataType type, Object value);

    /**
     * Decode the given string <code>value</code> in accordance with the given type.
     *
     * @param type  the {@link DataType} of the value
     * @param value the value to be decoded
     * @return Object equal to the value that was encoded
     */
    Object decode(DataType type, String value);

    void setListener(ErrorListener listener);

    /**
     * Convenience method to encode a collection of strings. Same as
     * encode(DataType.COMPLEX_TYPE, value).
     *
     * @param value a {@link Collection} of {@link String}s
     * @return the serialization of the collection as a string
     */
    String encodeCollection(Collection<String> value);

    void setCurrentFile(File file);

    void setErrorListener(ErrorListener listener);
}
