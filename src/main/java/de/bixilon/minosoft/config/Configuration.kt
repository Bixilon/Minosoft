/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.config

import com.squareup.moshi.JsonWriter
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.config.Config
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.json.JSONSerializer
import de.bixilon.minosoft.util.logging.Log
import okio.Buffer
import java.io.File
import java.io.FileWriter
import java.io.IOException

class Configuration(private val configName: String = StaticConfiguration.CONFIG_FILENAME) {
    private val file = File(StaticConfiguration.HOME_DIRECTORY + "config/minosoft/" + configName)
    val config: Config

    init {
        if (file.exists()) {
            config = JSONSerializer.CONFIG_ADAPTER.fromJson(Util.readFile(file.absolutePath))!!

            if (config.general.version > LATEST_CONFIG_VERSION) {
                throw ConfigMigrationException(String.format("Configuration was migrated to newer config format (version=${config.general.version}, expected=${LATEST_CONFIG_VERSION}). Downgrading the config file is unsupported!"))
            }
            if (config.general.version < LATEST_CONFIG_VERSION) {
                migrateConfiguration()
            }
        } else {
            // no configuration file
            config = Config()
            Log.debug("Created new config file")
        }
    }


    fun saveToFile() {
        Minosoft.THREAD_POOL.execute {
            // write config to temp file, delete original config, rename temp file to original file to avoid conflicts if minosoft gets closed while saving the config
            val tempFile = File(StaticConfiguration.HOME_DIRECTORY + "config/minosoft/" + configName + ".tmp")
            Util.createParentFolderIfNotExist(tempFile)
            val buffer = Buffer()
            val jsonWriter: JsonWriter = JsonWriter.of(buffer)
            jsonWriter.indent = "  "

            synchronized(this.config) {
                JSONSerializer.CONFIG_ADAPTER.toJson(jsonWriter, config)
            }
            val writer: FileWriter = try {
                FileWriter(tempFile)
            } catch (e: IOException) {
                e.printStackTrace()
                return@execute
            }
            writer.write(buffer.readUtf8())
            try {
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (file.exists()) {
                if (!file.delete()) {
                    throw RuntimeException("Could not save config!")
                }
            }
            if (!tempFile.renameTo(file)) {
                Log.fatal("An error occurred while saving the config file")
            } else {
                Log.verbose("Configuration saved to file %s", configName)
            }
        }
    }

    private fun migrateConfiguration() {
        Log.info(String.format("Migrating config from version ${config.general.version} to  $LATEST_CONFIG_VERSION"))
        for (nextVersion in config.general.version + 1..LATEST_CONFIG_VERSION) {
            migrateConfiguration(nextVersion)
        }
        config.general.version = LATEST_CONFIG_VERSION
        saveToFile()
        Log.info("Finished migrating config!")
    }

    private fun migrateConfiguration(nextVersion: Int) {
        // switch (nextVersion) {
        //     // ToDo
        //     default: throw new ConfigMigrationException("Can not migrate config: Unknown config version " + nextVersion);
        // }
    }

    companion object {
        const val LATEST_CONFIG_VERSION = 1
    }
}
