package xyz.exemplator.exemplator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Main {
    private Logger logger = LoggerFactory.getLogger(Main.class);
    private final Properties config;
    private final Map<String, String> envProp;

    public Main(String configLocation) {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(configLocation));
        } catch (IOException e) {
            logger.error("unable to load config-file {}", configLocation, e);
            System.exit(-1);
        }
        this.config = config;
        this.envProp = System.getenv();
    }


    public static void main(String[] args) {
        String configLocation = "./conf/conf.properties";
        if (args.length != 0) {
            configLocation = args[0];
        }

        Main main = new Main(configLocation);
        main.init();
    }

    private void init() {
        int port = -1;
        try {
            port = Integer.parseInt(requireProperty("PORT"));
        } catch (NumberFormatException e) {
            logger.error("PORT must be a number, {}", e);
            System.exit(-1);
        }
        Router router = new Router(port);
        try {
            router.init();
        } catch (Exception e) {
            logger.error("unable to init router");
            System.exit(-1);
        }
    }

    private String requireProperty(String key) {
        String property = getProperty(key);
        if (property == null) {
            logger.error("property {} must be set", key);
            System.exit(-1);
        }
        return property;
    }

    private String getProperty(String key) {
        String valueEnv = envProp.get(key);
        if (valueEnv != null) {
            return valueEnv;
        }
        return config.getProperty(key);
    }
}
