package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;

import java.util.Collection;

/**
 * A {@link Codex} is capable of translating java objects, to a suitable string representation
 * and back again.
 * <p>
 * The contract realised by implementations of this interface for any Object alpha
 * that is of DataType.X:
 * <code>
 * Object beta = codex.decode(DataType.X, codex.encode(DataType.X, alpha).toString());
 * // and
 * beta.equals(alpha);
 * </code>
 * </p>
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

    /**
     * Convenience method to encode a collection of strings. Same as
     * encode(DataType.COMPLEX_TYPE, value).
     *
     * @param value a {@link Collection} of {@link String}s
     * @return the serialization of the collection as a string
     */
    String encodeCollection(Collection<String> value);

    /**
     * Listener for errors and warnings dispatched by a {@link Codex} during encoding and decoding.
     */
    public interface Listener {

        void reportWarning(String message, Throwable cause);

        void reportError(String message, Throwable cause);
    }
}
