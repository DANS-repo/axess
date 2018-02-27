package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultCodexTest {


    @Test
    void encodeDecodeTime() throws Exception {
        DefaultCodex codex = new DefaultCodex(null);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1899, 11, 30, 12, 47, 0);
        Date timeDate = cal.getTime();
        // timeDate is what we get from Jackcess for time values.
        //System.out.println(timeDate);

        String encoded = (String) codex.encode(DataType.SHORT_DATE_TIME, timeDate);
        //System.out.println(encoded);
        assertEquals("12:47:00", encoded);

        Date decoded = (Date) codex.decode(DataType.SHORT_DATE_TIME, encoded);
        //System.out.println(decoded);
        assertEquals(timeDate, decoded);
    }

    @Test
    void encodeDecodeDateTime() throws Exception {
        DefaultCodex codex = new DefaultCodex(null);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2018, 11, 30, 12, 47, 0);
        Date dtDate = cal.getTime();
        // dtDate is what we get from Jackcess for datetime values.
        //System.out.println(dtDate);

        String encoded = (String) codex.encode(DataType.SHORT_DATE_TIME, dtDate);
        //System.out.println(encoded);
        assertEquals("2018-12-30T12:47:00", encoded);

        Date decoded = (Date) codex.decode(DataType.SHORT_DATE_TIME, encoded);
        //System.out.println(decoded);
        assertEquals(dtDate, decoded);
    }

    @Test
    void encodeDecodeEarlyDateTime() throws Exception {
        DefaultCodex codex = new DefaultCodex(null);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1899, 11, 29, 12, 47, 0);
        Date dtDate = cal.getTime();
        // dtDate is what we get from Jackcess for datetime values.
        //System.out.println(dtDate);

        String encoded = (String) codex.encode(DataType.SHORT_DATE_TIME, dtDate);
        //System.out.println(encoded);
        assertEquals("1899-12-29T12:47:00", encoded);

        Date decoded = (Date) codex.decode(DataType.SHORT_DATE_TIME, encoded);
        //System.out.println(decoded.toString());
        assertEquals(dtDate, decoded);
    }

    @Test
    void encodeDecodeBoolean() throws Exception {
        DefaultCodex codex = new DefaultCodex(null)
          .withBooleanStrings("on", "off");

        String encoded = (String) codex.encode(DataType.BOOLEAN, true);
        assertEquals("on", encoded);
        boolean decoded = (boolean) codex.decode(DataType.BOOLEAN, encoded);
        assertTrue(decoded);

        encoded = (String) codex.encode(DataType.BOOLEAN, false);
        assertEquals("off", encoded);
        decoded = (boolean) codex.decode(DataType.BOOLEAN, encoded);
        assertFalse(decoded);
    }

    @Test
    @SuppressWarnings("unchecked")
    void encodeDecodeComplexType() throws Exception {
        DefaultCodex codex = new DefaultCodex(null);

        Long l = 12345L;
        Long encoded = (Long) codex.encode(DataType.COMPLEX_TYPE, l);
        assertEquals("12345", encoded.toString());

        Long decoded = (Long) codex.decode(DataType.COMPLEX_TYPE, encoded.toString());
        assertEquals(l, decoded);

        List<String> list = Arrays.asList("alpha", "beta", "gamma");
        String encodedStr = (String) codex.encode(DataType.COMPLEX_TYPE, list);
        assertEquals("alpha,beta,gamma", encodedStr);

        List<String> decodedList = (List<String>) codex.decode(DataType.COMPLEX_TYPE, encodedStr);
        assertEquals(list, decodedList);
    }

    @Test
    void contract() throws Exception {
        DefaultCodex codex = new DefaultCodex(null);

        String alpha = "foo\r\nbar\nbas";
        String beta = (String) codex.decode(DataType.TEXT, codex.encode(DataType.TEXT, alpha).toString());
        assertEquals(alpha, beta);

        String encodedStr = (String) codex.encode(DataType.TEXT, alpha);
        String encodeStr2 = codex.encode(DataType.TEXT, codex.decode(DataType.TEXT, encodedStr).toString()).toString();
        assertEquals(encodedStr, encodeStr2);
        assertEquals(alpha, codex.decode(DataType.TEXT, encodedStr));

        double da = 123.456;
        double db = (double) codex.decode(DataType.DOUBLE, codex.encode(DataType.DOUBLE, da).toString());
        assertEquals(da, db);
    }

}
