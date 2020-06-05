package de.bixilon.minosoft.config;

import de.bixilon.minosoft.Config;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class Configuration {
    LinkedHashMap<String, Object> config;

    public Configuration(String filename) throws IOException {

        File file = new File(Config.homeDir + "config/" + filename);
        if (!file.exists()) {
            // no configuration file
            InputStream input = getClass().getResourceAsStream("/config/" + filename);
            if (input == null) {
                throw new FileNotFoundException(String.format("[Config] Missing default config: %s!", filename));
            }
            File c_folder = new File(Config.homeDir + "config/");
            if (!c_folder.exists() && !c_folder.mkdirs()) {
                throw new IOException("[Config] Could not create config folder!");
            }
            Files.copy(input, Paths.get(file.getAbsolutePath()));
            file = new File(Config.homeDir + "config/" + filename);
        }
        Yaml yml = new Yaml();
        config = yml.load(new FileInputStream(file));
    }

    public boolean getBoolean(String path) {
        return (boolean) get(path);
    }

    public boolean getBoolean(ConfigEnum config) {
        return getBoolean(config.getPath());
    }

    public int getInteger(String path) {
        return (int) get(path);
    }

    public int getInteger(ConfigEnum config) {
        return getInteger(config.getPath());
    }

    public String getString(String path) {
        return (String) get(path);
    }

    public String getString(ConfigEnum config) {
        return getString(config.getPath());
    }


    public Object get(String path) {
        if (path.contains(".")) {
            // split
            String[] spilt = path.split("\\.");
            LinkedHashMap<String, Object> temp = config;
            for (int i = 0; i < spilt.length - 1; i++) {
                //noinspection unchecked
                temp = (LinkedHashMap<String, Object>) temp.get(spilt[i]);
            }
            return temp.get(spilt[spilt.length - 1]);
        }
        return config.get(path);
    }
}

