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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Configuration {
    final JsonObject config;
    final Thread thread;

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
        config = Util.readJsonFromFile(file.getAbsolutePath());

        final File finalFile = file;
        thread = new Thread(() -> {
            while (true) {
                // wait for interrupt
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException ignored) {
                }
                // write config to temp file, delete original config, rename temp file to original file to avoid conflicts if minosoft gets closed while saving the config
                File tempFile = new File(Config.homeDir + "config/" + filename + ".tmp");
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
                    finalFile.delete();
                }
                if (!tempFile.renameTo(finalFile)) {
                    Log.fatal("An error occurred while saving the config file");
                } else {
                    Log.verbose(String.format("Configuration saved to file %s", filename));
                }
            }
        }, "IO");
        thread.start();
    }

    public boolean getBoolean(GameConfiguration path) {
        return switch (path) {
            case NETWORK_FAKE_CLIENT_BRAND -> config.getAsJsonObject("network").get("fake-network-brand").getAsBoolean();
            default -> throw new RuntimeException(String.format("Illegal boolean value: %s", path));
        };
    }

    public void putBoolean(GameConfiguration path, boolean value) {
        switch (path) {
            case NETWORK_FAKE_CLIENT_BRAND -> config.getAsJsonObject("network").addProperty("fake-network-brand", value);
            default -> throw new RuntimeException(String.format("Illegal boolean value: %s", path));
        }
    }

    public int getInt(GameConfiguration path) {
        return switch (path) {
            case CONFIG_VERSION -> config.getAsJsonObject("general").get("version").getAsInt();
            case GAME_RENDER_DISTANCE -> config.getAsJsonObject("game").get("render-distance").getAsInt();
            default -> throw new RuntimeException(String.format("Illegal int value: %s", path));
        };
    }

    public void putInt(GameConfiguration path, int value) {
        switch (path) {
            case CONFIG_VERSION -> config.getAsJsonObject("general").addProperty("version", value);
            case GAME_RENDER_DISTANCE -> config.getAsJsonObject("game").addProperty("render-distance", value);
            default -> throw new RuntimeException(String.format("Illegal int value: %s", path));
        }
    }

    public String getString(GameConfiguration path) {
        return switch (path) {
            case ACCOUNT_SELECTED -> config.getAsJsonObject("accounts").get("selected").getAsString();
            case GENERAL_LOG_LEVEL -> config.getAsJsonObject("general").get("log-level").getAsString();
            case MAPPINGS_URL -> config.getAsJsonObject("download").getAsJsonObject("urls").get("mappings").getAsString();
            case CLIENT_TOKEN -> config.getAsJsonObject("accounts").get("client-token").getAsString();
            default -> throw new RuntimeException(String.format("Illegal String value: %s", path));
        };
    }

    public void putString(GameConfiguration path, String value) {
        switch (path) {
            case ACCOUNT_SELECTED -> config.getAsJsonObject("accounts").addProperty("selected", value);
            case GENERAL_LOG_LEVEL -> config.getAsJsonObject("general").addProperty("log-level", value);
            case MAPPINGS_URL -> config.getAsJsonObject("download").getAsJsonObject("urls").addProperty("mappings", value);
            case CLIENT_TOKEN -> config.getAsJsonObject("accounts").addProperty("client-token", value);
            default -> throw new RuntimeException(String.format("Illegal String value: %s", path));
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
        thread.interrupt();
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
}

