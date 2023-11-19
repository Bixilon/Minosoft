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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.collections.CollectionUtil.mutableBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.observer.map.bi.BiMapObserver.Companion.observedBiMap
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.util.json.Jackson


abstract class StorageProfileManager<P : Profile> : Iterable<P>, Identified {
    private val jacksonType = Jackson.MAPPER.typeFactory.constructType(type.clazz)


    abstract val latestVersion: Int
    abstract val type: ProfileType<P>

    override val identifier get() = type.identifier

    val profiles: AbstractMutableBiMap<String, P> by observedBiMap(mutableBiMapOf())
    var selected: P by observed(unsafeNull())


    open fun migrate(version: Int, data: MutableJsonObject) = Unit
    open fun migrate(data: MutableJsonObject) {
        val version = data["version"]?.toInt() ?: throw IllegalArgumentException("Data has no version set!")
        when {
            version == latestVersion -> return
            version > latestVersion -> throw IllegalArgumentException("Profile was created with a newer version!")
            version < latestVersion -> {
                for (version in version until latestVersion) {
                    migrate(version, data)
                }
                // TODO: log, save
            }
        }
    }

    fun load(name: String, data: MutableJsonObject) = Unit
    fun create(name: String): P = TODO()

    fun delete(name: String) = Unit
    fun delete(profile: P) = Unit
    operator fun get(name: String): P = TODO()


    override fun iterator(): Iterator<P> {
        return profiles.values.iterator()
    }

    companion object {
        const val DEFAULT_NAME = "Default"

        val NAME_REGEX = "[\\w_ ]{1,32}".toRegex()
    }
}
