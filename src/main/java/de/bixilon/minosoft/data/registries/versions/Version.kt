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
package de.bixilon.minosoft.data.registries.versions

import com.google.common.collect.HashBiMap
import de.bixilon.mbf.MBFBinaryReader
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsManager
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.protocol.PacketTypes.C2S
import de.bixilon.minosoft.protocol.protocol.PacketTypes.S2C
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound

@Deprecated(message = "Some refactoring needed")
data class Version(
    var name: String,
    val versionId: Int,
    val protocolId: Int,
    val c2SPacketMapping: Map<ProtocolStates, HashBiMap<C2S, Int>>,
    val s2CPacketMapping: Map<ProtocolStates, HashBiMap<S2C, Int>>,
) {
    val sortingId: Int = (versionId == -1).decide(Int.MAX_VALUE, versionId)
    val type: VersionTypes = VersionTypes[this]
    var isLoaded = false
    val registries: Registries = Registries()
    lateinit var jarAssetsManager: JarAssetsManager
    lateinit var indexAssetsManager: IndexAssetsManager

    fun getPacketById(state: ProtocolStates, command: Int): S2C? {
        return s2CPacketMapping[state]?.inverse()?.get(command)
    }

    fun getPacketId(packet: C2S): Int? {
        return c2SPacketMapping[packet.state]?.get(packet)
    }

    fun isFlattened(): Boolean {
        return versionId >= ProtocolDefinition.FLATTING_VERSION_ID
    }

    private fun initializeAssetManger(latch: CountUpAndDownLatch) {
        if (this::jarAssetsManager.isInitialized) {
            return
        }
        if (!isFlattened() && versionId != ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
            jarAssetsManager = Versions.PRE_FLATTENING_VERSION.jarAssetsManager
            return
        }
        // ToDo
    }

    @Synchronized
    fun load(latch: CountUpAndDownLatch) {
        if (isLoaded) {
            return
        }

        if (!isFlattened() && this !== Versions.PRE_FLATTENING_VERSION && !Versions.PRE_FLATTENING_VERSION.isLoaded) {
            // no matter what, we need the version mapping for all pre flattening versions
            try {
                Versions.PRE_FLATTENING_VERSION.load(latch)
            } catch (exception: Exception) {
                Versions.PRE_FLATTENING_VERSION.unload()
                Versions.PRE_FLATTENING_MAPPING = null
                throw exception
            }
        }
        latch.inc()
        Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.INFO) { "Loading registries for $this..." }
        initializeAssetManger(latch)
        val startTime = KUtil.time


        if (versionId == ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
            Versions.PRE_FLATTENING_MAPPING = registries
        } else if (!isFlattened()) {
            registries.parentRegistries = Versions.PRE_FLATTENING_MAPPING
        }
        val pixlyzerData = try {
            MBFBinaryReader(FileUtil.readFile(FileAssetsUtil.getPath(AssetsVersionProperties[this]!!.pixlyzerHash), false)).readMBF().data.asCompound()
        } catch (exception: Throwable) {
            // should not happen, but if this version is not flattened, we can fallback to the flatten mappings. Some things might not work...
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.VERBOSE) { exception }
            if (isFlattened()) {
                throw exception
            }
            if (versionId == ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
                Versions.PRE_FLATTENING_MAPPING = null
            }
            mutableMapOf()
        }
        latch.inc()
        registries.load(this, pixlyzerData)
        latch.dec()
        if (pixlyzerData.isNotEmpty()) {
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.INFO) { "Loaded registries for $name in ${KUtil.time - startTime}ms" }
        } else {
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.WARN) { "Could not load registries for ${name}. Some features might not work." }
        }
        isLoaded = true
        latch.dec()
    }

    fun unload() {
        registries.clear()
        if (registries.parentRegistries == registries) {
            registries.parentRegistries = null
        }
        isLoaded = false
    }

    override fun hashCode(): Int {
        return versionId
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            return true
        }
        if (other !is Version) {
            return false
        }
        return if (hashCode() != other.hashCode()) {
            false
        } else {
            this.name == other.name
        }
    }

    override fun toString(): String {
        return name
    }

    val hasOffhand = versionId >= V_15W31A
}
