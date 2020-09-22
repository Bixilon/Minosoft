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

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
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
    final LinkedHashMap<String, Object> config;

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

    public int getInt(String path) {
        return (int) get(path);
    }

    public int getInt(ConfigEnum config) {
        return getInt(config.getPath());
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

    public void putInt(ConfigEnum config, int value) {
        putInt(config.getPath(), value);
    }

    public void putInt(String path, int value) {
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

    public void putServer(Server server) {
        String basePath = String.format("servers.%d.", server.getId());
        putString(basePath + "name", server.getName());
        putString(basePath + "address", server.getAddress());
        putInt(basePath + "version", server.getDesiredVersion());
        if (server.getBase64Favicon() != null) {
            putString(basePath + "favicon", server.getBase64Favicon());
        }
    }

    public void removeServer(Server server) {
        remove(String.format("servers.%d", server.getId()));
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

    public void remove(String path) {
        if (path.contains(".")) {
            // split
            String[] spilt = path.split("\\.");
            LinkedHashMap<String, Object> temp = config;
            for (int i = 0; i < spilt.length - 1; i++) {
                // not yet existing, creating it
                temp = (LinkedHashMap<String, Object>) temp.get(spilt[i]);
                if (temp == null) {
                    return;
                }
            }
            temp.remove(spilt[spilt.length - 1]);
            return;
        }
        config.remove(path);
    }

    public void saveToFile(String filename) {
        Thread thread = new Thread(() -> {
            // write config to temp file, delete original config, rename temp file to original file to avoid conflicts if minosoft gets closed while saving the config
            File tempFile = new File(Config.homeDir + "config/" + filename + ".tmp");
            File file = new File(Config.homeDir + "config/" + filename);
            Yaml yaml = new Yaml();
            FileWriter writer;
            try {
                writer = new FileWriter(tempFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            synchronized (config) {
                yaml.dump(config, writer);
            }
            if (!file.delete() || !tempFile.renameTo(file)) {
                Log.fatal("An error occurred while saving the config file");
            } else {
                Log.verbose(String.format("Configuration saved to file %s", filename));
            }
        });
        thread.setName("IO-Thread");
        thread.start();
    }

    public HashBiMap<String, MojangAccount> getMojangAccounts() {
        HashBiMap<String, MojangAccount> accounts = HashBiMap.create();
        LinkedHashMap<String, LinkedHashMap<String, Object>> objects = (LinkedHashMap<String, LinkedHashMap<String, Object>>) get("account.accounts");
        if (objects == null) {
            return accounts;
        }
        for (Map.Entry<String, LinkedHashMap<String, Object>> set : objects.entrySet()) {
            LinkedHashMap<String, Object> entry = set.getValue();
            accounts.put(set.getKey(), new MojangAccount((String) entry.get("accessToken"), set.getKey(), UUID.fromString((String) entry.get("uuid")), (String) entry.get("playerName"), (String) entry.get("userName")));
        }
        return accounts;
    }

    public ArrayList<Server> getServers() {
        ArrayList<Server> servers = new ArrayList<>();
        LinkedHashMap<String, LinkedHashMap<String, Object>> objects = (LinkedHashMap<String, LinkedHashMap<String, Object>>) get("servers");
        if (objects == null) {
            return servers;
        }
        for (Map.Entry<String, LinkedHashMap<String, Object>> set : objects.entrySet()) {
            LinkedHashMap<String, Object> entry = set.getValue();
            String favicon = null;
            if (entry.containsKey("favicon")) {
                favicon = (String) entry.get("favicon");
            }
            servers.add(new Server(Integer.parseInt(set.getKey()), (String) entry.get("name"), (String) entry.get("address"), (int) entry.get("version"), favicon));
        }
        return servers;
    }

    public void removeAccount(MojangAccount account) {
        remove(String.format("account.accounts.%s", account.getUserId()));
    }
}

