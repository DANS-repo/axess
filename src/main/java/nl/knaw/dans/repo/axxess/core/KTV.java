package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;
import org.apache.commons.csv.CSVRecord;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KTV {

    private static Pattern indexPattern = Pattern.compile("-?\\d+");

    private final String key;
    private final DataType type;
    private final Object value;
    private String prefix = null;

    public KTV(String key, DataType type, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public KTV(CSVRecord record) {
        Iterator<String> iterator = record.iterator();
        prefix = iterator.next();
        key = iterator.next();
        type = DataType.valueOf(iterator.next());
        value = convert(type, iterator.next());
    }

    public String getKey() {
        return key;
    }

    public DataType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getFirstIndex() {
        Matcher m = indexPattern.matcher(prefix);
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        return -1;
    }

    public int getSecondIndex() {
        Matcher m = indexPattern.matcher(prefix);
        if (m.find()) {
            if (m.find()) {
                return Integer.parseInt(m.group());
            }
        }
        return -1;
    }

    public KTV cloneClean() {
        KTV clone = new KTV(key.replaceAll("\\(.*?\\) ", ""), type, value);
        clone.prefix = prefix;
        return clone;
    }

    public boolean isDatabaseProp() {
        return Pattern.matches(ObjectType.DATABASE.pattern(), prefix);
    }

    public boolean isRelationshipProp() {
        return Pattern.matches(ObjectType.RELATIONSHIP.pattern(), prefix);
    }

    public boolean isQueryProp() {
        return Pattern.matches(ObjectType.QUERY.pattern(), prefix);
    }

    public boolean isTableProp() {
        return Pattern.matches(ObjectType.TABLE.pattern(), prefix);
    }

    public boolean isTableIndexProp() {
        return Pattern.matches(ObjectType.TABLE_INDEX.pattern(), prefix);
    }

    public boolean isTableColumnProp() {
        return Pattern.matches(ObjectType.TABLE_COLUMN.pattern(), prefix);
    }

    public Object convert(DataType dataType, String value) {
        if (DataType.TEXT == dataType) {
            return value;
        } else if (DataType.MEMO == dataType) {
            return value;
        } else if (DataType.GUID == dataType) {
            return value;
        } else if (DataType.BOOLEAN == dataType) {
            return value.equals("true");
        } else if (DataType.COMPLEX_TYPE == dataType) {
            return Arrays.asList(value.split(Axxess.CSV_DELIMITER));
        } else if (DataType.LONG == dataType) {
            return Long.parseLong(value);
        } else if (DataType.INT == dataType) {
            return Integer.parseInt(value);
        } else if (DataType.BINARY == dataType) {
            return value;
        } else {
            throw new IllegalArgumentException("Unknown DataType: " + type);
        }
    }

}
