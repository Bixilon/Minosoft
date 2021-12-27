package de.bixilon.minosoft.config.profile

import com.google.common.collect.HashBiMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.ExceptionUtil.tryCatch
import de.bixilon.kutil.file.FileUtil
import de.bixilon.kutil.file.watcher.FileWatcher
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.config.profile.delegate.delegate.BackingDelegate
import de.bixilon.minosoft.config.profile.delegate.delegate.ProfileDelegate
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.ListDelegateProfile
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.MapDelegateProfile
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.SetDelegateProfile
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
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
    val profileSelectable: Boolean
        get() = true
    val icon: Ikon
        get() = FontAwesomeSolid.QUESTION

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
            return createProfile(name)
        } else {
            Jackson.MAPPER.convertValue(data, profileClass)
        }
        profile.saved = true
        profiles[name] = profile
        currentLoadingPath = null
        return profile
    }

    fun <V> delegate(value: V, verify: ((V) -> Unit)? = null): ProfileDelegate<V> {
        return ProfileDelegate(value, this, currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify)
    }

    fun <V> backingDelegate(verify: ((V) -> Unit)? = null, getter: () -> V, setter: (V) -> Unit): BackingDelegate<V> {
        return object : BackingDelegate<V>(this, currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify) {
            override fun get(): V = getter()

            override fun set(value: V) = setter(value)
        }
    }

    fun <K, V> mapDelegate(default: MutableMap<K, V> = mutableMapOf(), verify: ((MapChangeListener.Change<out K, out V>) -> Unit)? = null): MapDelegateProfile<K, V> {
        return MapDelegateProfile(FXCollections.synchronizedObservableMap(FXCollections.observableMap(default)), profileManager = this, profileName = currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify = verify)
    }

    fun <V> listDelegate(default: MutableList<V> = mutableListOf(), verify: ((ListChangeListener.Change<out V>) -> Unit)? = null): ListDelegateProfile<V> {
        return ListDelegateProfile(FXCollections.synchronizedObservableList(FXCollections.observableList(default)), profileManager = this, profileName = currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify = verify)
    }

    fun <V> setDelegate(default: MutableSet<V> = mutableSetOf(), verify: ((SetChangeListener.Change<out V>) -> Unit)? = null): SetDelegateProfile<V> {
        return SetDelegateProfile(FXCollections.synchronizedObservableSet(FXCollections.observableSet(default)), profileManager = this, profileName = currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify = verify)
    }

    fun selectDefault() {
        selected = profiles[DEFAULT_PROFILE_NAME] ?: createProfile()
    }

    fun createProfile(name: String = DEFAULT_PROFILE_NAME, description: String? = null): T

    fun initDefaultProfile(): T {
        profiles[DEFAULT_PROFILE_NAME]?.let { return it }
        val profile = createProfile()
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
        val jsonString = tryCatch(FileNotFoundException::class.java) { Util.readFile(path) }
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

        val PROFILE_REGEX = "(\\w| |\\d|_){1,32}".toRegex()
    }
}
