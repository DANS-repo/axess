package nl.knaw.dans.repo.ingress;


import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;
import nl.knaw.dans.repo.ingress.core.FilenameComposer;
import nl.knaw.dans.repo.ingress.core.KeyTypeValueMatrix;
import nl.knaw.dans.repo.ingress.impl.SimpleFilenameComposer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataWriter {

  public static final String K_BUILD = "Build";

  public static final String T_BOOLEAN = "BOOLEAN";
  public static final String T_COMPLEX_TYPE = "COMPLEX_TYPE";
  public static final String T_LONG = "LONG";
  public static final String T_TEXT = "TEXT";

  public static final String CSV_DELIMITER = String.valueOf(CSVFormat.RFC4180.getDelimiter());

  private String rootDirectory;
  private FilenameComposer filenameComposer;

  public String getRootDirectory() {
    if (rootDirectory == null || "".equals(rootDirectory)) {
      rootDirectory = "root";
    }
    return rootDirectory;
  }

  public void setRootDirectory(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  public FilenameComposer getFilenameComposer() {
    if (filenameComposer == null) {
      filenameComposer = new SimpleFilenameComposer();
    }
    return filenameComposer;
  }

  public void setFilenameComposer(FilenameComposer filenameComposer) {
    this.filenameComposer = filenameComposer;
  }

  public void writeMetadata(Database db) throws Exception {
    KeyTypeValueMatrix matrix = getDatabaseMetadata(db);
    int tableCount = 0;
    for (String tableName : db.getTableNames()) {
      Table table = db.getTable(tableName);
      KeyTypeValueMatrix tableMatrix = getTableMetadata(table);
      String prefix = String.format("T%d_", ++tableCount);
      tableMatrix.prefixKeys(prefix);
      matrix.append(tableMatrix);
    }
    String filename = buildPaths(getFilenameComposer().getDabaseMetadataFilename(db));
    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), Charset.forName("UTF-8"));
    matrix.printVertical(osw);
  }

  public void writeMetadata(Database db, KeyTypeValueMatrix.Orientation orientation) throws IOException {
    writeDatabaseMetadata(db, orientation);
    writeTableMetadata(db, orientation);
  }

  public void writeDatabaseMetadata(Database db, KeyTypeValueMatrix.Orientation orientation) throws IOException {
    String filename = buildPaths(getFilenameComposer().getDabaseMetadataFilename(db));
    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), Charset.forName("UTF-8"));
    KeyTypeValueMatrix matrix = getDatabaseMetadata(db);
    matrix.print(osw, orientation);
  }

  public void writeTableMetadata(Database db, KeyTypeValueMatrix.Orientation orientation) throws IOException {
    for (String tableName : db.getTableNames()) {
      Table table = db.getTable(tableName);
      String filename = buildPaths(getFilenameComposer().getTableMetadataFilename(table));
      OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), Charset.forName("UTF-8"));
      KeyTypeValueMatrix matrix = getTableMetadata(table);
      matrix.print(osw, orientation);
    }
  }

  private String buildPaths(String basename) {
    String filename = FilenameUtils.concat(getRootDirectory(), basename);
    File directory = new File(filename).getParentFile();
    assert directory.exists() || directory.mkdirs();
    return filename;
  }

  public KeyTypeValueMatrix getDatabaseMetadata(Database db) throws IOException {
    KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
      .add("Filename", T_TEXT, db.getFile().getName())
      .add("Password", T_TEXT, db.getDatabasePassword())
      .add("File_format", T_TEXT, db.getFileFormat())
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
      matrix.add("Summary_" + prop.getName(), prop.getType().name(), prop.getValue());
    }
    for (PropertyMap.Property prop : db.getUserDefinedProperties()) {
      matrix.add("User_defined_" + prop.getName(), prop.getType().name(), prop.getValue());
    }

    matrix.add("Table_names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, db.getTableNames()));

    List<String> names = db.getRelationships().stream().map(Relationship::getName).collect(Collectors.toList());
    matrix.add("Relationship_names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, names));

    names = db.getQueries().stream().map(Query::getName).collect(Collectors.toList());
    matrix.add("Query_names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, names));

    return matrix;
  }

  public KeyTypeValueMatrix getTableMetadata(Table table) throws IOException {
    KeyTypeValueMatrix matrix = new KeyTypeValueMatrix()
      .add("Table_name", T_TEXT, table.getName())
      .add("Database_name", T_TEXT, table.getDatabase().getFile().getName())
      .add("Row_count", T_LONG, table.getRowCount())
      .add("Column_count", T_LONG, table.getColumnCount());

    List<String> names = table.getColumns().stream().map(Column::getName).collect(Collectors.toList());
    matrix.add("Column_names", T_COMPLEX_TYPE, String.join(CSV_DELIMITER, names));
    return matrix;
  }


}
