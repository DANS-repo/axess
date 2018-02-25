package nl.knaw.dans.repo.axxess.csv2acc.xdb;

import com.healthmarketscience.jackcess.DataType;
import nl.knaw.dans.repo.axxess.core.KTV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class XDatabaseObject {

    private List<KTV> ktvLines = new ArrayList<>();

    public List<KTV> getKtvLines() {
        return ktvLines;
    }

    public void addKtv(KTV ktv) {
        ktvLines.add(ktv);
    }

    public List<KTV> getProperties(String startStr) {
        return ktvLines
          .stream()
          .filter(ktv -> ktv.getKey().startsWith(startStr))
          .map(KTV::cloneClean)
          .collect(Collectors.toList());
    }

    public String getString(String key) {
        return ktvLines
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((String) ktv.getValue()))
          .orElse(null);
    }

    public boolean getBool(String key) {
        return ktvLines
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((boolean) ktv.getValue()))
          .orElse(false);
    }

    public long getLong(String key) {
        return ktvLines
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((long) ktv.getValue()))
          .orElse(0L);
    }

    public int getInt(String key) {
        return ktvLines
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((int) ktv.getValue()))
          .orElse(0);
    }

    public DataType getDataType(String key) {
        return DataType.valueOf(getString(key));
    }

    public List<String> getList(String key) {
        return ktvLines
          .stream()
          .filter(ktv -> ktv.getKey().equals(key))
          .findFirst()
          .map(ktv -> ((List<String>) ktv.getValue()))
          .orElse(Collections.emptyList());
    }

    public String[] getStringArray(String key) {
        List<String> list = getList(key);
        String[] strings = new String[list.size()];
        return list.toArray(strings);
    }


}
