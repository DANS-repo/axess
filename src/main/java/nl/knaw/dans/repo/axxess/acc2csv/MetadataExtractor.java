package nl.knaw.dans.repo.axxess.acc2csv;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;
import nl.knaw.dans.repo.axxess.core.KTV;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nl.knaw.dans.repo.axxess.core.ObjectType.DATABASE;
import static nl.knaw.dans.repo.axxess.core.ObjectType.QUERY;
import static nl.knaw.dans.repo.axxess.core.ObjectType.RELATIONSHIP;
import static nl.knaw.dans.repo.axxess.core.ObjectType.TABLE;
import static nl.knaw.dans.repo.axxess.core.ObjectType.TABLE_COLUMN;

public class MetadataExtractor {

    private static final String K_BUILD = "Build";
    private static final List<String> EXCLUDED_COLUMN_PROPERTIES = Arrays.asList(
      "AggregateType",
      "BoundColumn",
      "ColumnCount",
      "ColumnHeads",
      "ColumnHidden",
      "ColumnOrder",
      "ColumnWidth",
      "ColumnWidths",
      "CurrencyLCID",
      "DecimalPlaces",
      "DisplayControl",
      "IMEMode",
      "IMESentenceMode",
      "ListRows",
      "ListWidth",
      "ResultType",
      "ShowDatePicker",
      "ShowOnlyRowSourceValues",
      "TextAlign"

    );

    public KeyTypeValueMatrix getMetadata(Database db, boolean extended) throws IOException {
        KeyTypeValueMatrix matrix = getDatabaseMetadata(db)
          .append(getRelationshipMetadata(db))
          .append(getQueryMetadata(db));
        if (extended) {
            matrix.append(getExtendedTableMetadata(db));
        } else {
            matrix.append(getTableMetadata(db));
        }
        return matrix;
    }

    public KeyTypeValueMatrix getDatabaseMetadata(Database db) throws IOException {
        List<String> relationshipNames =
          db.getRelationships().stream().map(Relationship::getName).collect(Collectors.toList());
        List<String> queryNames = db.getQueries().stream().map(Query::getName).collect(Collectors.toList());
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add("Filename", DataType.TEXT, db.getFile().getName())
          .add("Password", DataType.TEXT, db.getDatabasePassword())
          .add("File format", DataType.TEXT, db.getFileFormat())
          .add("Charset", DataType.TEXT, db.getCharset());

        PropertyMap propertyMap = db.getDatabaseProperties();
        PropertyMap.Property accessVersion = propertyMap.get(PropertyMap.ACCESS_VERSION_PROP);
        if (accessVersion != null) {
            matrix.add(PropertyMap.ACCESS_VERSION_PROP, accessVersion.getType(), accessVersion.getValue());
        }
        PropertyMap.Property build = propertyMap.get(K_BUILD);
        if (build != null) {
            matrix.add(K_BUILD, build.getType(), build.getValue());
        }
        for (PropertyMap.Property prop : db.getSummaryProperties()) {
            matrix.add("(Summary) " + prop.getName(), prop.getType(), prop.getValue());
        }
        for (PropertyMap.Property prop : db.getUserDefinedProperties()) {
            matrix.add("(User defined) " + prop.getName(), prop.getType(), prop.getValue());
        }
        matrix.add("Relationship count", DataType.INT, relationshipNames.size())
              .add("Relationship names", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, relationshipNames))
              .add("Query count", DataType.INT, queryNames.size())
              .add("Query names", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, queryNames))
              .add("Table count", DataType.INT, db.getTableNames().size())
              .add("Table names", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, db.getTableNames()))
              .prefixKeys(DATABASE);
        return matrix;
    }

    public KeyTypeValueMatrix getTableMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int tableCount = 0;
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            KeyTypeValueMatrix tableMatrix = getTableMetadata(table, false);
            tableMatrix.prefixKeys(TABLE, tableCount);
            matrix.append(tableMatrix);
            ++tableCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getTableMetadata(Table table, boolean includeDbName) throws IOException {
        List<String> tableNames = table.getColumns()
                                       .stream()
                                       .map(Column::getName)
                                       .collect(Collectors.toList());
        List<String> relationshipNames = table.getDatabase().getRelationships(table)
                                              .stream()
                                              .map(Relationship::getName)
                                              .collect(Collectors.toList());
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add("Table name", DataType.TEXT, table.getName());
        if (includeDbName) {
            matrix.add("Database name", DataType.TEXT, table.getDatabase().getFile().getName());
        }
        // table can have indexes.
        matrix.add("Row count", DataType.INT, table.getRowCount())
              .add("Column count", DataType.INT, table.getColumnCount())
              .add("Column names", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, tableNames))
              .add("Relationship names", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, relationshipNames));
        return matrix;
    }

    public KeyTypeValueMatrix getRelationshipMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int relationshipCount = 0;
        for (Relationship relationship : db.getRelationships()) {
            KeyTypeValueMatrix relationshipMatrix = getRelationshipMetadata(relationship);
            relationshipMatrix.prefixKeys(RELATIONSHIP, relationshipCount);
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
          .add("Relationship name", DataType.TEXT, relationship.getName())
          .add("CascadeDeletes", DataType.BOOLEAN, relationship.cascadeDeletes())
          .add("CascadeNullOnDelete", DataType.BOOLEAN, relationship.cascadeNullOnDelete())
          .add("CascadeUpdates", DataType.BOOLEAN, relationship.cascadeUpdates())
          .add("HasReferentialIntegrity", DataType.BOOLEAN, relationship.hasReferentialIntegrity())
          .add("IsLeftOuterJoin", DataType.BOOLEAN, relationship.isLeftOuterJoin())
          .add("IsOneToOne", DataType.BOOLEAN, relationship.isOneToOne())
          .add("IsRightOuterJoin", DataType.BOOLEAN, relationship.isRightOuterJoin())
          .add("FromTable", DataType.TEXT, relationship.getFromTable().getName())
          .add("FromColumns", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, fromColumnNames))
          .add("ToTable", DataType.TEXT, relationship.getToTable().getName())
          .add("ToColumns", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, toColumnNames))
          .add("JoinType", DataType.TEXT, relationship.getJoinType());
    }

    public KeyTypeValueMatrix getQueryMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int queryCount = 0;
        for (Query query : db.getQueries()) {
            KeyTypeValueMatrix queryMatrix = getQueryMetadata(query);
            queryMatrix.prefixKeys(QUERY, queryCount);
            matrix.append(queryMatrix);
            ++queryCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getQueryMetadata(Query query) {
        return new KeyTypeValueMatrix()
          .add("Query name", DataType.TEXT, query.getName())
          .add("Query type", DataType.TEXT, query.getType())
          .add("Parameters", DataType.COMPLEX_TYPE, String.join(KTV.CSV_DELIMITER, query.getParameters()))
          .add("SQL", DataType.TEXT, query.toSQLString().replaceAll("[\r\n]", " "));
    }

    public KeyTypeValueMatrix getExtendedTableMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int tableCount = 0;
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            KeyTypeValueMatrix tableMatrix = getTableMetadata(table, false);
            tableMatrix.prefixKeys(TABLE, tableCount);
            matrix.append(tableMatrix);
            int columnCount = 0;
            for (Column column : table.getColumns()) {
                KeyTypeValueMatrix columnMatrix = getColumnMetadata(column);
                columnMatrix.prefixKeys(TABLE_COLUMN, tableCount, columnCount);
                matrix.append(columnMatrix);
                ++columnCount;
            }
            ++tableCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getColumnMetadata(Column column) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add("Column name", DataType.TEXT, column.getName())
          .add("Column index", DataType.INT, column.getColumnIndex())
          .add("Data type", DataType.TEXT, column.getType())
          .add("Length", DataType.INT, column.getLength())

          .add("IsAppendOnly", DataType.BOOLEAN, column.isAppendOnly())
          .add("IsAutoNumber", DataType.BOOLEAN, column.isAutoNumber())
          .add("IsCalculated", DataType.BOOLEAN, column.isCalculated())
          .add("IsCompressedUnicode", DataType.BOOLEAN, column.isCompressedUnicode())
          .add("IsHyperlink", DataType.BOOLEAN, column.isHyperlink())
          .add("IsVariableLength", DataType.BOOLEAN, column.isVariableLength());
        for (PropertyMap.Property prop : column.getProperties()) {
            if (!EXCLUDED_COLUMN_PROPERTIES.contains(prop.getName())) {
                matrix.add("(Property) " + prop.getName(), prop.getType(), prop.getValue());
            }
        }
        Column vhColumn = column.getVersionHistoryColumn();
        if (vhColumn != null) {
            matrix.add("VersionHistoryColumn", DataType.INT, vhColumn.getColumnIndex());
        }
        return matrix;
    }


}
