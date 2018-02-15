package nl.knaw.dans.repo.axxess;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;
import nl.knaw.dans.repo.axxess.core.KeyTypeValueMatrix;
import org.apache.commons.csv.CSVFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataExtractor {

    private static final String K_BUILD = "Build";
    private static final String T_BOOLEAN = "BOOLEAN";
    private static final String T_COMPLEX_TYPE = "COMPLEX_TYPE";
    private static final String T_LONG = "LONG";
    private static final String T_TEXT = "TEXT";
    private static final String CSV_DELIMITER = String.valueOf(CSVFormat.RFC4180.getDelimiter());
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
          .add("Filename", T_TEXT, db.getFile().getName())
          .add("Password", T_TEXT, db.getDatabasePassword())
          .add("File format", T_TEXT, db.getFileFormat())
          .add("Charset", T_TEXT, db.getCharset());

        PropertyMap propertyMap = db.getDatabaseProperties();
        PropertyMap.Property accessVersion = propertyMap.get(PropertyMap.ACCESS_VERSION_PROP);
        if (accessVersion != null) {
            matrix.add(PropertyMap.ACCESS_VERSION_PROP, accessVersion.getType().name(), accessVersion.getValue());
        }
        PropertyMap.Property build = propertyMap.get(K_BUILD);
        if (build != null) {
            matrix.add(K_BUILD, build.getType().name(), build.getValue());
        }
        for (PropertyMap.Property prop : db.getSummaryProperties()) {
            matrix.add("(Summary) " + prop.getName(), prop.getType().name(), prop.getValue());
        }
        for (PropertyMap.Property prop : db.getUserDefinedProperties()) {
            matrix.add("(User defined) " + prop.getName(), prop.getType().name(), prop.getValue());
        }
        matrix.add("Relationship count", T_LONG, relationshipNames.size())
              .add("Relationship names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, relationshipNames))
              .add("Query count", T_LONG, queryNames.size())
              .add("Query names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, queryNames))
              .add("Table count", T_LONG, db.getTableNames().size())
              .add("Table names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, db.getTableNames()))
              .prefixKeys("[DB]");
        return matrix;
    }

    public KeyTypeValueMatrix getTableMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int tableCount = 0;
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            KeyTypeValueMatrix tableMatrix = getTableMetadata(table, false);
            String prefix = String.format("[T%d]", tableCount);
            tableMatrix.prefixKeys(prefix);
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
          .add("Table name", T_TEXT, table.getName());
        if (includeDbName) {
            matrix.add("Database name", T_TEXT, table.getDatabase().getFile().getName());
        }
        matrix.add("Row count", T_LONG, table.getRowCount())
              .add("Column count", T_LONG, table.getColumnCount())
              .add("Column names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, tableNames))
              .add("Relationship names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, relationshipNames));
        return matrix;
    }

    public KeyTypeValueMatrix getRelationshipMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int relationshipCount = 0;
        for (Relationship relationship : db.getRelationships()) {
            KeyTypeValueMatrix relationshipMatrix = getRelationshipMetadata(relationship);
            String prefix = String.format("[R%d]", relationshipCount);
            relationshipMatrix.prefixKeys(prefix);
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
          .add("Relationship name", T_TEXT, relationship.getName())
          .add("CascadeDeletes", T_BOOLEAN, relationship.cascadeDeletes())
          .add("CascadeNullOnDelete", T_BOOLEAN, relationship.cascadeNullOnDelete())
          .add("CascadeUpdates", T_BOOLEAN, relationship.cascadeUpdates())
          .add("HasReferentialIntegrity", T_BOOLEAN, relationship.hasReferentialIntegrity())
          .add("IsLeftOuterJoin", T_BOOLEAN, relationship.isLeftOuterJoin())
          .add("IsOneToOne", T_BOOLEAN, relationship.isOneToOne())
          .add("IsRightOuterJoin", T_BOOLEAN, relationship.isRightOuterJoin())
          .add("FromTable", T_TEXT, relationship.getFromTable().getName())
          .add("FromColumns", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, fromColumnNames))
          .add("ToTable", T_TEXT, relationship.getToTable().getName())
          .add("ToColumns", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, toColumnNames))
          .add("JoinType", T_TEXT, relationship.getJoinType());
    }

    public KeyTypeValueMatrix getQueryMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int queryCount = 0;
        for (Query query : db.getQueries()) {
            KeyTypeValueMatrix queryMatrix = getQueryMetadata(query);
            String prefix = String.format("[Q%d]", queryCount);
            queryMatrix.prefixKeys(prefix);
            matrix.append(queryMatrix);
            ++queryCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getQueryMetadata(Query query) {
        return new KeyTypeValueMatrix()
          .add("Query name", T_TEXT, query.getName())
          .add("Query type", T_TEXT, query.getType())
          .add("Parameters", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, query.getParameters()))
          .add("SQL", T_TEXT, query.toSQLString().replaceAll("[\r\n]", " "));
    }

    public KeyTypeValueMatrix getExtendedTableMetadata(Database db) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix();
        int tableCount = 0;
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            KeyTypeValueMatrix tableMatrix = getTableMetadata(table, false);
            String tablePrefix = String.format("[T%d]", tableCount);
            tableMatrix.prefixKeys(tablePrefix);
            matrix.append(tableMatrix);
            int columnCount = 0;
            for (Column column : table.getColumns()) {
                KeyTypeValueMatrix columnMatrix = getColumnMetadata(column);
                String prefix = String.format("%s[C%d]", tablePrefix, columnCount);
                columnMatrix.prefixKeys(prefix);
                matrix.append(columnMatrix);
                ++columnCount;
            }
            ++tableCount;
        }
        return matrix;
    }

    public KeyTypeValueMatrix getColumnMetadata(Column column) throws IOException {
        KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
          .add("Column name", T_TEXT, column.getName())
          .add("Column index", T_LONG, column.getColumnIndex())
          .add("Data type", T_TEXT, column.getType())
          .add("Length", T_LONG, column.getLength())

          .add("IsAppendOnly", T_BOOLEAN, column.isAppendOnly())
          .add("IsAutoNumber", T_BOOLEAN, column.isAutoNumber())
          .add("IsCalculated", T_BOOLEAN, column.isCalculated())
          .add("IsCompressedUnicode", T_BOOLEAN, column.isCompressedUnicode())
          .add("IsHyperlink", T_BOOLEAN, column.isHyperlink())
          .add("IsVariableLength", T_BOOLEAN, column.isVariableLength());
        for (PropertyMap.Property prop : column.getProperties()) {
            if (!EXCLUDED_COLUMN_PROPERTIES.contains(prop.getName())) {
                matrix.add("(Property) " + prop.getName(), prop.getType(), prop.getValue());
            }
        }
        Column vhColumn = column.getVersionHistoryColumn();
        if (vhColumn != null) {
            matrix.add("VersionHistoryColumn", T_LONG, vhColumn.getColumnIndex());
        }
        return matrix;
    }


}
