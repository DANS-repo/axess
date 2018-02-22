package nl.knaw.dans.repo.axxess.core;

import org.apache.commons.csv.CSVFormat;

import java.util.Arrays;
import java.util.List;

public interface Axxess {

    String CSV_DELIMITER = String.valueOf(CSVFormat.RFC4180.getDelimiter());

    String DB_FILENAME = "Filename";
    String DB_PASSWORD = "Password";
    String DB_FILE_FORMAT = "File format";
    String DB_BUILD = "Build";
    String DB_CHARSET = "Charset";

    String DB_SUMMARY_PROP = "(Summary)";
    String DB_USER_DEFINED_PROP = "(User defined)";

    String DB_RELATIONSHIP_COUNT = "Relationship count";
    String DB_RELATIONSHIP_NAMES = "Relationship names";
    String DB_QUERY_COUNT = "Query count";
    String DB_QUERY_NAMES = "Query names";
    String DB_TABLE_COUNT = "Table count";
    String DB_TABLE_NAMES = "Table names";

    String TABLE_NAME = "Table name";
    String TABLE_DATABASE_NAME = "Database name";
    String TABLE_ROW_COUNT = "Row count";
    String TABLE_COLUMN_COUNT = "Column count";
    String TABLE_COLUMN_NAMES = "Column names";
    String TABLE_RELATIONSHIP_NAMES = "Relationship names";
    String TABLE_IS_ALLOW_AUTO_NUMBER_INSERT = "IsAllowAutoNumberInsert";
    String TABLE_INDEX_NAMES = "Index names";
    String TABLE_PRIMARY_KEY_INDEX = "PrimaryKeyIndex";

    String R_NAME = "Relationship name";
    String R_CASCADE_DELETES = "CascadeDeletes";
    String R_CASCADE_NULL_ON_DELETETES = "CascadeNullOnDelete";
    String R_CASCADE_UPDATES = "CascadeUpdates";
    String R_HAS_REFERENTIAL_INTEGRITY = "HasReferentialIntegrity";
    String R_IS_LEFT_OUTER_JOIN = "IsLeftOuterJoin";
    String R_IS_ONE_TO_ONE = "IsOneToOne";
    String R_IS_RIGHT_OUTER_JOIN = "IsRightOuterJoin";
    String R_FROM_TABLE = "FromTable";
    String R_FROM_COLUMNS = "FromColumns";
    String R_TO_TABLE = "ToTable";
    String R_TO_COLUMNS = "ToColumns";
    String R_JOIN_TYPE = "JoinType";

    String Q_NAME = "Query name";
    String Q_TYPE = "Query type";
    String Q_PARAMETERS = "Parameters";
    String Q_SQL = "SQL";

    String I_NAME = "Index name";
    String I_COLUMN_COUNT = "Column count";
    String I_COLUMN_NAMES = "Column names";
    String I_REFERENCED_INDEX = "Referenced index";
    String I_IS_FOREIGN_KEY = "IsForeignKey";
    String I_IS_PRIMARY_KEY = "IsPrimaryKey";
    String I_IS_REQUIRED = "IsRequired";
    String I_IS_UNIQUE = "IsUnique";
    String I_SHOULD_IGNORE_NULLS = "ShouldIgnoreNulls";

    String C_NAME = "Column name";
    String C_INDEX = "Column index";
    String C_DATA_TYPE = "Data type";
    String C_LENGTH = "Length";
    String C_IS_APPEND_ONLY = "IsAppendOnly";
    String C_IS_AUTO_NUMBER = "IsAutoNumber";
    String C_IS_CALCULATED = "IsCalculated";
    String C_IS_COMPRESSED_UNICODE = "IsCompressedUnicode";
    String C_IS_HYPERLINK = "IsHyperlink";
    String C_IS_VARIABLE_LENGTH = "IsVariableLength";
    String C_VERSION_HISTORY_COLUMN = "VersionHistoryColumn";
    String C_PROP = "(Property)";

    List<String> C_EXCLUDED_PROPERTIES = Arrays.asList(
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

}
