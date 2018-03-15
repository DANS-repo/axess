package nl.knaw.dans.repo.axxess.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import nl.knaw.dans.repo.axxess.acc2csv.Axxess2CsvConverter;
import nl.knaw.dans.repo.axxess.core.AxxessException;
import nl.knaw.dans.repo.axxess.csv2acc.Csv2AxxessConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
        if (args.length > 0) {
            if (args[0].equals("-h") || args[0].equals("--help")) {
                help();
                System.exit(0);
            }
        }
        if (args.length > 1) {
            // assume SLF4J is bound to logback in the current environment
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                // Call context.reset() to clear any previous configuration, e.g. default
                // configuration. For multi-step configuration, omit calling context.reset().
                context.reset();
                configurator.doConfigure(args[1]);
            } catch (JoranException je) {
                // StatusPrinter will handle this
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        } else {
            // Logging as configured in resources/logback.xml
            System.out.println("No log configuration given. Using default. See: logs/axxess.log");
        }

        LOG.info(AXXESS_ART);
        File propFile = null;
        File baseDir = new File(".");
        baseDir = baseDir.getCanonicalFile();
        if (args.length > 0) {
            String propFilename = args[0];
            propFile = new File(propFilename).getCanonicalFile();
            if (!propFile.exists()) {
                LOG.error(" Cannot find properties file at " + propFile.getAbsolutePath());
                help();
                System.exit(-1);
            }
        }

        if (propFile == null || !propFile.exists()) {
            propFile = new File(baseDir, AXXESS_DEF).getCanonicalFile();
        }

        if (!propFile.exists()) {
            LOG.info("No properties file at " + propFile.getAbsolutePath());
            baseDir = new File("docker");
            baseDir = baseDir.getCanonicalFile();
            propFile = new File(baseDir, AXXESS_DEF).getCanonicalFile();
        }
        if (!propFile.exists()) {
            LOG.error("Cannot find properties file at " + propFile.getAbsolutePath());
            help();
            System.exit(-1);
        }

        doConvert(propFile, baseDir);
    }

    private static void doConvert(File propFile, File baseDir) throws IOException, AxxessException {
        LOG.info("Configuring Axxess run from {}", propFile.getPath());
        FileInputStream fis = new FileInputStream(propFile);
        PROPS.load(fis);
        fis.close();

        String mode = getProp("axxess.mode", MODE_ACA);
        List<File> csvResultFiles = new ArrayList<>();
        List<File> dbResultFiles = new ArrayList<>();
        Axxess2CsvConverter a2c = null;
        Csv2AxxessConverter c2a = null;

        File dbSourceFile = null;
        String dbSource = getProp("db.source.file");
        if ((dbSource == null || dbSource.isEmpty()) && mode.startsWith("a")) {
            LOG.warn("No source file or source directory specified. See cfg/axxess.properties, db.source.file");
            return;
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
            dbSourceFile = new File(dbSource);
            if (!dbSourceFile.isAbsolute()) {
                dbSourceFile = new File(baseDir, dbSource).getCanonicalFile();
            }
            LOG.info("Absolute       db.source.file={}", dbSourceFile.getAbsolutePath());

            a2c = new Axxess2CsvConverter()
              .withTargetDirectory(csvTargetDir)
              .withForceSourceEncoding(getProp("db.force.source.encoding"))
              .withTargetEncoding(csvTargetEncoding)
              .withCSVFormat(csvTargetFormat)
              .setExtractMetadata("true".equalsIgnoreCase(getProp("csv.target.include.metadata", "true")))
              .setIncludeManifest("true".equalsIgnoreCase(getProp("csv.target.include.manifest", "true")))
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
              .setIncludeManifest("true".equalsIgnoreCase(getProp("db.target.include.manifest", "true")));

        }

        if (a2c != null) {
            csvResultFiles = a2c.convert(dbSourceFile);
        }

        if (c2a != null) {
            dbResultFiles = c2a.convert(csvSourceFile);
        }

        if (a2c != null) {
            String msg = String.format("Converted %d database(s) to %d result files, with %d error(s) and %d warnings(s).",
              a2c.getDatabaseCount(), csvResultFiles.size(), a2c.getErrorCount(), a2c.getWarningCount());
            System.out.println(msg);
            LOG.info(msg);
            if (a2c.getErrorCount() > 0) {
                LOG.info("Errors during conversion from Access to csv:");
                LOG.info(a2c.getErrorList()
                            .stream()
                            .map(Throwable::getMessage)
                            .collect(Collectors.joining("\n\t")));
            }
            if (a2c.getWarningCount() > 0) {
                LOG.info("Warnings during conversion from Access to csv:");
                LOG.info(a2c.getWarningList()
                            .stream()
                            .map(Throwable::getMessage)
                            .collect(Collectors.joining("\n\t")));
            }
            String csvResultListFile = getProp("csv.result.list.file", null);
            if (csvResultListFile != null) {
                boolean absoluteNames = "true".equalsIgnoreCase(getProp("csv.result.list.absolute.filenames", "false"));
                writeResultFiles(csvResultFiles, csvResultListFile, baseDir, absoluteNames);
            }
        }
        if (c2a != null) {
            String msg = String.format("Converted %d metadata file(s) to %d database(s), with %d error(s) and %d warning(s)",
              c2a.getMetadataFilenameCount(), c2a.getDatabaseCount(), c2a.getErrorCount(), c2a.getWarningCount());
            System.out.println(msg);
            LOG.info(msg);
            if (c2a.getErrorCount() > 0) {
                LOG.info("Errors during conversion from csv to Access:");
                LOG.info(c2a.getErrorList()
                            .stream()
                            .map(Throwable::getMessage)
                            .collect(Collectors.joining("\n\t")));
            }
            if (c2a.getWarningCount() > 0) {
                LOG.info("Warnings during conversion from csv to Access:");
                LOG.info(c2a.getWarningList()
                            .stream()
                            .map(Throwable::getMessage)
                            .collect(Collectors.joining("\n\t")));
            }
            String dbResultListFile = getProp("db.result.list.file", null);
            if (dbResultListFile != null) {
                boolean absoluteNames = "true".equalsIgnoreCase(getProp("db.result.list.absolute.filenames", "false"));
                writeResultFiles(dbResultFiles, dbResultListFile, baseDir, absoluteNames);
            }
        }
        System.out.println("See logs for details");
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

    private static void writeResultFiles(List<File> resultFiles, String filename, File baseDir, boolean absoluteNames)
      throws IOException {
        File file = new File(filename);
        if (!file.isAbsolute()) {
            file = new File(baseDir, filename);
        }
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"))) {
            for (File resultFile : resultFiles) {
                String path = resultFile.getPath();
                if (!absoluteNames) {
                    path = path.substring(baseDir.getPath().length() + 1);
                }
                osw.write(path + "\n");
            }
            osw.close();
        }
        LOG.info("Wrote result file list to {}", file.getAbsolutePath());
    }

    private static void help() {
        System.out.println("Axxess is a tool for converting MS Access databases to and from csv files.");
        System.out.println("See also: https://github.com/DANS-repo/axxess\n");
        String jarFile = new java.io.File(AxxessMain.class.getProtectionDomain()
                                                          .getCodeSource()
                                                          .getLocation()
                                                          .getPath())
          .getName();
        if ("classes".equals(jarFile)) {
            System.out.println("USAGE:\n\n" +
              "      java " + AxxessMain.class.getName() + " [axxess.properties] [logback configuration]");
        } else {
            System.out.println("USAGE:\n\n" +
              "          java -jar " + jarFile + " [axxess.properties] [logback configuration]");
        }
        System.out.println("\naxxess.properties      - configuration file.");
        System.out.println("                         " +
          "See https://github.com/DANS-repo/axxess/blob/master/docker/cfg/axxess.properties");
        System.out.println("                         " +
          "If no properties file given will look for cfg/axxess.properties");

        System.out.println("\nlogback configuration  - logging configuration.");
        System.out.println("                         " +
          "See https://logback.qos.ch/manual/configuration.html");
        System.out.println("                         " +
          "If no logging configuration given will log to logs/axxess.log");
    }
}
