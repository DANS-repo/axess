package nl.knaw.dans.repo.ingress.core;


public class KTV {

  private Object key;
  private final Object type;
  private final Object value;

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

  public void prefixKey(String prefix) {
    key = prefix + key;
  }
}
