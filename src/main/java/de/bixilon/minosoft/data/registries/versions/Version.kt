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
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.language.LanguageManager
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.PacketTypes.C2S
import de.bixilon.minosoft.protocol.protocol.PacketTypes.S2C
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound

data class Version(
    var versionName: String,
    val versionId: Int,
    val protocolId: Int,
    val c2SPacketMapping: Map<ConnectionStates, HashBiMap<C2S, Int>>,
    val s2CPacketMapping: Map<ConnectionStates, HashBiMap<S2C, Int>>,
) {
    var isLoaded = false
    val registries: Registries = Registries()
    lateinit var assetsManager: MinecraftAssetsManager
    lateinit var language: LanguageManager

    fun getPacketById(state: ConnectionStates, command: Int): S2C? {
        return s2CPacketMapping[state]?.inverse()?.get(command)
    }

    fun getPacketId(packet: C2S): Int? {
        return c2SPacketMapping[packet.state]?.get(packet)
    }

    fun isFlattened(): Boolean {
        return versionId >= ProtocolDefinition.FLATTING_VERSION_ID
    }

    private fun initializeAssetManger(latch: CountUpAndDownLatch) {
        if (this::assetsManager.isInitialized) {
            return
        }
        if (!isFlattened() && versionId != ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
            assetsManager = Versions.PRE_FLATTENING_VERSION.assetsManager
            language = Versions.PRE_FLATTENING_VERSION.language
            return
        }
        assetsManager = MinecraftAssetsManager(Resources.getAssetVersionByVersion(this), Resources.getPixLyzerDataHashByVersion(this))
        assetsManager.downloadAllAssets(latch)
        language = LanguageManager.load(Minosoft.config.config.general.language, this)
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
        val startTime = System.currentTimeMillis()


        if (versionId == ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
            Versions.PRE_FLATTENING_MAPPING = registries
        } else if (!isFlattened()) {
            registries.parentRegistries = Versions.PRE_FLATTENING_MAPPING
        }
        val pixlyzerData = try {
            MBFBinaryReader(assetsManager.readAssetAsStream(Resources.getPixLyzerDataHashByVersion(this), false)).readMBF().data.asCompound()
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
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.INFO) { "Loaded registries for $versionName in ${System.currentTimeMillis() - startTime}ms" }
        } else {
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.WARN) { "Could not load registries for ${versionName}. Some features might not work." }
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
        if (other == null) {
            return false
        }
        return if (hashCode() != other.hashCode()) {
            false
        } else {
            versionName == versionName
        }
    }

    override fun toString(): String {
        return versionName
    }
}
