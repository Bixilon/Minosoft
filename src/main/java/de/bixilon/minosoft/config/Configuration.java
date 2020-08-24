/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config;

import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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
            File folder = new File(Config.homeDir + "config/");
            if (!folder.exists() && !folder.mkdirs()) {
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

    public void putBoolean(ConfigEnum config, boolean value) {
        putBoolean(config.getPath(), value);
    }

    public void putBoolean(String path, boolean value) {
        put(path, value);
    }

    public void putInteger(ConfigEnum config, int value) {
        putInteger(config.getPath(), value);
    }

    public void putInteger(String path, int value) {
        put(path, value);
    }

    public void putString(ConfigEnum config, String value) {
        putString(config.getPath(), value);
    }

    public void putString(String path, String value) {
        put(path, value);
    }

    public void putMojangAccount(MojangAccount account) {
        String basePath = String.format("account.accounts.%s.", account.getUserId());
        putString(basePath + "accessToken", account.getAccessToken());
        putString(basePath + "uuid", account.getUUID().toString());
        putString(basePath + "userName", account.getMojangUserName());
        putString(basePath + "playerName", account.getPlayerName());
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
            if (temp == null) {
                return null;
            }
            return temp.get(spilt[spilt.length - 1]);
        }
        return config.get(path);
    }

    public void put(String path, Serializable value) {
        if (path.contains(".")) {
            // split
            String[] spilt = path.split("\\.");
            LinkedHashMap<String, Object> temp = config;
            for (int i = 0; i < spilt.length - 1; i++) {
                // not yet existing, creating it
                temp.computeIfAbsent(spilt[i], k -> new LinkedHashMap<String, Object>());
                temp = (LinkedHashMap<String, Object>) temp.get(spilt[i]);
            }
            temp.put(spilt[spilt.length - 1], value);
            return;
        }
        config.put(path, value);
    }

    public void saveToFile(String filename) {
        Yaml yaml = new Yaml();
        FileWriter writer;
        try {
            writer = new FileWriter(Config.homeDir + "config/" + filename);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        yaml.dump(config, writer);
    }

    public ArrayList<MojangAccount> getMojangAccounts() {
        ArrayList<MojangAccount> accounts = new ArrayList<>();
        LinkedHashMap<String, Object> objects = (LinkedHashMap<String, Object>) get("account.accounts");
        if (objects == null) {
            return accounts;
        }
        for (Map.Entry<String, Object> set : objects.entrySet()) {
            LinkedHashMap<String, Object> entry = (LinkedHashMap<String, Object>) set.getValue();
            accounts.add(new MojangAccount((String) entry.get("accessToken"), set.getKey(), UUID.fromString((String) entry.get("uuid")), (String) entry.get("playerName"), (String) entry.get("mojangUserName")));
        }
        return accounts;
    }
}

