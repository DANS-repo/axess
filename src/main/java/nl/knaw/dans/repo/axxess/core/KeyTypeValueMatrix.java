package nl.knaw.dans.repo.axxess.core;

import com.healthmarketscience.jackcess.DataType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyTypeValueMatrix {

    private List<KTV> ktvLines = new ArrayList<>();

    public KeyTypeValueMatrix() {
    }

    public KeyTypeValueMatrix(List<KTV> ktvLines) {
        this.ktvLines = new ArrayList<>(ktvLines);
    }

    public KeyTypeValueMatrix(Reader reader, CSVFormat csvFormat, Codex codex) throws IOException {
        CSVParser parser = new CSVParser(reader, csvFormat);
        for (CSVRecord record : parser) {
            add(record, codex);
        }
    }

    public KeyTypeValueMatrix add(String key, DataType type, Object value) {
        ktvLines.add(new KTV(key, type, value));
        return this;
    }

    public KeyTypeValueMatrix add(CSVRecord record, Codex codex) {
        ktvLines.add(new KTV(record, codex));
        return this;
    }

    public KTV get(ObjectType objectType, Object key, int... indexes) {
        String prefix = objectType.prefix(indexes);
        for (KTV ktv : ktvLines) {
            if (ktv.getPrefix() != null && ktv.getPrefix().equals(prefix)) {
                if (ktv.getKey() != null && ktv.getKey().equals(key)) {
                    return ktv;
                }
            }
        }
        return null;
    }

    public KeyTypeValueMatrix append(KeyTypeValueMatrix ktvMatrix) {
        ktvLines.addAll(ktvMatrix.ktvLines);
        return this;
    }

    public KeyTypeValueMatrix prefixKeys(ObjectType objectType, int... indexes) {
        String prefix = objectType.prefix(indexes);
        ktvLines.forEach(ktv -> ktv.setPrefix(prefix));
        return this;
    }

    public List<KTV> getLines() {
        return ktvLines;
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

    public void print(Appendable out, CSVFormat format, Orientation orientation) throws IOException {
        if (Orientation.VERTICAL.equals(orientation)) {
            printVertical(out, format);
        }
        if (Orientation.HORIZONTAL.equals(orientation)) {
            printHorizontal(out, format);
        }
    }

    public void printVertical(Appendable out, CSVFormat format) throws IOException {
        CSVPrinter printer = new CSVPrinter(out, format);
        for (KTV ktv : ktvLines) {
            printer.printRecord(ktv.getPrefix(), ktv.getKey(), ktv.getType(), ktv.getValue());
        }
    }

    public void printHorizontal(Appendable out, CSVFormat format) throws IOException {
        CSVPrinter printer = new CSVPrinter(out, format);
        printer.printRecord(getPrefixes());
        printer.printRecord(getKeys());
        printer.printRecord(getTypes());
        printer.printRecord(getValues());
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

}
