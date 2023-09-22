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

package de.bixilon.minosoft.protocol.versions

import de.bixilon.minosoft.protocol.packets.registry.PacketMapping
import de.bixilon.minosoft.protocol.protocol.PacketDirections
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object Versions : Iterable<Version> {
    private val name: MutableMap<String, Version> = mutableMapOf()
    private val id: Int2ObjectOpenHashMap<Version> = Int2ObjectOpenHashMap()
    private val protocol: Int2ObjectOpenHashMap<Version> = Int2ObjectOpenHashMap()
    val AUTOMATIC = Version("Automatic", -1, -1, VersionTypes.RELEASE, PacketMapping(PacketDirections.SERVER_TO_CLIENT), PacketMapping(PacketDirections.CLIENT_TO_SERVER))

    fun register(version: Version) {
        name.put(version.name, version)?.let { throw IllegalStateException("Duplicated version name: ${version.name}") }
        id.put(version.versionId, version)?.let { throw IllegalStateException("Duplicated version id: ${version.name}") }
        protocol.put(version.protocolId, version)?.let { throw IllegalStateException("Duplicated protocol id: ${version.name}") }
    }

    operator fun get(name: String?): Version? {
        if (name == "automatic") {
            return AUTOMATIC
        }
        return this.name[name]
    }

    fun getById(versionId: Int): Version? {
        return id[versionId]
    }

    fun getByProtocol(protocolId: Int): Version? {
        return protocol[protocolId]
    }

    override fun iterator(): Iterator<Version> {
        return name.values.iterator()
    }
}
