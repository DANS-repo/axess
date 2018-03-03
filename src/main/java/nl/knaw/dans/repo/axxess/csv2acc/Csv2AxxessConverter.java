package nl.knaw.dans.repo.axxess.csv2acc;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.IndexBuilder;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.RelationshipBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;
import nl.knaw.dans.repo.axxess.core.Axxess;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.Codex;
import nl.knaw.dans.repo.axxess.core.Converter;
import nl.knaw.dans.repo.axxess.core.KTV;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XColumn;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XDatabase;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XIndex;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XRelationship;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XTable;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Csv2AxxessConverter extends Converter<Csv2AxxessConverter> implements Axxess {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "axxess-db-out";

    private static Logger LOG = LoggerFactory.getLogger(Csv2AxxessConverter.class);

    private Charset sourceEncoding;
    private Database.FileFormat targetFormat;
    private boolean includeIndexes = true;
    private boolean includeRelationships = true;
    private boolean autoNumberColumns;

    private String currentDatabaseFormat = null;
    private String currentTableName = null;
    private String currentColumnName = null;
    private String currentIndexName = null;
    private String currentRelationshipName = null;

    private int metadataFilenameCount;

    public Csv2AxxessConverter withSourceEncoding(Charset sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
        return this;
    }

    public Csv2AxxessConverter withSourceEncoding(String sourceEncoding) {
        if (sourceEncoding == null || sourceEncoding.isEmpty()) {
            return this;
        } else {
            return withSourceEncoding(Charset.forName(sourceEncoding));
        }
    }

    public Charset getSourceEncoding() {
        if (sourceEncoding == null) {
            sourceEncoding = Charset.forName("UTF-8");
        }
        return sourceEncoding;
    }

    public Csv2AxxessConverter withTargetDatabaseFileFormat(Database.FileFormat format) {
        targetFormat = format;
        return this;
    }

    public Csv2AxxessConverter withTargetDatabaseFileFormat(String dbFormat) {
        if (dbFormat == null || dbFormat.isEmpty()) {
            return this;
        } else {
            return withTargetDatabaseFileFormat(Database.FileFormat.valueOf(dbFormat));
        }
    }

    public Csv2AxxessConverter setIncludeIndexes(boolean include) {
        includeIndexes = include;
        if (!includeIndexes) {
            includeRelationships = false;
        }
        return this;
    }

    public Csv2AxxessConverter setIncludeRelationships(boolean include) {
        includeRelationships = include;
        if (includeRelationships) {
            includeIndexes = true;
        }
        return this;
    }

    public Csv2AxxessConverter setAutoNumberColumns(boolean autoNumber) {
        autoNumberColumns = autoNumber;
        return this;
    }

    @Override
    public String getDefaultOutputDirectory() {
        return DEFAULT_OUTPUT_DIRECTORY;
    }

    public int getMetadataFilenameCount() {
        return metadataFilenameCount;
    }

    @Override
    public List<File> convert(File file) throws AxxessException {
        reset();
        List<File> resultFiles = new ArrayList<>();
        try {
            convert(file.getAbsoluteFile(), getTargetDirectory(), resultFiles);
        } catch (IOException e) {
            throw new AxxessException("Exception during conversion of " + file.getAbsolutePath(), e);
        }
        return resultFiles;
    }

    @Override
    protected void reset() {
        super.reset();
        metadataFilenameCount = 0;
    }

    private void convert(File file, File targetDirectory, List<File> resultFiles) throws AxxessException, IOException {
        if (!file.exists()) {
            LOG.warn("File not found: {}", file);
            return;
        }
        if (file.isDirectory()) {
            File td;
            if (file.getCanonicalPath().equals(targetDirectory.getCanonicalPath())) {
                td = targetDirectory;
            } else {
                td = new File(targetDirectory, file.getName());
            }
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                convert(f, td, resultFiles);
            }
        } else if (getFilenameComposer().isMetadataFilename(file)) {
            try {
                builtFromFile(file, getTargetFileFormat(), targetDirectory.getParentFile(), resultFiles);
                metadataFilenameCount += 1;
            } catch (Exception e) {
                LOG.error(errorContext() + " file: " + file.getAbsolutePath(), e);
                reportError("File: " + file.getAbsolutePath(), e);
            }
        } else {
            LOG.debug("File is not a metadata file: {}", file);
        }
    }

    private String errorContext() {
        String context = "Context: ";
        if (currentDatabaseFormat != null) {
            context += " Format=" + currentDatabaseFormat;
        }
        if (currentTableName != null) {
            context += ", Table=" + currentTableName;
        }
        if (currentColumnName != null) {
            context += ", Column=" + currentColumnName;
        }
        if (currentIndexName != null) {
            context += ", Index=" + currentIndexName;
        }
        if (currentRelationshipName != null) {
            context += ", Relationship=" + currentRelationshipName;
        }
        return context;
    }

    public void builtFromFile(File mdFile, Database.FileFormat targetFormat, File targetDirectory,
                              List<File> resultFiles) throws IOException {
        assert targetDirectory.exists() || targetDirectory.mkdirs();
        LOG.info("Trying to parse {}, encoding={}", mdFile.getAbsolutePath(), getSourceEncoding());
        XDatabase xdb = MetadataParser.parse(mdFile, getSourceEncoding(), getCSVFormat(), getCodex());
        currentDatabaseFormat = xdb.getString(DB_FILE_FORMAT);

        File targetFile = new File(targetDirectory,
          getFilenameComposer().getNewDatabaseFilename(mdFile.getName(), getTargetFileFormat().getFileExtension()));

        File targetDir = targetFile.getParentFile();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        if (!targetFile.exists()) {
            targetFile.createNewFile();
        }
        LOG.info("Trying to built database with format {} at {}", targetFormat, targetFile.getAbsolutePath());
        Database db = null;
        try {
            DatabaseBuilder databaseBuilder = new DatabaseBuilder()
              .setFile(targetFile)
              .setFileFormat(targetFormat);

            // Not putting database properties - Access will not always open databases with these properties set.

            for (KTV ktv : xdb.getProperties(DB_SUMMARY_PROP)) {
                databaseBuilder.putDatabaseProperty(ktv.getKey(), ktv.getType(), ktv.getValue());
            }
            for (KTV ktv : xdb.getProperties(DB_USER_DEFINED_PROP)) {
                databaseBuilder.putDatabaseProperty(ktv.getKey(), ktv.getType(), ktv.getValue());
            }
            db = databaseBuilder.create();

            for (XTable xt : xdb.getTables()) {
                currentTableName = xt.getString(TABLE_NAME);
                TableBuilder tableBuilder = new TableBuilder(currentTableName);
                for (KTV ktv : xt.getProperties(TABLE_PROP)) {
                    tableBuilder.putProperty(ktv.getKey(), ktv.getType(), ktv.getValue());
                }

                if (includeIndexes) {
                    for (XIndex xi : xt.getIndexes()) {
                        currentIndexName = xi.getString(I_NAME);
                        IndexBuilder indexBuilder = new IndexBuilder(currentIndexName)
                          .addColumns(xi.getStringArray(I_COLUMN_NAMES));
                        if (xi.getBool(I_IS_PRIMARY_KEY)) {
                            indexBuilder.setPrimaryKey();
                        }
                        if (xi.getBool(I_IS_REQUIRED)) {
                            indexBuilder.setRequired();
                        }
                        if (xi.getBool(I_IS_UNIQUE)) {
                            indexBuilder.setUnique();
                        }
                        if (xi.getBool(I_SHOULD_IGNORE_NULLS)) {
                            indexBuilder.setIgnoreNulls();
                        }
                        tableBuilder.addIndex(indexBuilder);
                        LOG.debug("Build index '{}' on table '{}'", currentIndexName, currentTableName);
                        currentIndexName = null;
                    }
                }

                for (XColumn xc : xt.getColumns()) {
                    currentColumnName = xc.getString(C_NAME);

                    int length = xc.getInt(C_LENGTH);
                    DataType dataType = xc.getDataType(C_DATA_TYPE);
                    if (xdb.getString(DB_FILE_FORMAT).startsWith("V1997") && dataType == DataType.TEXT) {
                        // See DataType:392 -> toUnitSize, return(size / getUnitSize());
                        // unitSize of TEXT = 2.
                        length = length * DataType.TEXT.getUnitSize();
                    }
                    if (dataType == DataType.NUMERIC) {
                        // 	A decimal number uses 17 bytes of disk space.
                        length = 17;
                    }

                    ColumnBuilder columnBuilder = new ColumnBuilder(currentColumnName)
                      .setType(xc.getDataType(C_DATA_TYPE))
                      .setLength(length)
                      //.setLengthInUnits(xc.getInt(C_LENGTH_IN_UNITS)) // is this calculated? + see length exceptions
                      .setScale(xc.getByte(C_SCALE))
                      .setPrecision(xc.getByte(C_PRECISION))
                      .setCalculated(xc.getBool(C_IS_CALCULATED))
                      .setCompressedUnicode(xc.getBool(C_IS_COMPRESSED_UNICODE))
                      .setHyperlink(xc.getBool(C_IS_HYPERLINK));
                    if (autoNumberColumns) {
                        columnBuilder.setAutoNumber(xc.getBool(C_IS_AUTO_NUMBER));
                    } else {
                        columnBuilder.setAutoNumber(false);
                    }
                    for (KTV ktv : xc.getProperties(C_PROP)) {
                        columnBuilder.putProperty(ktv.getKey(), ktv.getType(), ktv.getValue());
                    }
                    tableBuilder.addColumn(columnBuilder);
                    //LOG.debug("Build column '{}' on table '{}'", columnName, tableName);
                    currentColumnName = null;
                }
                Table table = tableBuilder.toTable(db);
                LOG.debug("Finished building table '{}'", currentTableName);

                File tableDataFile = getFilenameComposer().getTableDataFileFor(mdFile, currentTableName);
                parseTableData(tableDataFile, table, xt, getCodex());
                currentTableName = null;
            }

            if (includeRelationships) {
                for (XRelationship xr : xdb.getRelationships()) {
                    currentRelationshipName = xr.getString(R_NAME);
                    Iterator<String> fromColumns = xr.getList(R_FROM_COLUMNS).iterator();
                    Iterator<String> toColumns = xr.getList(R_TO_COLUMNS).iterator();

                    RelationshipBuilder relBuilder =
                      new RelationshipBuilder(xr.getString(R_FROM_TABLE), xr.getString(R_TO_TABLE))
                        .setName(currentRelationshipName)
                        .setJoinType(Relationship.JoinType.valueOf(xr.getString(R_JOIN_TYPE)));
                    while (fromColumns.hasNext() && toColumns.hasNext()) {
                        relBuilder.addColumns(fromColumns.next(), toColumns.next());
                    }
                    if (xr.getBool(R_CASCADE_DELETES)) {
                        relBuilder.setCascadeDeletes();
                    }
                    if (xr.getBool(R_CASCADE_NULL_ON_DELETETES)) {
                        relBuilder.setCascadeNullOnDelete();
                    }
                    if (xr.getBool(R_CASCADE_UPDATES)) {
                        relBuilder.setCascadeUpdates();
                    }
                    if (xr.getBool(R_HAS_REFERENTIAL_INTEGRITY)) {
                        relBuilder.setReferentialIntegrity();
                    }
                    relBuilder.toRelationship(db);
                    LOG.debug("Finished building relationship '{}'", currentRelationshipName);
                    currentRelationshipName = null;
                }
            }

            // building queries not supported:
            // "jackcess can read query definitions, that is all."
            // https://sourceforge.net/p/jackcess/discussion/456473/thread/8da9d7a3/

            db.setAllowAutoNumberInsert(xdb.getBool(DB_IS_ALLOW_AUTO_NUMBER_INSERT));
            db.setColumnOrder(Table.ColumnOrder.valueOf(xdb.getString(DB_COLUMN_ORDER)));

        } finally {
            if (db != null) {
                db.close();
            }
        }
        resultFiles.add(targetFile);
        LOG.info("Finished building database '{}'", targetFile);
        currentDatabaseFormat = null;
        increaseDbCount();

        if (isIncludingManifest()) {
            addManifest(resultFiles);
        }
    }

    private Database.FileFormat getTargetFileFormat() {
        if (targetFormat == null) {
            targetFormat = Database.FileFormat.V2010;
        }
        return targetFormat;
    }

    private void parseTableData(File tableDataFile, Table table, XTable xt, Codex codex) throws IOException {
        LOG.debug("Trying to parse table data from {}", tableDataFile);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(tableDataFile), getSourceEncoding());
        CSVParser parser = new CSVParser(reader, getCSVFormat());
        int recordCount = 0;
        for (CSVRecord record : parser) {
            if (record.getRecordNumber() > 1) { // first line is column header
                Object[] data = new Object[xt.getColumns().size()];
                Iterator<String> iterator = record.iterator();
                for (int i = 0; i < xt.getColumns().size(); i++) {
                    XColumn xc = xt.getColumns().get(i);
                    DataType type = xc.getDataType(C_DATA_TYPE);
                    data[i] = codex.decode(type, iterator.next());
                }
                table.addRow(data);
                recordCount++;
            }
        }
        LOG.debug("Finished adding {} rows to table '{}'", recordCount, table.getName());
    }

}
