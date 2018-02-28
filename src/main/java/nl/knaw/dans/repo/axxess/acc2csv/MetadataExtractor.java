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
import nl.knaw.dans.repo.axxess.core.Codex;
import nl.knaw.dans.repo.axxess.core.DefaultCodex;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;
import nl.knaw.dans.repo.axxess.core.ObjectType;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts data from <a href="http://jackcess.sourceforge.net/apidocs/index.html">com.healthmarketscience.jackcess</a>
 * objects.
 */
public class MetadataExtractor implements Axxess {

    private static void appendProperties(KeyTypeValueMatrix matrix, PropertyMap propMap, String keyPrefix,
                                         Codex codex) {
        for (PropertyMap.Property prop : propMap) {
            DataType dataType = prop.getType();
            Object value = codex.encode(dataType, prop.getValue());
            matrix.add(keyPrefix + prop.getName(), dataType, value);
        }
    }

    /**
     * Get metadata from the given {@link Database} using a {@link DefaultCodex} in a {@link KeyTypeValueMatrix}.
     *
     * @param db the {@link Database} to be questioned
     * @return metadata in a {@link KeyTypeValueMatrix}
     * @throws IOException for read errors
     */
    public KeyTypeValueMatrix getMetadata(Database db) throws IOException {
        return getMetadata(db, new DefaultCodex(null));
    }

    /**
     * Get metadata from the given {@link Database} using the given <{@link Codex} in a {@link KeyTypeValueMatrix}.
     *
     * @param db    the {@link Database} to be questioned
     * @param codex the {@link Codex} to use for translation
     * @return metadata in a {@link KeyTypeValueMatrix}
     * @throws IOException for read errors
     */
    public KeyTypeValueMatrix getMetadata(Database db, Codex codex) throws IOException {
        return getExtractionMetadata(codex)
          .append(getDatabaseMetadata(db, codex))
          .append(getRelationshipMetadata(db, codex))
          .append(getQueryMetadata(db, codex))
          .append(getExtendedTableMetadata(db, codex));
    }

    public KeyTypeValueMatrix getExtractionMetadata(Codex codex) throws IOException {
        return new KeyTypeValueMatrix()
          .add(EM_CONVERSION_DATE, DataType.TEXT, Instant.now().toString())
          .add(EM_AXXESS_VERSION, DataType.TEXT, Axxess.getVersion())
          .add(EM_AXXESS_BUILD, DataType.TEXT, Axxess.getBuild())
          .add(EM_CODEX, DataType.TEXT, codex.getClass().getName())
          .prefixKeys(ObjectType.EXTRACTION_METADATA);
    }

    public KeyTypeValueMatrix getDatabaseMetadata(Database db, Codex codex) throws IOException {
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

        appendProperties(matrix, db.getDatabaseProperties(), DB_DATABASE_PROP, codex);
        appendProperties(matrix, db.getSummaryProperties(), DB_SUMMARY_PROP, codex);
        appendProperties(matrix, db.getUserDefinedProperties(), DB_USER_DEFINED_PROP, codex);

        matrix.add(DB_RELATIONSHIP_COUNT, DataType.INT, relationshipNames.size())
              .add(DB_RELATIONSHIP_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(relationshipNames))
              .add(DB_QUERY_COUNT, DataType.INT, queryNames.size())
              .add(DB_QUERY_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(queryNames))
              .add(DB_TABLE_COUNT, DataType.INT, db.getTableNames().size())
              .add(DB_TABLE_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(db.getTableNames()))
              .prefixKeys(ObjectType.DATABASE);
        return matrix;
    }

    public KeyTypeValueMatrix getTableMetadata(Table table, Codex codex, boolean includeDbName) throws IOException {
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
              .add(TABLE_COLUMN_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(columnNames))
              .add(TABLE_IS_ALLOW_AUTO_NUMBER_INSERT, DataType.BOOLEAN, table.isAllowAutoNumberInsert())
              .add(TABLE_RELATIONSHIP_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(relationshipNames))
              .add(TABLE_INDEX_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(indexNames))
              .add(TABLE_PRIMARY_KEY_INDEX, DataType.TEXT, primaryKeyIndexName);

        appendProperties(matrix, table.getProperties(), TABLE_PROP, codex);
        return matrix;
    }

    public KeyTypeValueMatrix getRelationshipMetadata(Database db, Codex codex) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int relationshipCount = 0;
        for (Relationship relationship : db.getRelationships()) {
            KeyTypeValueMatrix relationshipMatrix = getRelationshipMetadata(relationship, codex);
            relationshipMatrix.prefixKeys(ObjectType.RELATIONSHIP, relationshipCount);
            matrix.append(relationshipMatrix);
            ++relationshipCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getRelationshipMetadata(Relationship relationship, Codex codex) {
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
          .add(R_FROM_COLUMNS, DataType.COMPLEX_TYPE, codex.encodeCollection(fromColumnNames))
          .add(R_TO_TABLE, DataType.TEXT, relationship.getToTable().getName())
          .add(R_TO_COLUMNS, DataType.COMPLEX_TYPE, codex.encodeCollection(toColumnNames))
          .add(R_JOIN_TYPE, DataType.TEXT, relationship.getJoinType());
    }

    public KeyTypeValueMatrix getQueryMetadata(Database db, Codex codex) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int queryCount = 0;
        for (Query query : db.getQueries()) {
            KeyTypeValueMatrix queryMatrix = getQueryMetadata(query, codex);
            queryMatrix.prefixKeys(ObjectType.QUERY, queryCount);
            matrix.append(queryMatrix);
            ++queryCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getQueryMetadata(Query query, Codex codex) {
        return new KeyTypeValueMatrix()
          .add(Q_NAME, DataType.TEXT, query.getName())
          .add(Q_TYPE, DataType.TEXT, query.getType())
          .add(Q_IS_HIDDEN, DataType.BOOLEAN, query.isHidden())
          .add(Q_PARAMETERS, DataType.COMPLEX_TYPE, codex.encodeCollection(query.getParameters()))
          .add(Q_SQL, DataType.TEXT, query.toSQLString().replaceAll("[\r\n]", " "));
    }

    public KeyTypeValueMatrix getExtendedTableMetadata(Database db, Codex codex) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int tableCount = 0;

        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            KeyTypeValueMatrix tableMatrix = getTableMetadata(table, codex, false);
            tableMatrix.prefixKeys(ObjectType.TABLE, tableCount);
            matrix.append(tableMatrix);

            int indexCount = 0;
            for (Index index : table.getIndexes()) {
                KeyTypeValueMatrix indexMatrix = getIndexMetadata(index, codex);
                indexMatrix.prefixKeys(ObjectType.TABLE_INDEX, tableCount, indexCount);
                matrix.append(indexMatrix);
                indexCount++;
            }

            int columnCount = 0;
            for (Column column : table.getColumns()) {
                KeyTypeValueMatrix columnMatrix = getColumnMetadata(column, codex);
                columnMatrix.prefixKeys(ObjectType.TABLE_COLUMN, tableCount, columnCount);
                matrix.append(columnMatrix);
                ++columnCount;
            }
            ++tableCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getIndexMetadata(Index index, Codex codex) throws IOException {
        List<String> columnNames = index.getColumns()
                                        .stream()
                                        .map(Index.Column::getName)
                                        .collect(Collectors.toList());
        String referencedIndexName =
          index.getReferencedIndex() == null ? null : index.getReferencedIndex().getName().replaceAll("^\\.", "");
        return new KeyTypeValueMatrix()
          .add(I_NAME, DataType.TEXT, index.getName().replaceAll("^\\.", ""))
          .add(I_COLUMN_COUNT, DataType.INT, index.getColumnCount())
          .add(I_COLUMN_NAMES, DataType.COMPLEX_TYPE, codex.encodeCollection(columnNames))
          .add(I_REFERENCED_INDEX, DataType.TEXT, referencedIndexName)
          .add(I_IS_FOREIGN_KEY, DataType.BOOLEAN, index.isForeignKey())
          .add(I_IS_PRIMARY_KEY, DataType.BOOLEAN, index.isPrimaryKey())
          .add(I_IS_REQUIRED, DataType.BOOLEAN, index.isRequired())
          .add(I_IS_UNIQUE, DataType.BOOLEAN, index.isUnique())
          .add(I_SHOULD_IGNORE_NULLS, DataType.BOOLEAN, index.shouldIgnoreNulls());
    }

    public KeyTypeValueMatrix getColumnMetadata(Column column, Codex codex) throws IOException {
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

        appendProperties(matrix, column.getProperties(), C_PROP, codex);

        Column vhColumn = column.getVersionHistoryColumn();
        if (vhColumn != null) {
            matrix.add(C_VERSION_HISTORY_COLUMN, DataType.INT, vhColumn.getColumnIndex());
        }
        return matrix;
    }


}
