package nl.knaw.dans.repo.ingress.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyTypeValueMatrix {

  public enum Orientation {
    HORIZONTAL,
    VERTICAL
  }

  private List<KTV> ktvLines = new ArrayList<>();

  public KeyTypeValueMatrix add(Object key, Object type, Object value) {
    ktvLines.add(new KTV(key, type, value));
    return this;
  }

  public void append(KeyTypeValueMatrix ktvMatrix) {
    ktvLines.addAll(ktvMatrix.ktvLines);
  }

  public void print(Appendable out, Orientation orientation) throws IOException {
    if (Orientation.VERTICAL.equals(orientation)) printVertical(out);
    if (Orientation.HORIZONTAL.equals(orientation)) printHorizontal(out);
  }

  public void prefixKeys(String prefix) {
    ktvLines.forEach(ktv -> ktv.prefixKey(prefix));
  }

  public void printVertical(Appendable out) throws IOException {
    CSVFormat format = CSVFormat.RFC4180.withHeader("Key", "Type", "Value");
    CSVPrinter printer = null;
    try {
      printer = new CSVPrinter(out, format);
      for (KTV ktv : ktvLines) {
        printer.printRecord(ktv.getKey(), ktv.getType(), ktv.getValue());
      }
    } finally {
      if (printer != null) {
        printer.close();
      }
    }
  }

  public void printHorizontal(Appendable out) throws IOException {
    CSVFormat format = CSVFormat.RFC4180.withFirstRecordAsHeader();
    CSVPrinter printer = null;
    try {
      printer = new CSVPrinter(out, format);
      printer.printRecord(getKeys());
      printer.printRecord(getTypes());
      printer.printRecord(getValues());
    } finally {
      if (printer != null) {
        printer.close();
      }
    }
  }

  public List<Object> getKeys() {
    return ktvLines.stream().map(KTV::getKey).collect(Collectors.toList());
  }

  public List<Object> getTypes() {
    return ktvLines.stream().map(KTV::getType).collect(Collectors.toList());
  }

  public List<Object> getValues() {
    return ktvLines.stream().map(KTV::getValue).collect(Collectors.toList());
  }

}
