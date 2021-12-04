package de.bixilon.minosoft.config.profile

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.util.delegate.BackingDelegate
import de.bixilon.minosoft.config.profile.util.delegate.MapDelegate
import de.bixilon.minosoft.config.profile.util.delegate.ProfileDelegate
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.filewatcher.FileWatcher
import de.bixilon.minosoft.util.filewatcher.FileWatcherService
import de.bixilon.minosoft.util.json.jackson.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.collections.MapChangeListener
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.locks.ReentrantLock


interface ProfileManager<T : Profile> {
    val namespace: ResourceLocation
    val latestVersion: Int
    val saveLock: ReentrantLock
    val profileClass: Class<T>

    val profiles: HashBiMap<String, T>
    var selected: T

    @Deprecated("Should not be accessed") var currentLoadingPath: String?

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


    fun load(name: String, data: MutableMap<String, Any?>?): T {
        currentLoadingPath = name
        val profile = if (data == null) {
            return createDefaultProfile(name)
        } else {
            Jackson.MAPPER.convertValue(data, profileClass)
        }
        profile.saved = true
        profiles[name] = profile
        currentLoadingPath = null
        return profile
    }

    fun <V> delegate(value: V, checkEquals: Boolean = true, verify: ((V) -> Unit)? = null): ProfileDelegate<V> {
        return ProfileDelegate(value, checkEquals, this, currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify)
    }

    fun <V> backingDelegate(checkEquals: Boolean = true, verify: ((V) -> Unit)? = null, getter: () -> V, setter: (V) -> Unit): BackingDelegate<V> {
        return object : BackingDelegate<V>(checkEquals, this, currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify) {
            override fun get(): V = getter()

            override fun set(value: V) = setter(value)
        }
    }

    fun <K, V> mapDelegate(checkEquals: Boolean = true, verify: ((MapChangeListener.Change<out K, out V>) -> Unit)? = null): MapDelegate<K, V> {
        return MapDelegate(profileManager = this, profileName = currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify = verify)
    }

    fun selectDefault() {
        selected = profiles[DEFAULT_PROFILE_NAME] ?: createDefaultProfile()
    }

    fun createDefaultProfile(name: String = DEFAULT_PROFILE_NAME): T

    fun initDefaultProfile() {
        val profile = createDefaultProfile()
        this.selected = profile
        save(profile)
    }


    fun serialize(profile: T): Map<String, Any?> {
        return Jackson.MAPPER.convertValue(profile, Jackson.JSON_MAP_TYPE)
    }


    fun save(profile: T) {
        if (saveLock.isLocked) {
            return
        }
        DefaultThreadPool += {
            saveLock.lock()
            try {
                val data = serialize(profile)
                val jsonString = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data)

                val profileFile = File(getPath(getName(profile)))
                KUtil.safeSaveToFile(profileFile, jsonString)
                profile.saved = true
            } catch (exception: Exception) {
                exception.printStackTrace()
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
        for (profileName in profileNames) {
            val path = getPath(profileName, baseDirectory)
            val (saveFile, json) = readAndMigrate(path)
            val profile: T
            try {
                profile = load(profileName, json)
                if (RunConfiguration.PROFILES_HOT_RELOADING) {
                    watchProfile(profileName, File(path))
                }
            } catch (exception: Throwable) {
                throw ProfileLoadException(path, exception)
            }
            if (saveFile) {
                profile.saved = false
                save(profile)
            }
        }

        if (selected != null) {
            profiles[selected]?.let { this.selected = it } ?: selectDefault()
        }

        Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Loaded ${profiles.size} $namespace profiles!" }
    }

    fun readAndMigrate(path: String): Pair<Boolean, MutableMap<String, Any?>?> {
        var saveFile = false
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

    fun watchProfile(profileName: String, path: File) {
        FileWatcherService.register(FileWatcher(path.toPath(), arrayOf(StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE)) { _, it ->
            val profile = profiles[profileName] ?: return@FileWatcher
            try {
                val data = readAndMigrate(path.path).second
                val dataString = Jackson.MAPPER.writeValueAsString(data)
                profile.reloading = true
                Jackson.MAPPER.readerForUpdating(profile).readValue<T>(dataString)
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
    }
}
