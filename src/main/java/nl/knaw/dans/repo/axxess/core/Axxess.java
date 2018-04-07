package nl.knaw.dans.repo.axxess.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Properties;

/**
 * Axxess constants.
 */
public interface Axxess {

    Properties APP_PROPS = new Properties();

    Logger LOG = LoggerFactory.getLogger(Axxess.class);

    String EM_CONVERSION_DATE = "Conversion date";
    String EM_OS_NAME = "OS name";
    String EM_OS_ARCH = "OS arch";
    String EM_OS_VERSION = "OS version";
    String EM_AXXESS_VERSION = "Axxess version";
    String EM_AXXESS_BUILD = "Axxess build";
    String EM_AXXESS_URL = "Axxess URL";
    String EM_CODEX = "Codex";
    String EM_WARNINGS = "Warnings";

    String DB_FILENAME = "Filename";
    String DB_PASSWORD = "Password";
    String DB_FILE_FORMAT = "File format";
    String DB_CHARSET = "Charset";
    String DB_IS_ALLOW_AUTO_NUMBER_INSERT = "IsAllowAutoNumberInsert";
    String DB_COLUMN_ORDER = "ColumnOrder";

    String DB_DATABASE_PROP = "(Database)";
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
    String TABLE_PROP = "(Property)";

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
    String Q_IS_HIDDEN = "IsHidden";
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
    String C_LENGTH_IN_UNITS = "Length in units";
    String C_SCALE = "Scale";
    String C_PRECISION = "Precision";
    String C_SQL_TYPE = "SQL type";
    String C_IS_APPEND_ONLY = "IsAppendOnly";
    String C_IS_AUTO_NUMBER = "IsAutoNumber";
    String C_IS_CALCULATED = "IsCalculated";
    String C_IS_COMPRESSED_UNICODE = "IsCompressedUnicode";
    String C_IS_HYPERLINK = "IsHyperlink";
    String C_IS_VARIABLE_LENGTH = "IsVariableLength";
    String C_VERSION_HISTORY_COLUMN = "VersionHistoryColumn";
    String C_PROP = "(Property)";

    static String getProperty(String propertyName) {
        return getProperties().getProperty(propertyName);
    }

    static Properties getProperties() {
        if (APP_PROPS.getProperty("loaded") == null) {
            try {
                APP_PROPS.load(Axxess.class.getClassLoader().getResourceAsStream("axxess_version.properties"));
            } catch (Exception e) {
                LOG.error("Could not load axxess_version.properties", e);
            }
            APP_PROPS.setProperty("loaded", Instant.now().toString());
        }
        return APP_PROPS;
    }

}
