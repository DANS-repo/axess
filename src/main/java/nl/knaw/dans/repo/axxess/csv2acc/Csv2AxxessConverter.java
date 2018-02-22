package nl.knaw.dans.repo.axxess.csv2acc;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.IndexBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;
import nl.knaw.dans.repo.axxess.core.AbstractConverter;
import nl.knaw.dans.repo.axxess.core.Axxess;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.core.KTV;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XColumn;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XDatabase;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XIndex;
import nl.knaw.dans.repo.axxess.csv2acc.xdb.XTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Csv2AxxessConverter extends AbstractConverter<Csv2AxxessConverter> implements Axxess {

    public static final String DEFAULT_OUTPUT_DIRECTORY = "axxess-csv-out";

    private static Logger LOG = LoggerFactory.getLogger(Csv2AxxessConverter.class);

    private Database.FileFormat targetFormat;

    public Csv2AxxessConverter withTargetDatabaseFileFormat(Database.FileFormat format) {
        targetFormat = format;
        return this;
    }

    @Override
    public String getDefaultOutputDirectory() {
        return DEFAULT_OUTPUT_DIRECTORY;
    }

    @Override
    public List<File> convert(File file) throws AxxessException {
        reset();
        List<File> resultFiles = new ArrayList<>();
        convert(file.getAbsoluteFile(), getTargetDirectory(), resultFiles);
        return resultFiles;
    }

    private void convert(File file, File targetDirectory, List<File> resultFiles) throws AxxessException {
        if (file.isDirectory()) {
            File td = new File(targetDirectory, file.getName());
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                convert(f, td, resultFiles);
            }
        } else if (isMetadataFile(file)) {
            try {
                builtFromFile(file, getTargetFileFormat(), targetDirectory, resultFiles);
            } catch (IOException e) {
                LOG.error("While converting: " + file.getAbsolutePath(), e);
                addError(e);
            }
        }
    }

    public void builtFromFile(File mdFile, Database.FileFormat targetFormat, File targetDirectory, List<File> resultFiles) throws IOException {
        assert targetDirectory.exists() || targetDirectory.mkdirs();
        LOG.info("Trying to parse {}", mdFile.getAbsolutePath());
        XDatabase xdb = MetadataParser.parse(mdFile);
        File targetFile = new File(targetDirectory, getFilenameComposer().getDatabaseFilename(mdFile.getName(), getTargetFileFormat().getFileExtension()));
        built(xdb, targetFormat, targetFile, resultFiles);
    }

    public void built(XDatabase xdb, Database.FileFormat targetFormat, File targetFile, List<File> resultFiles) throws IOException {
        assert targetFile.exists() || targetFile.createNewFile();
        LOG.info("Trying to built {}", targetFile.getAbsolutePath());
        Database db = null;
        try {
            db = DatabaseBuilder.create(targetFormat, targetFile);
            for (XTable xt : xdb.getTables()) {
                String tableName = xt.getString(TABLE_NAME);
                TableBuilder tableBuilder = new TableBuilder(tableName);
                //Optional<XIndex> maybeIndex = xt.getPrimaryKeyIndex();
                //maybeIndex.ifPresent(xIndex -> tableBuilder.setPrimaryKey(xIndex.getStringArray(I_COLUMN_NAMES)));

                for (XIndex xi : xt.getIndexes()) {
                    String indexName = xi.getString(I_NAME);
                    IndexBuilder indexBuilder = new IndexBuilder(indexName)
                      .addColumns(xi.getStringArray(I_COLUMN_NAMES));
                    if (xi.getBool(I_IS_PRIMARY_KEY)) indexBuilder.setPrimaryKey();
                    if (xi.getBool(I_IS_REQUIRED)) indexBuilder.setRequired();
                    if (xi.getBool(I_IS_UNIQUE)) indexBuilder.setUnique();
                    if (xi.getBool(I_SHOULD_IGNORE_NULLS)) indexBuilder.setIgnoreNulls();
                    tableBuilder.addIndex(indexBuilder);
                    LOG.debug("Build index '{}' on table '{}'", indexName, tableName);
                }

                for (XColumn xc : xt.getColumns()) {
                    String columnName = xc.getString(C_NAME);
                    ColumnBuilder columnBuilder = new ColumnBuilder(columnName)
                      .setType(xc.getDataType(C_DATA_TYPE))
                      .setLength(xc.getInt(C_LENGTH))
                      .setAutoNumber(xc.getBool(C_IS_AUTO_NUMBER))
                      .setCalculated(xc.getBool(C_IS_CALCULATED))
                      .setCompressedUnicode(xc.getBool(C_IS_COMPRESSED_UNICODE))
                      .setHyperlink(xc.getBool(C_IS_HYPERLINK));
                    for (KTV ktv : xc.getProperties(C_PROP)) {
                        columnBuilder.putProperty(ktv.getKey(), ktv.getType(), ktv.getValue());
                    }
                    tableBuilder.addColumn(columnBuilder);
                    LOG.debug("Build column '{}' on table '{}'", columnName, tableName);
                }
                Table table = tableBuilder.toTable(db);
                LOG.debug("Finished building table '{}'", tableName);
            }
            // for (XRelationship xr : xdb.getRelationships()) {
            //     RelationshipBuilder relBuilder = new RelationshipBuilder(xr.getString(R_FROM_TABLE), xr.getString(R_TO_TABLE));
            // }
        } finally {
            if (db != null) db.close();
        }
        resultFiles.add(targetFile);
    }

    private Database.FileFormat getTargetFileFormat() {
        if (targetFormat == null) {
            targetFormat = Database.FileFormat.V2010;
        }
        return targetFormat;
    }

    private boolean isMetadataFile(File file) {
        return Pattern.matches(getFilenameComposer().getMetadataFilenamePattern(), file.getName());
    }


}
