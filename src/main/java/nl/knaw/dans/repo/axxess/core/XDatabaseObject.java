package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class XDatabaseObject {

    private List<KTV> properties = new ArrayList<>();

    public List<KTV> getProperties() {
        return properties;
    }

    public void addProperty(KTV ktv) {
        properties.add(ktv);
    }

    public List<KTV> getRealProperties(String startStr) {
        return properties
          .stream()
          .filter(ktv -> ktv.getKey().startsWith(startStr))
          .map(KTV::cloneClean)
          .collect(Collectors.toList());
    }

    public String getString(String key) {
        return properties
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((String) ktv.getValue()))
          .orElse(null);
    }

    public boolean getBool(String key) {
        return properties
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((boolean) ktv.getValue()))
          .orElse(false);
    }

    public long getLong(String key) {
        return properties
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((long) ktv.getValue()))
          .orElse(0L);
    }

    public int getInt(String key) {
        return properties
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((int) ktv.getValue()))
          .orElse(0);
    }

    public List<String> getList(String key) {
        return properties
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((List<String>) ktv.getValue()))
          .orElse(Collections.emptyList());
    }




}
