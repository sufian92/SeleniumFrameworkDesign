package rahulshettyacademy.helpers;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConfigurationHelper {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationHelper.class.getName());

    public static final String CONFIG_PROPERTIES_FILE = "config.properties";

    public static final String BROWSER = "BROWSER";

    public static final String RUN_CONFIGURATION = "RUN_CONFIGURATION";

    public static final String SNAPSHOTMODE = "SNAPSHOTMODE";
    public static final String VALUE_SNAPSHOT_MODE = "compare";
    public static final String VALUE_SNAPSHOT_UPDATE_MODE = "update";
    public static final String VALUE_SNAPSHOT_IGNORE_MODE = "ignore";
    public static final String SNAPSHOTSTRATEGY = "SNAPSHOTSTRATEGY";
    public static final String SNAPSHOTSTRATEGY_VIEWPORT = "viewport";
    public static final String SNAPSHOTS_VIEWPORT_WAIT = "SNAPSHOTS_VIEWPORT_WAIT";
    public static final String SNAPSHOTS_VIEWPORT_WAIT_DEFAULT = "500";

    public static final String PIXELS_THRESHOLD = "PIXELS_THRESHOLD";

    public static final String RUN_ENVIRONMENT = "RUN_ENVIRONMENT";

    public static final String NODE_URL = "NODE_URL";
    public static final String VALUE_RUN_ENVIROMENT_LOCAL = "local";
    public static final String VALUE_RUN_ENVIROMENT_JENKINS = "jenkins";

    public static final String DBUSERNAME = "DBUSERNAME";
    public static final String DBPASSWORD = "DBPASSWORD";
    public static final String DB_URL = "DB_URL";

    public static final String CHROME_IMAGE = "CHROME_IMAGE";
    public static final String SELENIUM_GRID_URL = "SELENIUM_GRID_URL";

    public static final String TESTER_NAME = "TESTER_NAME";
    public static ConfigurationHelper INSTANCE = buildInstance();
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Properties props = new Properties();

    public ConfigurationHelper() {
    }

    private static ConfigurationHelper buildInstance() {
        ConfigurationHelper inst = new ConfigurationHelper();
        inst.init();
        return inst;
    }

    protected void init() {
        props.putAll(System.getenv());
        System.getProperties().entrySet().stream()
                .forEach(e -> props.putIfAbsent(e.getKey(), e.getValue()));
        InputStream configPropertiesFile = null;
        try {
            configPropertiesFile = getConfigPropertiesFile();
            if (configPropertiesFile != null) {
                Properties p = new Properties();
                p.load(configPropertiesFile);
                p.forEach((k, v) -> props.putIfAbsent(k, v));
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINEST, "Error looking to class path!", e);
        } finally {
            if (configPropertiesFile != null) {
                try {

                } catch (Exception e) {
                    LOGGER.log(Level.FINEST, "Error closing stream!", e);
                }
            }
        }
    }

    public String getProperty(final String propName) {
        return getProperty(propName, null);
    }

    public String getProperty(final String propName, final String defaultValue) {
        String value = props.getProperty(propName);
        return value != null ? value : defaultValue;
    }

    protected String getConfigPropertiesFileName() {
        return CONFIG_PROPERTIES_FILE;
    }

    protected InputStream getConfigPropertiesFile() {
        String property = System.getProperty(CONFIG_PROPERTIES_FILE);
        if (StringUtils.isNotBlank(property)) {
            //overriden by property
            return IOUtils.findResourceInClasspath(property);
        } else {
            return IOUtils.findResourceInClasspath(getConfigPropertiesFileName());
        }
    }
}
