package nl.knaw.dans.repo.axxess.app;

import nl.knaw.dans.repo.axxess.acc2csv.AxxessToCsvConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

public class AxxessMain {

    public static final String AXXESS_DEF = "cfg/axxess.properties";
    public static final String MODE_ACA = "aca";
    public static final String MODE_AC = "ac";
    public static final String MODE_CA = "ca";

    private static Logger LOG = LoggerFactory.getLogger(AxxessMain.class);

    private static final String AXXESS_ART = "\n\n" +

      "           %#@      \n" +
      "          %#@ &!    \n" +
      "         %#@  &!*   \n" +
      "        %#@    &!*  \n" +
      "       %#@#####&!*  \n" +
      "      %#@^^^^^^^&!* \n" +
      "     %#@        &!*~ \n" +
      "   ___________________\n";

    private static File BASE_DIR;

    public static void main(String[] args) throws Exception {
        LOG.info(AXXESS_ART);
        BASE_DIR = new File(".");
        File propFile = new File(BASE_DIR, AXXESS_DEF);
        if (!propFile.exists()) {
            BASE_DIR = new File("docker");
            propFile = new File(BASE_DIR, AXXESS_DEF);
        }
        if (!propFile.exists()) {
            LOG.error(" Cannot find properties file at " + propFile.getAbsolutePath());
            System.exit(-1);
        }
        LOG.info("Configuring Axxess run from {}", propFile.getPath());
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propFile);
        props.load(fis);
        fis.close();

        String mode = props.getProperty("axxess.mode", MODE_ACA);
        if (mode.startsWith("a")) {
            String source = props.getProperty("db.source.file");
            File sourceFile = new File(BASE_DIR, source);
            LOG.info("db.source.file={}", sourceFile.getAbsolutePath());

            String target = getEvenEmpty(props,"csv.target.directory", AxxessToCsvConverter.DEFAULT_OUTPUT_DIRECTORY);
            File targetDir = new File(BASE_DIR, target);
            LOG.info("csv.target.directory={}", targetDir.getAbsolutePath());

            AxxessToCsvConverter a2c = new AxxessToCsvConverter()
              .withTargetDirectory(targetDir)
              .withForceSourceEncoding(props.getProperty("db.source.force.encoding"))
              .withTargetEncoding(props.getProperty("csv.target.encoding"))
              .withCSVFormat(props.getProperty("csv.target.format"))
              .setIncludeManifest("true".equalsIgnoreCase(props.getProperty("include.manifest")))
              .setArchiveResults("true".equalsIgnoreCase(props.getProperty("create.zip")))
              .setCompressArchive("true".equalsIgnoreCase(props.getProperty("compress.zip")));

            List<File> files = a2c.convert(sourceFile);
        }
    }

    private static String getEvenEmpty(Properties props, String key, String defaultValue) {
        String prop = props.getProperty(key);
        if (prop == null || prop.isEmpty()) {
            return defaultValue;
        } else {
            return prop;
        }
    }
}
