package nl.knaw.dans.repo.axxess.app;

import nl.knaw.dans.repo.axxess.acc2csv.Axxess2CsvConverter;
import nl.knaw.dans.repo.axxess.csv2acc.Csv2AxxessConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AxxessMain {

    private static final String AXXESS_DEF = "cfg/axxess.properties";
    private static final String MODE_ACA = "aca";
    private static final String AXXESS_ART = "\n\n" +

      "           %#@      \n" +
      "          %#@ &!    \n" +
      "         %#@  &!*   \n" +
      "        %#@    &!*  \n" +
      "       %#@#####&!*  \n" +
      "      %#@^^^^^^^&!* \n" +
      "     %#@        &!*~ \n" +
      "   ___________________\n";
    private static Logger LOG = LoggerFactory.getLogger(AxxessMain.class);
    private static Properties PROPS = new Properties();

    public static void main(String[] args) throws Exception {
        LOG.info(AXXESS_ART);
        File propFile = null;
        File baseDir = new File(".");
        if (args.length > 0) {
            String propFilename = args[0];
            propFile = new File(propFilename).getCanonicalFile();
            if (!propFile.exists()) {
                LOG.error(" Cannot find properties file at " + propFile.getAbsolutePath());
                System.exit(-1);
            }
        }

        if (propFile == null || !propFile.exists()) {
            propFile = new File(baseDir, AXXESS_DEF).getCanonicalFile();
        }

        if (!propFile.exists()) {
            LOG.info("No properties file at " + propFile.getAbsolutePath());
            baseDir = new File("docker");
            propFile = new File(baseDir, AXXESS_DEF).getCanonicalFile();
        }
        if (!propFile.exists()) {
            LOG.error("Cannot find properties file at " + propFile.getAbsolutePath());
            System.exit(-1);
        }

        LOG.info("Configuring Axxess run from {}", propFile.getPath());
        FileInputStream fis = new FileInputStream(propFile);
        PROPS.load(fis);
        fis.close();

        String mode = getProp("axxess.mode", MODE_ACA);
        List<File> csvResultFiles = new ArrayList<>();
        List<File> dbResultFiles = new ArrayList<>();
        Axxess2CsvConverter a2c = null;
        Csv2AxxessConverter c2a = null;

        String dbSource = getProp("db.source.file");
        if (dbSource == null || dbSource.isEmpty()) {
            LOG.warn("No source file or source directory specified. See cfg/axxess.properties, db.source.file");
            return;
        }
        File dbSourceFile = new File(dbSource);
        if (!dbSourceFile.isAbsolute()) {
            dbSourceFile = new File(baseDir, dbSource).getCanonicalFile();
        }

        File csvTargetDir = null;
        String csvTarget = getProp("csv.target.directory", Axxess2CsvConverter.DEFAULT_OUTPUT_DIRECTORY);
        csvTargetDir = new File(csvTarget);
        if (!csvTargetDir.isAbsolute()) {
            csvTargetDir = new File(baseDir, csvTarget).getCanonicalFile();
        }
        LOG.info("Absolute csv.target.directory={}", csvTargetDir.getAbsolutePath());

        String csvSource = getProp("csv.source.file");
        if ((csvSource == null || csvSource.isEmpty())) {
            csvSource = csvTargetDir.getPath();
        }
        File csvSourceFile = new File(csvSource);
        if (!csvSourceFile.isAbsolute()) {
            csvSourceFile = new File(baseDir, csvSource).getCanonicalFile();
        }

        String csvTargetEncoding = getProp("csv.target.encoding");
        String csvTargetFormat = getProp("csv.target.csvformat");

        if (mode.startsWith("a")) {
            LOG.info("Absolute       db.source.file={}", dbSourceFile.getAbsolutePath());

            a2c = new Axxess2CsvConverter()
              .withTargetDirectory(csvTargetDir)
              .withForceSourceEncoding(getProp("db.force.source.encoding"))
              .withTargetEncoding(csvTargetEncoding)
              .withCSVFormat(csvTargetFormat)
              .setIncludeManifest("true".equalsIgnoreCase(getProp("csv.target.manifest", "true")))
              .setArchiveResults("true".equalsIgnoreCase(getProp("create.zip", "false")))
              .setCompressArchive("true".equalsIgnoreCase(getProp("compress.zip", "false")));
        }

        if (mode.endsWith("a")) {
            LOG.info("Absolute      csv.source.file={}", csvSourceFile.getAbsolutePath());

            String dbTarget = getProp("db.target.directory", Csv2AxxessConverter.DEFAULT_OUTPUT_DIRECTORY);
            File dbTargetDir = new File(dbTarget);
            if (!dbTargetDir.isAbsolute()) {
                dbTargetDir = new File(baseDir, dbTarget).getCanonicalFile();
            }
            LOG.info("Absolute  db.target.directory={}", dbTargetDir.getAbsolutePath());

            c2a = new Csv2AxxessConverter()
              .withTargetDirectory(dbTargetDir)
              .withSourceEncoding(getProp("csv.source.encoding", csvTargetEncoding))
              .withCSVFormat(getProp("csv.source.csvformat", csvTargetFormat))
              .withTargetDatabaseFileFormat(getProp("db.target.database.format"))
              .setAutoNumberColumns("true".equalsIgnoreCase(getProp("db.target.autonumber.columns", "false")))
              .setIncludeRelationships("true".equalsIgnoreCase(getProp("db.target.include.relationships", "true")))
              .setIncludeIndexes("true".equalsIgnoreCase(getProp("db.target.include.indexes", "true")))
              .setIncludeManifest("true".equalsIgnoreCase(getProp("db.target.manifest", "true")));

        }

        if (a2c != null) {
            csvResultFiles = a2c.convert(dbSourceFile);
        }

        if (c2a != null) {
            dbResultFiles = c2a.convert(csvSourceFile);
        }

        if (a2c != null) {
            LOG.info("Converted {} database(s) to {} result files, with {} error(s) and {} warnings(s).",
              a2c.getDatabaseCount(), csvResultFiles.size(), a2c.getErrorCount(), a2c.getWarningCount());
        }
        if (c2a != null) {
            LOG.info("Converted {} metadata file(s) to {} database(s), with {} error(s) and {} warning(s)",
              c2a.getMetadataFilenameCount(), c2a.getDatabaseCount(), c2a.getErrorCount(), c2a.getWarningCount());
        }
    }

    private static String getProp(String key, String defaultValue) {
        String prop = PROPS.getProperty(key);
        if (prop == null || prop.isEmpty()) {
            LOG.info("{}={} (Default)", key, defaultValue);
            return defaultValue;
        } else {
            LOG.info("{}={}", key, prop);
            return prop;
        }
    }

    private static String getProp(String key) {
        String prop = PROPS.getProperty(key);
        if (prop != null && !prop.isEmpty()) {
            LOG.info("{}={}", key, prop);
        }
        return prop;
    }
}
