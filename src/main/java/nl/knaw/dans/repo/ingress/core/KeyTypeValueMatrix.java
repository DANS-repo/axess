package nl.knaw.dans.repo.ingress.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyTypeValueMatrix {

    private List<KTV> ktvLines = new ArrayList<>();

    public KeyTypeValueMatrix add(Object key, Object type, Object value) {
        ktvLines.add(new KTV(key, type, value));
        return this;
    }

    public KeyTypeValueMatrix append(KeyTypeValueMatrix ktvMatrix) {
        ktvLines.addAll(ktvMatrix.ktvLines);
        return this;
    }

    public KeyTypeValueMatrix prefixKeys(String prefix) {
        ktvLines.forEach(ktv -> ktv.setPrefix(prefix));
        return this;
    }

    public void print(Appendable out, CSVFormat format, Orientation orientation) throws IOException {
        if (Orientation.VERTICAL.equals(orientation)) {
            printVertical(out, format);
        }
        if (Orientation.HORIZONTAL.equals(orientation)) {
            printHorizontal(out, format);
        }
    }

    public List<String> getPrefixes() {
        return ktvLines.stream().map(KTV::getPrefix).collect(Collectors.toList());
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

    public void printVertical(Appendable out, CSVFormat format) throws IOException {
        CSVPrinter printer = null;
        try {
            printer = new CSVPrinter(out, format);
            for (KTV ktv : ktvLines) {
                printer.printRecord(ktv.getPrefix(), ktv.getKey(), ktv.getType(), ktv.getValue());
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public void printHorizontal(Appendable out, CSVFormat format) throws IOException {
        CSVPrinter printer = null;
        try {
            printer = new CSVPrinter(out, format);
            printer.printRecord(getPrefixes());
            printer.printRecord(getKeys());
            printer.printRecord(getTypes());
            printer.printRecord(getValues());
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
