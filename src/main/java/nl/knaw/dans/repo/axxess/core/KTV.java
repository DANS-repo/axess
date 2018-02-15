package nl.knaw.dans.repo.axxess.core;


public class KTV {

    private final Object key;
    private final Object type;
    private final Object value;
    private String prefix = null;

    public KTV(Object key, Object type, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public Object getType() {
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
}
