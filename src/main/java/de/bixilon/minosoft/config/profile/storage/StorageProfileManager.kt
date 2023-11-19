/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.storage

import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.collections.CollectionUtil.mutableBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.observer.map.bi.BiMapObserver.Companion.observedBiMap
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.assets.util.FileUtil.mkdirParent
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.storage.ProfileIOUtil.isValidName
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.io.path.absolutePathString


abstract class StorageProfileManager<P : Profile> : Iterable<P>, Identified {
    private val jacksonType = Jackson.MAPPER.typeFactory.constructType(type.clazz)


    abstract val latestVersion: Int
    abstract val type: ProfileType<P>

    override val identifier get() = type.identifier

    val profiles: AbstractMutableBiMap<String, P> by observedBiMap(mutableBiMapOf())
    var selected: P by observed(unsafeNull())


    open fun migrate(version: Int, data: MutableJsonObject) = Unit
    open fun migrate(data: MutableJsonObject): Int {
        val version = data["version"]?.toInt() ?: throw IllegalArgumentException("Data has no version set!")
        when {
            version == latestVersion -> return -1
            version > latestVersion -> throw IllegalArgumentException("Profile was created with a newer version!")
            version < latestVersion -> {
                for (version in version until latestVersion) {
                    migrate(version, data)
                }
                return version
            }
        }
        Broken()
    }

    private fun createDefault() {
        val default = create(DEFAULT_NAME)
        selected = default
    }

    private fun loadAll() {
        val root = RunConfiguration.CONFIG_DIRECTORY.resolve(identifier.namespace).resolve(identifier.path).toFile()
        if (!root.exists()) {
            root.mkdirs()
            return createDefault()
        }
        var selected = DEFAULT_NAME // root.resolve("selected")
        if (!selected.isValidName()) selected = DEFAULT_NAME
        val files = root.listFiles() ?: return createDefault()

        for (file in files) {
            if (!file.name.endsWith(".json")) continue
            val name = file.name.removeSuffix(".json")
            if (!name.isValidName()) {
                Log.log(LogMessageType.PROFILES, LogLevels.WARN) { "Not loading $file: Invalid name!" }
                continue
            }
            val profile = load(name, file)
            profiles[name] = profile
        }

        this.selected = this[selected] ?: create(selected)
    }

    private fun load(name: String, path: File): P {
        val content = FileInputStream(path).readJsonObject(true).toMutableMap() // TODO: is copy needed?
        val storage = FileStorage(name, this, path.absolutePath)
        return load(storage, content)
    }

    fun load() {
        loadAll()
    }

    fun load(storage: FileStorage, data: MutableJsonObject): P {
        val profile = type.create(storage)
        update(profile, data)
        return profile
    }

    fun update(profile: P, data: MutableJsonObject) {
        val storage = profile.storage.nullCast<FileStorage>() ?: throw IllegalArgumentException("Storage not set!")
        val migrated = migrate(data)
        if (migrated >= 0) {
            Log.log(LogMessageType.PROFILES, LogLevels.INFO) { "Profile ${storage.name} (type=$identifier) was migrated from version $migrated to $latestVersion" }
            storage.invalidate()
        }
        profile.lock.lock()
        storage.updating = true

        val injectable = InjectableValues.Std()
        injectable.addValue(type.clazz, profile)
        Jackson.MAPPER
            .readerForUpdating(profile)
            .with(injectable)
            .readValue<P>(Jackson.MAPPER.valueToTree(data) as JsonNode)

        storage.updating = false
        storage.invalid = false
        profile.lock.unlock()
    }

    fun create(name: String): P {
        if (!name.isValidName()) throw IllegalArgumentException("Invalid profile name!")
        val path = RunConfiguration.CONFIG_DIRECTORY.resolve(identifier.namespace).resolve(identifier.path).resolve("$name.json")
        val storage = FileStorage(name, this, path.absolutePathString())
        val profile = type.create(storage)
        this.profiles[name] = profile

        storage.invalidate()

        return profile
    }

    fun save(profile: P) {
        val storage = profile.storage?.nullCast<FileStorage>() ?: throw IllegalArgumentException("Storage unset!")
        if (!storage.invalid) return
        val path = File(storage.path)
        path.mkdirParent()

        profile.lock.acquire()
        val node = Jackson.MAPPER.valueToTree<ObjectNode>(profile)
        node.put("version", latestVersion)
        val string = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node)
        val stream = FileOutputStream(path)
        stream.write(string.encodeNetwork())
        stream.close()

        storage.invalid = false

        profile.lock.release()
    }

    fun delete(name: String) = Unit
    fun delete(profile: P) = Unit
    operator fun get(name: String): P? {
        return profiles[name]
    }


    override fun iterator(): Iterator<P> {
        return profiles.values.iterator()
    }

    companion object {
        const val DEFAULT_NAME = "Default"

    }
}
