package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;
import nl.knaw.dans.repo.axxess.core.Axxess;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.Codex;
import nl.knaw.dans.repo.axxess.core.Extractor;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;
import nl.knaw.dans.repo.axxess.core.ObjectType;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts data from <a href="http://jackcess.sourceforge.net/apidocs/index.html">com.healthmarketscience.jackcess</a>
 * objects.
 */
public class MetadataExtractor extends Extractor<MetadataExtractor> implements Axxess {

    private static void appendProperties(KeyTypeValueMatrix matrix, PropertyMap propMap, String keyPrefix,
                                         Codex codex) {
        for (PropertyMap.Property prop : propMap) {
            DataType dataType = prop.getType();
            Object value = codex.encode(dataType, prop.getValue());
            matrix.add(keyPrefix + prop.getName(), dataType, value);
        }
    }

    /**
     * Writes metadata of the given database in vertical orientation in the given {@link CSVFormat} as a
     * <code>n x 4</code> <code>.csv</code> file.
     *
     * @param db the database
     * @return the newly created .csv file
     * @throws IOException     signals a failure in reading or writing
     * @throws AxxessException signals an insoluble conflict
     */
    public File writeDatabaseMetadata(Database db)
      throws IOException, AxxessException {
        String dirName = getFilenameComposer().getCsvDirectoryName(db);
        String filename = getFilenameComposer().getMetadataFilename(db);
        File file = buildPaths(dirName, filename);
        if (file.exists()) {
            throw new AxxessException("File exists: " + file.getAbsolutePath());
        }
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), getTargetCharset())) {
            getMetadata(db).printVertical(osw, buildVerticalFormat());
            LOG.debug("Wrote metadata: {}", file.getName());
            return file;
        }
    }

    private CSVFormat buildVerticalFormat() {
        CSVFormat format = getCSVFormat();
        if (format.getHeader() == null || format.getHeader().length != 3) {
            format = format.withHeader("Obj", "Key", "Type", "Value");
        }
        return format;
    }

    /**
     * Get metadata from the given {@link Database} in a {@link KeyTypeValueMatrix}.
     *
     * @param db the {@link Database} to be questioned
     * @return metadata in a {@link KeyTypeValueMatrix}
     * @throws IOException for read errors
     */
    public KeyTypeValueMatrix getMetadata(Database db) throws IOException {
        return getExtractionMetadata()
          .append(getDatabaseMetadata(db))
          .append(getRelationshipMetadata(db))
          .append(getQueryMetadata(db))
          .append(getExtendedTableMetadata(db));
    }

    public KeyTypeValueMatrix getExtractionMetadata() {
        return new KeyTypeValueMatrix()
          .add(EM_CONVERSION_DATE, DataType.TEXT, Instant.now().toString())
          .add(EM_OS_NAME, DataType.TEXT, System.getProperty("os.name"))
          .add(EM_OS_ARCH, DataType.TEXT, System.getProperty("os.arch"))
          .add(EM_OS_VERSION, DataType.TEXT, System.getProperty("os.version"))
          .add(EM_AXXESS_VERSION, DataType.TEXT, Axxess.getProperty("axxess.version"))
          .add(EM_AXXESS_BUILD, DataType.TEXT, Axxess.getProperty("axxess.build"))
          .add(EM_AXXESS_URL, DataType.TEXT, Axxess.getProperty("axxess.url"))
          .add(EM_CODEX, DataType.TEXT, getCodex().getClass().getName())
          .prefixKeys(ObjectType.EXTRACTION_METADATA);
    }

    public KeyTypeValueMatrix getDatabaseMetadata(Database db) throws IOException {
        List<String> relationshipNames =
          db.getRelationships().stream().map(Relationship::getName).collect(Collectors.toList());
        List<String> queryNames = db.getQueries().stream().map(Query::getName).collect(Collectors.toList());
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add(DB_FILENAME, DataType.TEXT, db.getFile().getName())
          .add(DB_PASSWORD, DataType.TEXT, db.getDatabasePassword())
          .add(DB_FILE_FORMAT, DataType.TEXT, db.getFileFormat())
          .add(DB_CHARSET, DataType.TEXT, db.getCharset())
          .add(DB_IS_ALLOW_AUTO_NUMBER_INSERT, DataType.BOOLEAN, db.isAllowAutoNumberInsert())
          .add(DB_COLUMN_ORDER, DataType.TEXT, db.getColumnOrder());

        appendProperties(matrix, db.getDatabaseProperties(), DB_DATABASE_PROP, getCodex());
        appendProperties(matrix, db.getSummaryProperties(), DB_SUMMARY_PROP, getCodex());
        appendProperties(matrix, db.getUserDefinedProperties(), DB_USER_DEFINED_PROP, getCodex());

        matrix.add(DB_RELATIONSHIP_COUNT, DataType.INT, relationshipNames.size())
              .add(DB_RELATIONSHIP_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(relationshipNames))
              .add(DB_QUERY_COUNT, DataType.INT, queryNames.size())
              .add(DB_QUERY_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(queryNames))
              .add(DB_TABLE_COUNT, DataType.INT, db.getTableNames().size())
              .add(DB_TABLE_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(db.getTableNames()))
              .prefixKeys(ObjectType.DATABASE);
        return matrix;
    }

    public KeyTypeValueMatrix getTableMetadata(Table table, boolean includeDbName) throws IOException {
        List<String> columnNames = table.getColumns()
                                        .stream()
                                        .map(Column::getName)
                                        .collect(Collectors.toList());
        List<String> relationshipNames = table.getDatabase().getRelationships(table)
                                              .stream()
                                              .map(Relationship::getName)
                                              .collect(Collectors.toList());
        List<String> indexNames = table.getIndexes()
                                       .stream()
                                       .map(Index::getName)
                                       .collect(Collectors.toList());
        String primaryKeyIndexName = table.getIndexes()
                                          .stream()
                                          .filter(Index::isPrimaryKey)
                                          .map(Index::getName)
                                          .findFirst()
                                          .orElse(null);
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add(TABLE_NAME, DataType.TEXT, table.getName());
        if (includeDbName) {
            matrix.add(TABLE_DATABASE_NAME, DataType.TEXT, table.getDatabase().getFile().getName());
        }
        matrix.add(TABLE_ROW_COUNT, DataType.INT, table.getRowCount())
              .add(TABLE_COLUMN_COUNT, DataType.INT, table.getColumnCount())
              .add(TABLE_COLUMN_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(columnNames))
              .add(TABLE_IS_ALLOW_AUTO_NUMBER_INSERT, DataType.BOOLEAN, table.isAllowAutoNumberInsert())
              .add(TABLE_RELATIONSHIP_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(relationshipNames))
              .add(TABLE_INDEX_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(indexNames))
              .add(TABLE_PRIMARY_KEY_INDEX, DataType.TEXT, primaryKeyIndexName);

        appendProperties(matrix, table.getProperties(), TABLE_PROP, getCodex());
        return matrix;
    }

    public KeyTypeValueMatrix getRelationshipMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int relationshipCount = 0;
        for (Relationship relationship : db.getRelationships()) {
            KeyTypeValueMatrix relationshipMatrix = getRelationshipMetadata(relationship);
            relationshipMatrix.prefixKeys(ObjectType.RELATIONSHIP, relationshipCount);
            matrix.append(relationshipMatrix);
            ++relationshipCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getRelationshipMetadata(Relationship relationship) {
        List<String> fromColumnNames =
          relationship.getFromColumns().stream().map(Column::getName).collect(Collectors.toList());
        List<String> toColumnNames =
          relationship.getToColumns().stream().map(Column::getName).collect(Collectors.toList());
        return new KeyTypeValueMatrix()
          .add(R_NAME, DataType.TEXT, relationship.getName())
          .add(R_CASCADE_DELETES, DataType.BOOLEAN, relationship.cascadeDeletes())
          .add(R_CASCADE_NULL_ON_DELETETES, DataType.BOOLEAN, relationship.cascadeNullOnDelete())
          .add(R_CASCADE_UPDATES, DataType.BOOLEAN, relationship.cascadeUpdates())
          .add(R_HAS_REFERENTIAL_INTEGRITY, DataType.BOOLEAN, relationship.hasReferentialIntegrity())
          .add(R_IS_LEFT_OUTER_JOIN, DataType.BOOLEAN, relationship.isLeftOuterJoin())
          .add(R_IS_ONE_TO_ONE, DataType.BOOLEAN, relationship.isOneToOne())
          .add(R_IS_RIGHT_OUTER_JOIN, DataType.BOOLEAN, relationship.isRightOuterJoin())
          .add(R_FROM_TABLE, DataType.TEXT, relationship.getFromTable().getName())
          .add(R_FROM_COLUMNS, DataType.COMPLEX_TYPE, getCodex().encodeCollection(fromColumnNames))
          .add(R_TO_TABLE, DataType.TEXT, relationship.getToTable().getName())
          .add(R_TO_COLUMNS, DataType.COMPLEX_TYPE, getCodex().encodeCollection(toColumnNames))
          .add(R_JOIN_TYPE, DataType.TEXT, relationship.getJoinType());
    }

    public KeyTypeValueMatrix getQueryMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int queryCount = 0;
        for (Query query : db.getQueries()) {
            KeyTypeValueMatrix queryMatrix = getQueryMetadata(query);
            queryMatrix.prefixKeys(ObjectType.QUERY, queryCount);
            matrix.append(queryMatrix);
            ++queryCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getQueryMetadata(Query query) {
        return new KeyTypeValueMatrix()
          .add(Q_NAME, DataType.TEXT, query.getName())
          .add(Q_TYPE, DataType.TEXT, query.getType())
          .add(Q_IS_HIDDEN, DataType.BOOLEAN, query.isHidden())
          .add(Q_PARAMETERS, DataType.COMPLEX_TYPE, getCodex().encodeCollection(query.getParameters()))
          .add(Q_SQL, DataType.TEXT, query.toSQLString().replaceAll("[\r\n]", " "));
    }

    public KeyTypeValueMatrix getExtendedTableMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int tableCount = 0;

        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            KeyTypeValueMatrix tableMatrix = getTableMetadata(table, false);
            tableMatrix.prefixKeys(ObjectType.TABLE, tableCount);
            matrix.append(tableMatrix);

            int indexCount = 0;
            for (Index index : table.getIndexes()) {
                KeyTypeValueMatrix indexMatrix = getIndexMetadata(index);
                indexMatrix.prefixKeys(ObjectType.TABLE_INDEX, tableCount, indexCount);
                matrix.append(indexMatrix);
                indexCount++;
            }

            int columnCount = 0;
            for (Column column : table.getColumns()) {
                KeyTypeValueMatrix columnMatrix = getColumnMetadata(column);
                columnMatrix.prefixKeys(ObjectType.TABLE_COLUMN, tableCount, columnCount);
                matrix.append(columnMatrix);
                ++columnCount;
            }
            ++tableCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getIndexMetadata(Index index) throws IOException {
        List<String> columnNames = index.getColumns()
                                        .stream()
                                        .map(Index.Column::getName)
                                        .collect(Collectors.toList());
        String referencedIndexName =
          index.getReferencedIndex() == null ? null : index.getReferencedIndex().getName().replaceAll("^\\.", "");
        return new KeyTypeValueMatrix()
          .add(I_NAME, DataType.TEXT, index.getName().replaceAll("^\\.", ""))
          .add(I_COLUMN_COUNT, DataType.INT, index.getColumnCount())
          .add(I_COLUMN_NAMES, DataType.COMPLEX_TYPE, getCodex().encodeCollection(columnNames))
          .add(I_REFERENCED_INDEX, DataType.TEXT, referencedIndexName)
          .add(I_IS_FOREIGN_KEY, DataType.BOOLEAN, index.isForeignKey())
          .add(I_IS_PRIMARY_KEY, DataType.BOOLEAN, index.isPrimaryKey())
          .add(I_IS_REQUIRED, DataType.BOOLEAN, index.isRequired())
          .add(I_IS_UNIQUE, DataType.BOOLEAN, index.isUnique())
          .add(I_SHOULD_IGNORE_NULLS, DataType.BOOLEAN, index.shouldIgnoreNulls());
    }

    public KeyTypeValueMatrix getColumnMetadata(Column column) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add(C_NAME, DataType.TEXT, column.getName())
          .add(C_INDEX, DataType.INT, column.getColumnIndex())
          .add(C_DATA_TYPE, DataType.TEXT, column.getType())
          .add(C_LENGTH, DataType.INT, column.getLength())
          .add(C_LENGTH_IN_UNITS, DataType.INT, column.getLengthInUnits())
          .add(C_SCALE, DataType.BYTE, column.getScale())
          .add(C_PRECISION, DataType.BYTE, column.getPrecision())
          //.add(C_SQL_TYPE, DataType.INT, column.getSQLType())
          .add(C_IS_APPEND_ONLY, DataType.BOOLEAN, column.isAppendOnly())
          .add(C_IS_AUTO_NUMBER, DataType.BOOLEAN, column.isAutoNumber())
          .add(C_IS_CALCULATED, DataType.BOOLEAN, column.isCalculated())
          .add(C_IS_COMPRESSED_UNICODE, DataType.BOOLEAN, column.isCompressedUnicode())
          .add(C_IS_HYPERLINK, DataType.BOOLEAN, column.isHyperlink())
          .add(C_IS_VARIABLE_LENGTH, DataType.BOOLEAN, column.isVariableLength());

        appendProperties(matrix, column.getProperties(), C_PROP, getCodex());

        Column vhColumn = column.getVersionHistoryColumn();
        if (vhColumn != null) {
            matrix.add(C_VERSION_HISTORY_COLUMN, DataType.INT, vhColumn.getColumnIndex());
        }
        return matrix;
    }


}
