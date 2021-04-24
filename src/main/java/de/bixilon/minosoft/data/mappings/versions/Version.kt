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
package de.bixilon.minosoft.data.mappings.versions

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.locale.minecraft.MinecraftLocaleManager
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.PacketTypes.C2S
import de.bixilon.minosoft.protocol.protocol.PacketTypes.S2C
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log.log
import de.bixilon.minosoft.util.logging.LogMessageType

data class Version(
    var versionName: String,
    val versionId: Int,
    val protocolId: Int,
    val c2SPacketMapping: Map<ConnectionStates, HashBiMap<C2S, Int>>,
    val s2CPacketMapping: Map<ConnectionStates, HashBiMap<S2C, Int>>,
) {
    var isLoaded = false
    var isGettingLoaded = false
    val mapping: VersionMapping = VersionMapping()
    lateinit var assetsManager: MinecraftAssetsManager
    lateinit var localeManager: MinecraftLocaleManager

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
            localeManager = Versions.PRE_FLATTENING_VERSION.localeManager
            return
        }
        assetsManager = MinecraftAssetsManager(Resources.getAssetVersionByVersion(this), Resources.getPixLyzerDataHashByVersion(this))
        assetsManager.downloadAllAssets(latch)
        localeManager = MinecraftLocaleManager(this)
        localeManager.load(this, Minosoft.getConfig().config.general.language)
    }

    fun load(latch: CountUpAndDownLatch) {
        if (isLoaded || isGettingLoaded) {
            // already loaded or is getting loaded
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
        latch.countUp()
        isGettingLoaded = true
        log(LogMessageType.OTHER_DEBUG, message = String.format("Loading mappings for version %s...", this), formatting = arrayOf())
        initializeAssetManger(latch)
        val startTime = System.currentTimeMillis()


        if (versionId == ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
            Versions.PRE_FLATTENING_MAPPING = mapping
        } else if (!isFlattened()) {
            mapping.parentMapping = Versions.PRE_FLATTENING_MAPPING
        }
        val pixlyzerData = try {
            Util.readJsonFromStream(assetsManager.readAssetAsStream(Resources.getPixLyzerDataHashByVersion(this)))
        } catch (exception: Throwable) {
            // should not happen, but if this version is not flattened, we can fallback to the flatten mappings. Some things might not work...
            log(LogMessageType.OTHER_DEBUG, message = exception)
            if (isFlattened()) {
                throw exception
            }
            if (versionId == ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
                Versions.PRE_FLATTENING_MAPPING = null
            }
            JsonObject()
        }
        latch.addCount(1)
        mapping.load(this, pixlyzerData)
        latch.countDown()
        if (pixlyzerData.size() > 0) {
            log(LogMessageType.OTHER_DEBUG, message = String.format("Loaded mappings for version %s in %dms (%s)", this, (System.currentTimeMillis() - startTime), versionName), formatting = arrayOf())
        } else {
            log(LogMessageType.OTHER_DEBUG, message = String.format("Could not load mappings for version %s. Some features will be unavailable.", this), formatting = arrayOf())
        }
        isLoaded = true
        isGettingLoaded = false
        latch.countDown()
    }

    fun unload() {
        mapping.clear()
        if (mapping.parentMapping == mapping) {
            mapping.parentMapping = null
        }
        isLoaded = false
        isGettingLoaded = false
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
