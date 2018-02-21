package nl.knaw.dans.repo.axxess.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class MetadataParser {

    public static XDatabase parse(String file) throws IOException {
        return parse(file, Charset.forName("UTF-8"), CSVFormat.RFC4180);
    }

    public static XDatabase parse(String file, Charset charset, CSVFormat csvFormat) throws IOException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset);
        return parse(reader, csvFormat);
    }

    public static XDatabase parse(File file) throws IOException {
        return parse(file, Charset.forName("UTF-8"), CSVFormat.RFC4180);
    }

    public static XDatabase parse(File file, Charset charset, CSVFormat csvFormat) throws IOException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset);
        return parse(reader, csvFormat);
    }

    public static XDatabase parse(Reader reader) throws IOException {
        return parse(reader, CSVFormat.RFC4180);
    }

    public static XDatabase parse(Reader reader, CSVFormat csvFormat) throws IOException {
        try {
            XDatabase xdb = new XDatabase();
            XRelationship xr = null;
            int idxr = -1;
            XQuery xq = null;
            int idxq = -1;
            XTable xt = null;
            int idxt = -1;
            XColumn xtc = null;
            int idxtc = -1;
            CSVParser parser = new CSVParser(reader, csvFormat);
            for (CSVRecord record : parser) {
                if (record.getRecordNumber() > 1) { // first line is header
                    KTV ktv = new KTV(record);
                    int index = ktv.getFirstIndex();
                    if (ktv.isDatabaseProp()) {
                        xdb.addProperty(ktv);
                    } else if (ktv.isRelationshipProp()) {
                        if (idxr != index) {
                            idxr = index;
                            xr = null;
                        }
                        if (xr == null) {
                            xr = new XRelationship();
                            xdb.addRelationship(xr);
                        }
                        xr.addProperty(ktv);
                    } else if (ktv.isQueryProp()) {
                        if (idxq != index) {
                            idxq = index;
                            xq = null;
                        }
                        if (xq == null) {
                            xq = new XQuery();
                            xdb.addQuery(xq);
                        }
                        xq.addProperty(ktv);
                    } else if (ktv.isTableProp()) {
                        if (idxt != index) {
                            idxt = index;
                            xt = null;
                        }
                        if (xt == null) {
                            xt = new XTable();
                            xdb.addTable(xt);
                        }
                        xt.addProperty(ktv);
                    } else if (ktv.isTableColumnProp()) {
                        int secondIndex = ktv.getSecondIndex();
                        if (idxtc != secondIndex) {
                            idxtc = secondIndex;
                            xtc = null;
                        }
                        if (xtc == null) {
                            xtc = new XColumn();
                            if (xt == null) {
                                throw new RuntimeException("Bad format");
                            }
                            xt.addColumn(xtc);
                        }
                        xtc.addProperty(ktv);
                    }
                }
            }
            return xdb;
        } finally {
            reader.close();
        }
    }
}
