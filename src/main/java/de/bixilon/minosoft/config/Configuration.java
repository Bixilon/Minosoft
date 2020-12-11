/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config;

import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    public static final int LATEST_CONFIG_VERSION = 1;
    private final static JsonObject DEFAULT_CONFIGURATION = JsonParser.parseReader(new InputStreamReader(Configuration.class.getResourceAsStream("/config/" + StaticConfiguration.CONFIG_FILENAME))).getAsJsonObject();
    private final HashMap<ConfigurationPaths.ConfigurationPath, Object> config = new HashMap<>();
    private final HashBiMap<String, MojangAccount> accountList = HashBiMap.create();
    private final HashBiMap<Integer, Server> serverList = HashBiMap.create();
    private final Object lock = new Object();

    public Configuration() throws IOException, ConfigMigrationException {
        File file = new File(StaticConfiguration.HOME_DIRECTORY + "config/" + StaticConfiguration.CONFIG_FILENAME);
        if (!file.exists()) {
            // no configuration file
            InputStream input = getClass().getResourceAsStream("/config/" + StaticConfiguration.CONFIG_FILENAME);
            if (input == null) {
                throw new FileNotFoundException(String.format("[Config] Missing default config: %s!", StaticConfiguration.CONFIG_FILENAME));
            }
            File folder = new File(StaticConfiguration.HOME_DIRECTORY + "config/");
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("[Config] Could not create config folder!");
            }
            Files.copy(input, Paths.get(file.getAbsolutePath()));
            file = new File(StaticConfiguration.HOME_DIRECTORY + "config/" + StaticConfiguration.CONFIG_FILENAME);
        }

        JsonObject json = Util.readJsonFromFile(file.getAbsolutePath());


        int configVersion = (int) getData(json, ConfigurationPaths.IntegerPaths.GENERAL_CONFIG_VERSION);
        if (configVersion > LATEST_CONFIG_VERSION) {
            throw new ConfigMigrationException(String.format("Configuration was migrated to newer config format (version=%d, expected=%d). Downgrading the config file is unsupported!", configVersion, LATEST_CONFIG_VERSION));
        }
        if (configVersion < LATEST_CONFIG_VERSION) {
            migrateConfiguration();
        }

        for (ConfigurationPaths.ConfigurationPath path : ConfigurationPaths.ALL_PATHS) {
            config.put(path, getData(json, path));
        }

        // servers
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("servers").getAsJsonObject("entries").entrySet()) {
            serverList.put(Integer.parseInt(entry.getKey()), Server.deserialize(entry.getValue().getAsJsonObject()));
        }

        // accounts
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("accounts").getAsJsonObject("entries").entrySet()) {
            MojangAccount account = MojangAccount.deserialize(entry.getValue().getAsJsonObject());
            accountList.put(account.getUserId(), account);
        }

        final File finalFile = file;
        new Thread(() -> {
            while (true) {
                // wait for interrupt
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                // write config to temp file, delete original config, rename temp file to original file to avoid conflicts if minosoft gets closed while saving the config
                File tempFile = new File(StaticConfiguration.HOME_DIRECTORY + "config/" + StaticConfiguration.CONFIG_FILENAME + ".tmp");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileWriter writer;
                try {
                    writer = new FileWriter(tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                JsonObject jsonObject = DEFAULT_CONFIGURATION.deepCopy();
                synchronized (config) {

                    // accounts
                    JsonObject accountsEntriesJson = jsonObject.getAsJsonObject("servers").getAsJsonObject("entries");
                    for (Map.Entry<Integer, Server> entry : serverList.entrySet()) {
                        accountsEntriesJson.add(String.valueOf(entry.getKey()), entry.getValue().serialize());
                    }

                    // servers
                    JsonObject serversEntriesJson = jsonObject.getAsJsonObject("accounts").getAsJsonObject("entries");
                    for (Map.Entry<String, MojangAccount> entry : accountList.entrySet()) {
                        serversEntriesJson.add(entry.getKey(), entry.getValue().serialize());
                    }

                    // rest of data
                    for (ConfigurationPaths.ConfigurationPath path : ConfigurationPaths.ALL_PATHS) {
                        saveData(jsonObject, path, config.get(path));
                    }
                }
                gson.toJson(jsonObject, writer);
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (finalFile.exists()) {
                    if (!finalFile.delete()) {
                        throw new RuntimeException("Could not save config!");
                    }
                }
                if (!tempFile.renameTo(finalFile)) {
                    Log.fatal("An error occurred while saving the config file");
                } else {
                    Log.verbose(String.format("Configuration saved to file %s", StaticConfiguration.CONFIG_FILENAME));
                }
            }
        }, "IO").start();
    }

    public boolean getBoolean(ConfigurationPaths.BooleanPaths path) {
        return (boolean) config.get(path);
    }

    public void putBoolean(ConfigurationPaths.BooleanPaths path, boolean value) {
        config.put(path, value);
    }

    public int getInt(ConfigurationPaths.IntegerPaths path) {
        return (int) config.get(path);
    }

    public void putInt(ConfigurationPaths.IntegerPaths path, int value) {
        config.put(path, value);
    }

    public String getString(ConfigurationPaths.StringPaths path) {
        return (String) config.get(path);
    }

    public void putString(ConfigurationPaths.StringPaths path, String value) {
        config.put(path, value);
    }

    public void putMojangAccount(MojangAccount account) {
        accountList.put(account.getUserId(), account);
    }

    public void putServer(Server server) {
        serverList.put(server.getId(), server);
    }

    public void removeServer(Server server) {
        serverList.remove(server.getId());
    }

    public void saveToFile() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void removeAccount(MojangAccount account) {
        accountList.remove(account.getUserId());
    }

    public HashBiMap<Integer, Server> getServerList() {
        return serverList;
    }

    public HashBiMap<String, MojangAccount> getAccountList() {
        return accountList;
    }

    private void migrateConfiguration() throws ConfigMigrationException {
        int configVersion = getInt(ConfigurationPaths.IntegerPaths.GENERAL_CONFIG_VERSION);
        Log.info(String.format("Migrating config from version %d to  %d", configVersion, LATEST_CONFIG_VERSION));
        for (int nextVersion = configVersion + 1; nextVersion <= LATEST_CONFIG_VERSION; nextVersion++) {
            migrateConfiguration(nextVersion);
        }
        putInt(ConfigurationPaths.IntegerPaths.GENERAL_CONFIG_VERSION, LATEST_CONFIG_VERSION);
        saveToFile();
        Log.info("Finished migrating config!");

    }

    private void migrateConfiguration(int nextVersion) throws ConfigMigrationException {
        switch (nextVersion) {
            // ToDo
            default -> throw new ConfigMigrationException("Can not migrate config: Unknown config version " + nextVersion);
        }
    }

    private Object getData(JsonObject json, ConfigurationPaths.ConfigurationPath path) {
        if (path instanceof ConfigurationPaths.BooleanPaths booleanPath) {
            return switch (booleanPath) {
                case NETWORK_FAKE_CLIENT_BRAND -> json.getAsJsonObject("network").get("fake-network-brand").getAsBoolean();
                case NETWORK_SHOW_LAN_SERVERS -> json.getAsJsonObject("network").get("show-lan-servers").getAsBoolean();
                case DEBUG_VERIFY_ASSETS -> json.getAsJsonObject("debug").get("verify-assets").getAsBoolean();
            };
        }
        if (path instanceof ConfigurationPaths.IntegerPaths integerPath) {
            return switch (integerPath) {
                case GENERAL_CONFIG_VERSION -> json.getAsJsonObject("general").get("version").getAsInt();
                case GAME_RENDER_DISTANCE -> json.getAsJsonObject("game").get("render-distance").getAsInt();
            };
        }
        if (path instanceof ConfigurationPaths.StringPaths stringPath) {
            return switch (stringPath) {
                case ACCOUNT_SELECTED -> json.getAsJsonObject("accounts").get("selected").getAsString();
                case GENERAL_LOG_LEVEL -> json.getAsJsonObject("general").get("log-level").getAsString();
                case GENERAL_LANGUAGE -> json.getAsJsonObject("general").get("language").getAsString();
                case RESOURCES_URL -> json.getAsJsonObject("download").getAsJsonObject("urls").get("resources").getAsString();
                case CLIENT_TOKEN -> json.getAsJsonObject("accounts").get("client-token").getAsString();
            };
        }
        throw new RuntimeException();
    }

    private void saveData(JsonObject input, ConfigurationPaths.ConfigurationPath path, Object data) {
        if (data instanceof Boolean bool) {
            switch ((ConfigurationPaths.BooleanPaths) path) {
                case NETWORK_FAKE_CLIENT_BRAND -> input.getAsJsonObject("network").addProperty("fake-network-brand", bool);
                case NETWORK_SHOW_LAN_SERVERS -> input.getAsJsonObject("network").addProperty("show-lan-servers", bool);
                case DEBUG_VERIFY_ASSETS -> input.getAsJsonObject("debug").addProperty("verify-assets", bool);
            }
        } else if (data instanceof Integer integer) {
            switch ((ConfigurationPaths.IntegerPaths) path) {
                case GENERAL_CONFIG_VERSION -> input.getAsJsonObject("general").addProperty("version", integer);
                case GAME_RENDER_DISTANCE -> input.getAsJsonObject("game").addProperty("render-distance", integer);
            }
        } else if (data instanceof String string) {
            switch ((ConfigurationPaths.StringPaths) path) {
                case ACCOUNT_SELECTED -> input.getAsJsonObject("accounts").addProperty("selected", string);
                case GENERAL_LOG_LEVEL -> input.getAsJsonObject("general").addProperty("log-level", string);
                case GENERAL_LANGUAGE -> input.getAsJsonObject("general").addProperty("language", string);
                case RESOURCES_URL -> input.getAsJsonObject("download").getAsJsonObject("urls").addProperty("resources", string);
                case CLIENT_TOKEN -> input.getAsJsonObject("accounts").addProperty("client-token", string);
            }
        }
    }
}
