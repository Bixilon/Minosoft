package de.bixilon.minosoft.config.config2

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.config2.config.Profile
import de.bixilon.minosoft.config.config2.util.ConfigDelegate
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.json.jackson.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock


interface ProfileManager<T : Profile> {
    val namespace: ResourceLocation
    val latestVersion: Int
    val saveLock: ReentrantLock

    val profiles: HashBiMap<String, T>
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
    fun load(name: String, data: MutableMap<String, Any?>?): T


    fun <V> delegate(value: V, checkEquals: Boolean = true): ConfigDelegate<V>

    fun selectDefault()
    fun createDefaultProfile(): T

    fun initDefaultProfile() {
        val profile = createDefaultProfile()
        this.selected = profile
        save(profile)
    }

    fun serialize(profile: T): Map<String, Any?>

    fun save(profile: T) {
        saveLock.lock()
        DefaultThreadPool += {
            try {
                val data = serialize(profile)
                val jsonString = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data)

                val profileFile = File(getPath(getName(profile)))
                val parent = profileFile.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                    if (!parent.isDirectory) {
                        throw IOException("Could not create profile folder: ${parent.path}")
                    }
                }

                val tempFile = File("${profileFile.path}.tmp")
                if (tempFile.exists()) {
                    if (!tempFile.delete()) {
                        throw IOException("Could not delete $tempFile!")
                    }
                }
                FileWriter(tempFile).apply {
                    write(jsonString)
                    close()
                }
                if (profileFile.exists() && !profileFile.delete()) {
                    throw IOException("Could not delete $profileFile!")
                }
                if (!tempFile.renameTo(profileFile)) {
                    throw IOException("Could not move $tempFile to $profileFile!")
                }
            } catch (exception: Exception) {
                exception.crash()
            } finally {
                saveLock.unlock()
            }
        }
    }

    fun getName(profile: T): String {
        return profiles.inverse()[profile] ?: "Unknown profile"
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
        val profileNames = baseDirectory.list { current, name -> File(current, name).isDirectory } ?: throw IOException("Can not create a list of profiles in ${baseDirectory.path}")
        if (selected == null || profileNames.isEmpty()) {
            initDefaultProfile()
        }
        var migrated = false
        for (profileName in profileNames) {
            val path = getPath(profileName, baseDirectory)
            val json: MutableMap<String, Any?>?
            val jsonString = KUtil.tryCatch(FileNotFoundException::class.java) { Util.readFile(path) }
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
                    Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Migrated profile ($path) from $version to $latestVersion" }
                    json["version"] = latestVersion
                    migrated = true
                }
            } else {
                json = null
            }

            val profile = load(profileName, json)
            if (migrated) {
                save(profile)
            }
        }

        if (selected != null) {
            profiles[selected]?.let { this.selected = it } ?: selectDefault()
        }

        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Loaded ${profiles.size} $namespace profiles!" }
    }

    companion object {
        const val DEFAULT_PROFILE_NAME = "Default"
    }
}
