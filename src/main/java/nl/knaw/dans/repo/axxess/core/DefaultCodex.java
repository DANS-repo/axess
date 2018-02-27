package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default {@link Codex} suitable for most use cases. A {@link Codex} is used to translate from java objects,
 * representing values in an Access database,
 * to a suitable string representation for the csv file format.
 * And to get from csv file to a refurbished Access database, it translates string values back to these java objects.
 * <p>
 * Caveat dates and times. Because we get both date time and time from
 * Jackcces as a java.util.{@link Date}, the only way to discriminate between them is on
 * the null-values for year, month and date which happen to be year=1899, month=12 and day=30.
 * As a consequence any real date time on January 30, 1899 will be interpreted as time.
 * <p>
 * The representation for date time is {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} ('2011-12-03T10:15:30'),
 * that for time is {@link DateTimeFormatter#ISO_LOCAL_TIME} ('10:15:30').
 * </p>
 * <p>
 * Carriage return and line break characters in {@link String}s of data types
 * {@link DataType#TEXT} and {@link DataType#MEMO} are encoded as <code>u+0000</code> and
 * <code>u+0001</code> respectively - and decoded back to the original characters.
 * </p>
 * <p>
 * Caveat {@link DataType#COMPLEX_TYPE}. We also use it to represent collections of data (represented as
 * comma-separated strings). However, if the object to be encoded is a {@link Long} or the string to be decoded
 * has only digits the value is treated as a Long.
 * </p>
 * <p>
 * Byte arrays of {@link DataType#OLE} are encoded and decoded as {@link Base64} strings.
 * </p>
 * <p>
 * The string representation for boolean values can be set {@link #withBooleanStrings(String, String)}.
 * (Yes/No, On/Off, 1/0 etc.)
 * The default string representation is "true" for <code>true</code> and "false" for <code>false</code>.
 * </p>
 * <p>
 * Any warnings or errors that take place during encoding or decoding of values will be reported
 * to the given {@link Codex.Listener}.
 * </p>
 */
public class DefaultCodex implements Codex {

    private static final String CSV_DELIMITER = ",";
    private static final Pattern digitPattern = Pattern.compile("d?\\d+");
    private static Logger LOG = LoggerFactory.getLogger(DefaultCodex.class);
    private Listener listener;
    private String booleanTrue = "true";
    private String booleanFalse = "false";

    /**
     * Construct a new {@link DefaultCodex} with the given <code>listener</code>.
     *
     * @param listener listens for errors and warnings, may be <code>null</code>
     */
    public DefaultCodex(Listener listener) {
        this.listener = listener;
    }

    /**
     * Set the string values for <code>true</code> and <code>false</code>. This will help
     * adept csv-files for certain applications.
     *
     * @param trueString  String to be used for boolean value <code>true</code>
     * @param falseString String to be used for boolean value <code>false</code>
     * @return this {@link DefaultCodex} for chaining
     * @throws IllegalArgumentException if <code>trueString</code> is <code>null</code> or empty
     */
    public DefaultCodex withBooleanStrings(String trueString, String falseString) {
        if (trueString == null) {
            throw new IllegalArgumentException("BooleanTrue String cannot be null");
        }
        if (trueString.isEmpty()) {
            throw new IllegalArgumentException("BooleanTrue String cannot be empty");
        }
        booleanTrue = trueString;
        booleanFalse = falseString;
        return this;
    }

    public Object encode(DataType type, Object value) {
        if (value == null) {
            return null;
        }
        if (DataType.BOOLEAN == type) {
            return ((boolean) value) ? booleanTrue : booleanFalse;
        } else if (DataType.SHORT_DATE_TIME == type) {
            return encodeDate(value);
        } else if (DataType.TEXT == type || DataType.MEMO == type) {
            return encodeString(value);
        } else if (DataType.OLE == type) {
            return encodeOLE(value);
        } else if (DataType.COMPLEX_TYPE == type) {
            if (value instanceof Long) {
                return value;
            } else {
                return encodeCollection(value);
            }
        }
        return value;
    }

    public Object decode(DataType type, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (DataType.BOOLEAN == type) {
            return value.equals(booleanTrue);
        } else if (DataType.BYTE == type) {
            return Byte.valueOf(value);
        } else if (DataType.INT == type) {
            return Integer.parseInt(value);
        } else if (DataType.LONG == type) {
            return Integer.parseInt(value);
        } else if (DataType.MONEY == type) {
            return new BigDecimal(value);
        } else if (DataType.FLOAT == type) {
            return Float.valueOf(value);
        } else if (DataType.DOUBLE == type) {
            return Double.valueOf(value);
        } else if (DataType.SHORT_DATE_TIME == type) {
            return decodeDate(value);
        } else if (DataType.BINARY == type) {
            return value;
        } else if (DataType.TEXT == type) {
            return decodeString(value);
        } else if (DataType.OLE == type) {
            return decodeOLE(value);
        } else if (DataType.MEMO == type) {
            return decodeString(value);
        } else if (DataType.UNKNOWN_0D == type) {
            return value;
        } else if (DataType.GUID == type) {
            return value;
        } else if (DataType.NUMERIC == type) {
            return new BigDecimal(value);
        } else if (DataType.UNKNOWN_11 == type) {
            return value;
        } else if (DataType.COMPLEX_TYPE == type) {
            if (Pattern.matches("[0-9]*", value)) {
                return Long.valueOf(value);
            } else {
                return decodeCollection(value);
            }
        } else if (DataType.BIG_INT == type) {
            return Long.valueOf(value);
        } else if (DataType.UNSUPPORTED_FIXEDLEN == type) {
            return value;
        } else if (DataType.UNSUPPORTED_VARLEN == type) {
            return value;
        } else {
            getListener().reportWarning(">> Unknown DataType: " + type, null);
            return value;
        }
    }

    private String encodeOLE(Object value) {
        if (value instanceof byte[]) {
            return Base64.getEncoder().encodeToString(((byte[]) value));
        } else {
            getListener().reportWarning(">> Unexpected OLE field type: " + value.getClass(), null);
            return null;
        }
    }

    private byte[] decodeOLE(String value) {
        return Base64.getDecoder().decode(value);
    }

    private String encodeString(Object value) {
        if (value instanceof String) {
            return ((String) value).replaceAll("\r", "\u0000").replaceAll("\n", "\u0001");
        } else {
            getListener().reportWarning(">> Unexpected String field type: " + value.getClass(), null);
            return null;
        }
    }

    private String decodeString(String value) {
        return value.replaceAll("\u0000", "\r").replaceAll("\u0001", "\n");
    }

    private Object getJavaTime(Date value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(((Date) value));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if (year == 1899 && month == 12 && day == 30) {
            return LocalTime.of(hour, minute, second);
        } else {
            return LocalDateTime.of(year, month, day, hour, minute, second);
        }
    }

    private String encodeDate(Object value) {
        // Access date times are unaware of time zones so we cannot use
        //      LocalDateTime ldt = LocalDateTime.ofInstant(((Date)value).toInstant(), ZoneId.systemDefault());
        // because ZoneId will (over)correct historical date times.
        // i.e. ZoneId Europe/Amsterdam will give 1899-02-26T12:06:32 for 1899-02-26T12:47:00.
        if (value instanceof Date) {
            Object dt = getJavaTime(((Date) value));
            if (dt instanceof LocalTime) {
                return DateTimeFormatter.ISO_LOCAL_TIME.format(((LocalTime) dt));
            } else {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(((LocalDateTime) dt));
            }
        } else {
            getListener().reportWarning(">> Unexpected Date field type: " + value.getClass(), null);
            return null;
        }
    }

    private Date decodeDate(String value) {
        // See encodeDate
        Matcher m = digitPattern.matcher(value);
        if (value.length() == 8) {
            int hour = 0;
            int minute = 0;
            int second = 0;
            if (m.find()) {
                hour = Integer.parseInt(m.group());
                if (m.find()) {
                    minute = Integer.parseInt(m.group());
                    if (m.find()) {
                        second = Integer.parseInt(m.group());
                    }
                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(1899, 11, 30, hour, minute, second);
            return new Date(calendar.getTimeInMillis());
        } else {
            int year = 0;
            int month = 0;
            int day = 1;
            int hour = 0;
            int minute = 0;
            int second = 0;
            if (m.find()) {
                year = Integer.parseInt(m.group());
                if (m.find()) {
                    month = Integer.parseInt(m.group()) - 1;
                    if (m.find()) {
                        day = Integer.parseInt(m.group());
                        if (m.find()) {
                            hour = Integer.parseInt(m.group());
                            if (m.find()) {
                                minute = Integer.parseInt(m.group());
                                if (m.find()) {
                                    second = Integer.parseInt(m.group());
                                }
                            }
                        }
                    }
                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(year, month, day, hour, minute, second);
            return new Date(calendar.getTimeInMillis());
        }
    }

    @SuppressWarnings("unchecked")
    private String encodeCollection(Object value) {
        if (value instanceof Collection) {
            return encodeCollection(((Collection) value));
        } else {
            getListener().reportWarning(">> Unexpected Complex Type field type: " + value.getClass(), null);
            return null;
        }
    }

    public String encodeCollection(Collection<String> value) {
        return String.join(CSV_DELIMITER, (value));
    }

    private Collection<String> decodeCollection(String value) {
        return Arrays.asList(value.split(CSV_DELIMITER));
    }

    private Listener getListener() {
        if (listener == null) {
            listener = new Listener() {

                @Override
                public void reportWarning(String message, Throwable cause) {
                    LOG.warn(message, cause);
                }

                @Override
                public void reportError(String message, Throwable cause) {
                    LOG.error(message, cause);
                }
            };
        }
        return listener;
    }


}
