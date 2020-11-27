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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Configuration {
    public static final int LATEST_CONFIG_VERSION = 1;
    final JsonObject config;
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
        config = Util.readJsonFromFile(file.getAbsolutePath());
        int configVersion = getInt(ConfigurationPaths.IntegerPaths.GENERAL_CONFIG_VERSION);
        if (configVersion > LATEST_CONFIG_VERSION) {
            throw new ConfigMigrationException(String.format("Configuration was migrated to newer config format (version=%d, expected=%d). Downgrading the config file is unsupported!", configVersion, LATEST_CONFIG_VERSION));
        }
        if (configVersion < LATEST_CONFIG_VERSION) {
            migrateConfiguration();
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
                synchronized (config) {
                    gson.toJson(config, writer);
                }
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
        return switch (path) {
            case NETWORK_FAKE_CLIENT_BRAND -> config.getAsJsonObject("network").get("fake-network-brand").getAsBoolean();
            case NETWORK_SHOW_LAN_SERVERS -> config.getAsJsonObject("network").get("show-lan-servers").getAsBoolean();
            case DEBUG_VERIFY_ASSETS -> config.getAsJsonObject("debug").get("verify-assets").getAsBoolean();
        };
    }

    public void putBoolean(ConfigurationPaths.BooleanPaths path, boolean value) {
        switch (path) {
            case NETWORK_FAKE_CLIENT_BRAND -> config.getAsJsonObject("network").addProperty("fake-network-brand", value);
            case NETWORK_SHOW_LAN_SERVERS -> config.getAsJsonObject("network").addProperty("show-lan-servers", value);
            case DEBUG_VERIFY_ASSETS -> config.getAsJsonObject("debug").addProperty("verify-assets", value);
        }
    }

    public int getInt(ConfigurationPaths.IntegerPaths path) {
        return switch (path) {
            case GENERAL_CONFIG_VERSION -> config.getAsJsonObject("general").get("version").getAsInt();
            case GAME_RENDER_DISTANCE -> config.getAsJsonObject("game").get("render-distance").getAsInt();
        };
    }

    public void putInt(ConfigurationPaths.IntegerPaths path, int value) {
        switch (path) {
            case GENERAL_CONFIG_VERSION -> config.getAsJsonObject("general").addProperty("version", value);
            case GAME_RENDER_DISTANCE -> config.getAsJsonObject("game").addProperty("render-distance", value);
        }
    }

    public String getString(ConfigurationPaths.StringPaths path) {
        return switch (path) {
            case ACCOUNT_SELECTED -> config.getAsJsonObject("accounts").get("selected").getAsString();
            case GENERAL_LOG_LEVEL -> config.getAsJsonObject("general").get("log-level").getAsString();
            case GENERAL_LANGUAGE -> config.getAsJsonObject("general").get("language").getAsString();
            case RESOURCES_URL -> config.getAsJsonObject("download").getAsJsonObject("urls").get("resources").getAsString();
            case CLIENT_TOKEN -> config.getAsJsonObject("accounts").get("client-token").getAsString();
        };
    }

    public void putString(ConfigurationPaths.StringPaths path, String value) {
        switch (path) {
            case ACCOUNT_SELECTED -> config.getAsJsonObject("accounts").addProperty("selected", value);
            case GENERAL_LOG_LEVEL -> config.getAsJsonObject("general").addProperty("log-level", value);
            case GENERAL_LANGUAGE -> config.getAsJsonObject("general").addProperty("language", value);
            case RESOURCES_URL -> config.getAsJsonObject("download").getAsJsonObject("urls").addProperty("resources", value);
            case CLIENT_TOKEN -> config.getAsJsonObject("accounts").addProperty("client-token", value);
        }
    }

    public void putMojangAccount(MojangAccount account) {
        config.getAsJsonObject("accounts").getAsJsonObject("entries").add(account.getUserId(), account.serialize());
    }

    public void putServer(Server server) {
        config.getAsJsonObject("servers").getAsJsonObject("entries").add(String.valueOf(server.getId()), server.serialize());
    }

    public void removeServer(Server server) {
        config.getAsJsonObject("servers").getAsJsonObject("entries").remove(String.valueOf(server.getId()));
    }

    public void saveToFile() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public HashBiMap<String, MojangAccount> getMojangAccounts() {
        HashBiMap<String, MojangAccount> accounts = HashBiMap.create();
        JsonObject entries = config.getAsJsonObject("accounts").getAsJsonObject("entries");
        entries.keySet().forEach((entry) -> {
            MojangAccount account = MojangAccount.deserialize(entries.get(entry).getAsJsonObject());
            accounts.put(account.getUserId(), account);
        });
        return accounts;
    }

    public ArrayList<Server> getServers() {
        ArrayList<Server> servers = new ArrayList<>();
        JsonObject entries = config.getAsJsonObject("servers").getAsJsonObject("entries");
        entries.keySet().forEach((entry) -> servers.add(Server.deserialize(entries.get(entry).getAsJsonObject())));
        return servers;
    }

    public void removeAccount(MojangAccount account) {
        config.getAsJsonObject("accounts").getAsJsonObject("entries").remove(account.getUserId());
    }

    public JsonObject getConfig() {
        return config;
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
}
