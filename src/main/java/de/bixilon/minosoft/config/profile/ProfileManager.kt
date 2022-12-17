/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile

import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.ExceptionUtil.tryCatch
import de.bixilon.kutil.file.FileUtil
import de.bixilon.kutil.file.FileUtil.read
import de.bixilon.kutil.file.watcher.FileWatcher
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.locks.ReentrantLock


interface ProfileManager<T : Profile> {
    val mapper: ObjectMapper
    val namespace: ResourceLocation
    val latestVersion: Int
    val saveLock: ReentrantLock
    val profileClass: Class<T>
    val jacksonProfileType: JavaType
        get() = Jackson.MAPPER.typeFactory.constructType(profileClass)
    val profileSelectable: Boolean
        get() = true
    val icon: Ikon
        get() = FontAwesomeSolid.QUESTION

    val profiles: AbstractMutableBiMap<String, T>
    var selected: T

    val baseDirectory: File
        get() = File(RunConfiguration.HOME_DIRECTORY + "config/" + namespace.namespace + "/")

    fun getPath(profileName: String, baseDirectory: File = this.baseDirectory): String {
        return baseDirectory.path + "/" + profileName + "/" + namespace.path + ".json"
    }

    /**
     * Migrates the config from 1 version to the next
     * Does not convert to the latest version, just 1 version number higher
     */
    fun migrate(from: Int, data: MutableMap<String, Any?>) = Unit


    fun updateValue(profile: T, data: MutableMap<String, Any?>?) {
        profile.reloading = true
        val injectable = InjectableValues.Std()
        injectable.addValue(profileClass, profile)
        mapper.injectableValues = injectable
        mapper.updateValue(profile, data)
        profile.saved = true
        profile.reloading = false
    }

    fun load(name: String, data: MutableMap<String, Any?>?): T {
        val profile = createProfile()
        updateValue(profile, data)
        profiles[name] = profile
        return profile
    }

    fun selectDefault() {
        selected = profiles[DEFAULT_PROFILE_NAME] ?: createProfile()
    }

    fun createProfile(description: String? = null): T
    fun createProfile(name: String = DEFAULT_PROFILE_NAME, description: String? = null): T {
        val profile = createProfile(description)
        profiles[name] = profile
        saveAndWatch(profile)

        return profile
    }

    fun initDefaultProfile(): T {
        profiles[DEFAULT_PROFILE_NAME]?.let { return it }
        val profile = createProfile()
        profiles[DEFAULT_PROFILE_NAME] = profile
        saveAndWatch(profile)
        this.selected = profile
        return profile
    }

    fun saveAndWatch(profile: T) {
        save(profile)
        val name = profile.name
        if (RunConfiguration.PROFILES_HOT_RELOADING) {
            watchProfile(name)
        }
    }

    fun serialize(profile: T): Map<String, Any?> {
        return Jackson.MAPPER.convertValue(profile, Jackson.JSON_MAP_TYPE)
    }

    fun deleteAsync(profile: T) {
        if (saveLock.isLocked) {
            return
        }
        DefaultThreadPool += { delete(profile) }
    }

    fun canDelete(profile: T): Boolean {
        return profiles.size > 1
    }

    fun delete(profile: T) {
        saveLock.lock()
        if (!canDelete(profile)) {
            throw IllegalStateException("Can not delete $profile")
        }
        try {
            val name = profile.name
            profiles.remove(name)
            if (selected == profile) {
                selected = profiles.iterator().next().value
            }
            val file = File(getPath(name))
            if (file.exists()) {
                if (!file.delete() || file.exists()) {
                    throw IOException("Can not delete $file")
                }
                val parent = file.parentFile
                if (parent.list()?.isEmpty() == true) {
                    parent.delete()
                }
            }
            // ToDo: FileWatcherService.unregister(file)
        } catch (exception: Exception) {
            exception.printStackTrace()
            exception.crash()
        } finally {
            saveLock.unlock()
        }
    }

    fun saveAsync(profile: T) {
        if (saveLock.isLocked) {
            return
        }
        DefaultThreadPool += { save(profile) }
    }

    fun save(profile: T) {
        saveLock.lock()
        try {
            val data = serialize(profile)
            val jsonString = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data)

            val profileFile = File(getPath(profile.name))
            profile.ignoreNextReload = true
            FileUtil.safeSaveToFile(profileFile, jsonString)
            profile.saved = true
        } catch (exception: Exception) {
            exception.printStackTrace()
            exception.crash()
        } finally {
            saveLock.unlock()
        }
    }

    fun getName(profile: T): String {
        return profiles.getKey(profile) ?: "Unknown profile"
    }

    fun load(selected: String?) {
        val baseDirectory = baseDirectory
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs()
            // ToDo: Skip further processing
        }
        if (!baseDirectory.isDirectory) {
            throw IOException("${baseDirectory.path} is not an directory!")
        }
        val profileNames = baseDirectory.list { current, name -> File(current, name).isDirectory }?.toMutableSet() ?: throw IOException("Can not create a list of profiles in ${baseDirectory.path}")

        for (profileName in profileNames) {
            val path = getPath(profileName, baseDirectory)
            val (saveFile, json) = readAndMigrate(path)
            if (json == null) {
                continue
            }
            val profile: T
            try {
                profile = load(profileName, json)
            } catch (exception: Throwable) {
                throw ProfileLoadException(path, exception)
            }
            if (saveFile) {
                profile.saved = false
                save(profile)
            }
            if (RunConfiguration.PROFILES_HOT_RELOADING) {
                watchProfile(profileName, File(path))
            }
        }

        this.selected = profiles[selected]?.let { return@let it } ?: initDefaultProfile()

        Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Loaded ${profiles.size} $namespace profiles!" }
    }

    fun readAndMigrate(path: String): Pair<Boolean, MutableMap<String, Any?>?> {
        var saveFile = false
        val json: MutableMap<String, Any?>?
        val jsonString = tryCatch(FileNotFoundException::class.java) { File(path).read() }
        if (jsonString != null) {
            json = Jackson.MAPPER.readValue(jsonString, Jackson.JSON_MAP_TYPE)!!
            val version = json["version"]?.toInt() ?: throw IllegalArgumentException("Can not find version attribute in profile: $path")
            if (version > latestVersion) {
                throw IllegalStateException("Your profile ($path) was created with a newer version of minosoft. Expected $version <= $latestVersion!")
            }
            if (version < latestVersion) {
                for (toMigrate in version until latestVersion) {
                    migrate(toMigrate, json)
                }
                Log.log(LogMessageType.PROFILES, LogLevels.INFO) { "Migrated profile ($path) from version $version to $latestVersion" }
                json["version"] = latestVersion
                saveFile = true
            }
        } else {
            json = null
            saveFile = true
        }

        return Pair(saveFile, json)
    }

    fun watchProfile(profileName: String, path: File = File(getPath(profileName))) {
        FileWatcherService.register(FileWatcher(path.toPath(), arrayOf(StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE)) { _, it ->
            val profile = profiles[profileName] ?: return@FileWatcher
            if (profile.ignoreNextReload) {
                profile.ignoreNextReload = false
                return@FileWatcher
            }
            try {
                val data = readAndMigrate(path.path).second
                updateValue(profile, data)
            } catch (exception: Exception) {
                exception.printStackTrace()
                exception.crash()
            } finally {
                profile.reloading = false
            }
            Log.log(LogMessageType.PROFILES, LogLevels.INFO) { "Reloaded profile: $profileName ($it)" }
        })
    }

    companion object {
        const val DEFAULT_PROFILE_NAME = "Default"

        val PROFILE_REGEX = "[\\w_ ]{1,32}".toRegex()
    }
}
