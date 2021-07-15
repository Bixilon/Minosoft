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
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.json.JSONSerializer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import okio.Buffer
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

class Configuration(private val configName: String = RunConfiguration.CONFIG_FILENAME) {
    private val saveLock = Object()
    private val file = File(RunConfiguration.HOME_DIRECTORY + "config/minosoft/" + configName)
    val config: Config

    init {
        if (file.exists()) {
            val config = JSONSerializer.MAP_ADAPTER.fromJson(Util.readFile(file.absolutePath))!!

            migrate(config)
            var wasMigrated = false
            let {
                val configVersion = config["general"]?.compoundCast()?.get("version")?.toInt() ?: return@let
                if (config["general"]?.compoundCast()?.get("version")?.toInt() ?: 0 > LATEST_CONFIG_VERSION) {
                    throw ConfigMigrationException("Configuration was migrated to newer config format (version=${configVersion}, expected=${LATEST_CONFIG_VERSION}). Downgrading the config file is unsupported!")
                }
                if (configVersion < LATEST_CONFIG_VERSION) {
                    startMigration(configVersion, config)
                    wasMigrated = true
                }
            }

            this.config = JSONSerializer.CONFIG_ADAPTER.fromJsonValue(config)!!

            if (wasMigrated) {
                saveToFile()
            }
        } else {
            // no configuration file
            config = Config()
            Log.debug("Created new config file")
        }
    }


    fun saveToFile() {
        Minosoft.THREAD_POOL.execute {
            synchronized(saveLock) {
                // write config to temp file, delete original config, rename temp file to original file to avoid conflicts if minosoft gets closed while saving the config
                val tempFile = File(RunConfiguration.HOME_DIRECTORY + "config/minosoft/" + configName + ".tmp")
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
                Files.move(tempFile.toPath(), file.toPath())
            }
        }
    }

    @Deprecated(message = "Will be removed one a release/beta is there")
    private fun migrate(config: MutableMap<String, Any>) {
        config["download"]?.compoundCast()?.get("url")?.compoundCast()?.let {
            if (it["pixlyzer"] == "https://gitlab.com/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.gz?inline=false") {
                it["pixlyzer"] = "https://gitlab.com/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false"
            }
        }
    }

    private fun startMigration(configVersion: Int, config: MutableMap<String, Any>) {
        Log.info(String.format("Migrating config from version $configVersion to  $LATEST_CONFIG_VERSION"))
        for (nextVersion in configVersion + 1..LATEST_CONFIG_VERSION) {
            migrateConfiguration(nextVersion, config)
        }
        config["general"]?.compoundCast()?.put("version", LATEST_CONFIG_VERSION)
        Log.info("Finished migrating config!")
    }

    private fun migrateConfiguration(nextVersion: Int, config: Map<String, Any>) {
        when (nextVersion) {
            else -> throw ConfigMigrationException("Can not migrate config: Unknown config version $nextVersion")
        }
    }

    companion object {
        const val LATEST_CONFIG_VERSION = 1
    }
}
