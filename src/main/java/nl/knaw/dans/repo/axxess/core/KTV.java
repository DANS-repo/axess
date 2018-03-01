package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;
import org.apache.commons.csv.CSVRecord;

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

    public KTV(CSVRecord record, Codex codex) {
        Iterator<String> iterator = record.iterator();
        prefix = iterator.next();
        key = iterator.next();
        type = DataType.valueOf(iterator.next());
        value = codex.decode(type, iterator.next());
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
        KTV clone = new KTV(key.replaceAll("\\(.*?\\)", ""), type, value);
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

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s", prefix, key, type.name(), value == null ? null : value.toString());
    }
}
