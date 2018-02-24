package nl.knaw.dans.repo.axxess.csv2acc;

import nl.knaw.dans.repo.axxess.core.Axxess;
import nl.knaw.dans.repo.axxess.core.KTV;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XColumn;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XDatabase;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XIndex;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XQuery;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XRelationship;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Parse database metadata in csv format to the intermediate class structure {@link XDatabase}.
 */
public class MetadataParser {

    private static Logger LOG = LoggerFactory.getLogger(MetadataParser.class);

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
            XIndex xti = null;
            int idxti = -1;

            int recordCount = 0;
            CSVParser parser = new CSVParser(reader, csvFormat);
            for (CSVRecord record : parser) {
                if (record.getRecordNumber() > 1) { // first line is header
                    KTV ktv = new KTV(record);
                    int index = ktv.getFirstIndex();
                    if (ktv.isDatabaseProp()) {
                        xdb.addKtv(ktv);
                    } else if (ktv.isRelationshipProp()) {
                        if (idxr != index) {
                            idxr = index;
                            xr = null;
                        }
                        if (xr == null) {
                            xr = new XRelationship();
                            xdb.addRelationship(xr);
                        }
                        xr.addKtv(ktv);
                    } else if (ktv.isQueryProp()) {
                        if (idxq != index) {
                            idxq = index;
                            xq = null;
                        }
                        if (xq == null) {
                            xq = new XQuery();
                            xdb.addQuery(xq);
                        }
                        xq.addKtv(ktv);
                    } else if (ktv.isTableProp()) {
                        if (idxt != index) {
                            idxt = index;
                            idxti = -1;
                            idxtc = -1;
                            xt = null;
                        }
                        if (xt == null) {
                            xt = new XTable();
                            xdb.addTable(xt);
                        }
                        xt.addKtv(ktv);
                    } else if (ktv.isTableIndexProp()) {
                        int secondIndex = ktv.getSecondIndex();
                        if (idxti != secondIndex) {
                            idxti = secondIndex;
                            xti = null;
                        }
                        if (xti == null) {
                            xti = new XIndex();
                            if (xt == null) {
                                throw new IOException("Bad format: Index without Table metadata");
                            }
                            xt.addIndex(xti);
                        }
                        xti.addKtv(ktv);
                    } else if (ktv.isTableColumnProp()) {
                        int secondIndex = ktv.getSecondIndex();
                        if (idxtc != secondIndex) {
                            idxtc = secondIndex;
                            xtc = null;
                        }
                        if (xtc == null) {
                            xtc = new XColumn();
                            if (xt == null) {
                                throw new IOException("Bad format: Columns without Table metadata");
                            }
                            xt.addColumn(xtc);
                        }
                        xtc.addKtv(ktv);
                    }
                }
                recordCount++;
            }
            LOG.info("Parsed {} records to intermediate class structure. FileFormat={}", recordCount,
              xdb.getString(Axxess.DB_FILE_FORMAT));
            return xdb;
        } finally {
            reader.close();
        }
    }
}
