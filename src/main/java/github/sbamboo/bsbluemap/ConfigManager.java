package github.sbamboo.bsbluemap;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FOLDER_NAME = "backstage_bluemap";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final File CONFIG_FOLDER = new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FOLDER_NAME);
    private static final File CONFIG_FILE = new File(CONFIG_FOLDER, CONFIG_FILE_NAME);

    public static boolean enabled = true;

    public static void loadConfig() {
        Properties properties = new Properties();

        // Ensure config directory exists
        if (!CONFIG_FOLDER.exists()) {
            boolean created = CONFIG_FOLDER.mkdirs();
            if (created) {
                BackstageBluemap.LOGGER.info("Created config directory: " + CONFIG_FOLDER.getPath());
            } else {
                BackstageBluemap.LOGGER.warn("Failed to create config directory: " + CONFIG_FOLDER.getPath());
            }
        }

        // Load or create config file with default values
        if (CONFIG_FILE.exists()) {
            try (FileInputStream inStream = new FileInputStream(CONFIG_FILE)) {
                properties.load(inStream);
                enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
                BackstageBluemap.LOGGER.info("Config loaded from file: " + CONFIG_FILE.getPath());
            } catch (IOException e) {
                BackstageBluemap.LOGGER.error("Failed to load config file: " + CONFIG_FILE.getPath(), e);
            }
        } else {
            // Set default values and create a new config file
            properties.setProperty("enabled", String.valueOf(enabled));
            saveConfig(properties);
            BackstageBluemap.LOGGER.info("Config file not found, created new config file: " + CONFIG_FILE.getPath());
        }
    }

    static void saveConfig(Properties properties) {
        try (FileOutputStream outStream = new FileOutputStream(CONFIG_FILE)) {
            properties.store(outStream, "Backstage Bluemap Configuration");
            //BackstageBluemap.LOGGER.info("Config saved to file: " + CONFIG_FILE.getPath());
        } catch (IOException e) {
            BackstageBluemap.LOGGER.error("Failed to save config file: " + CONFIG_FILE.getPath(), e);
        }
    }
}
