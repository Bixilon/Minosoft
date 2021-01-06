/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.data.Mappings;
import de.bixilon.minosoft.data.assets.AssetsManager;
import de.bixilon.minosoft.data.assets.Resources;
import de.bixilon.minosoft.data.locale.minecraft.MinecraftLocaleManager;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogLevels;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;

public class Version {
    private final int versionId;
    private final int protocolId;
    private final HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping;
    private final HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping;
    String versionName;
    VersionMapping mapping;
    boolean isGettingLoaded;
    private AssetsManager assetsManager;
    private MinecraftLocaleManager localeManager;

    public Version(String versionName, int versionId, int protocolId, HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping, HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping) {
        this.versionName = versionName;
        this.versionId = versionId;
        this.protocolId = protocolId;
        this.serverboundPacketMapping = serverboundPacketMapping;
        this.clientboundPacketMapping = clientboundPacketMapping;
    }

    public Packets.Clientbound getPacketByCommand(ConnectionStates state, int command) {
        if (this.clientboundPacketMapping.containsKey(state) && this.clientboundPacketMapping.get(state).containsValue(command)) {
            return this.clientboundPacketMapping.get(state).inverse().get(command);
        }
        return null;
    }

    public Integer getCommandByPacket(Packets.Serverbound packet) {
        if (this.serverboundPacketMapping.containsKey(packet.getState()) && this.serverboundPacketMapping.get(packet.getState()).containsKey(packet)) {
            return this.serverboundPacketMapping.get(packet.getState()).get(packet);
        }
        return null;
    }

    public HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> getClientboundPacketMapping() {
        return this.clientboundPacketMapping;
    }

    public HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> getServerboundPacketMapping() {
        return this.serverboundPacketMapping;
    }

    public VersionMapping getMapping() {
        return this.mapping;
    }

    public void setMapping(VersionMapping mapping) {
        this.mapping = mapping;
    }

    public boolean isGettingLoaded() {
        return this.isGettingLoaded;
    }

    public void setGettingLoaded(boolean gettingLoaded) {
        this.isGettingLoaded = gettingLoaded;
    }

    public boolean isFlattened() {
        return this.versionId >= ProtocolDefinition.FLATTING_VERSION_ID;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionId() {
        return this.versionId;
    }

    @Override
    public int hashCode() {
        return getVersionId();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        return getVersionName().equals(this.versionName);
    }

    @Override
    public String toString() {
        return getVersionName();
    }

    public int getProtocolId() {
        return this.protocolId;
    }

    public boolean isLoaded() {
        return getMapping() != null && getMapping().isFullyLoaded();
    }

    public AssetsManager getAssetsManager() {
        return this.assetsManager;
    }

    public void setAssetsManager(AssetsManager assetsManager) {
        this.assetsManager = assetsManager;
    }

    public MinecraftLocaleManager getLocaleManager() {
        return this.localeManager;
    }

    public void initializeAssetManger(CountUpAndDownLatch latch) throws Exception {
        if (this.assetsManager == null) {
            this.assetsManager = new AssetsManager(Minosoft.getConfig().getBoolean(ConfigurationPaths.BooleanPaths.DEBUG_VERIFY_ASSETS), Resources.getAssetVersionByVersion(this));
            this.assetsManager.downloadAllAssets(latch);
            this.localeManager = new MinecraftLocaleManager(this);
        }
    }

    public void loadMappings(CountUpAndDownLatch latch) throws IOException {
        if (isLoaded()) {
            // already loaded
            return;
        }
        Version preFlatteningVersion = Versions.getVersionById(ProtocolDefinition.PRE_FLATTENING_VERSION_ID);
        if (!isFlattened() && this != preFlatteningVersion && !preFlatteningVersion.isLoaded()) {
            // no matter what, we need the version mapping for all pre flattening versions
            preFlatteningVersion.loadMappings(latch);
        }
        if (isGettingLoaded()) {
            // async: we don't wanna load this version twice, skip
            return;
        }
        latch.countUp();
        this.isGettingLoaded = true;
        Log.verbose(String.format("Loading mappings for version %s...", this));
        long startTime = System.currentTimeMillis();

        HashMap<String, JsonObject> files;
        try {
            files = Util.readJsonTarStream(AssetsManager.readAssetAsStreamByHash(this.assetsManager.getAssetVersion().getMinosoftMappings()));
        } catch (Exception e) {
            // should not happen, but if this version is not flattened, we can fallback to the flatten mappings. Some things might not work...
            Log.printException(e, LogLevels.VERBOSE);
            if (isFlattened() || getVersionId() == ProtocolDefinition.FLATTING_VERSION_ID) {
                throw e;
            }
            files = new HashMap<>();
        }

        latch.addCount(Mappings.VALUES.length);
        for (Mappings mapping : Mappings.VALUES) {
            JsonObject data = null;
            if (files.containsKey(mapping.getFilename() + ".json")) {
                data = files.get(mapping.getFilename() + ".json");
            }
            if (data == null) {
                loadVersionMappings(mapping, ProtocolDefinition.DEFAULT_MOD, null);
                latch.countDown();
                continue;
            }
            for (String mod : data.keySet()) {
                loadVersionMappings(mapping, mod, data.getAsJsonObject(mod));
            }
            latch.countDown();
        }
        if (!files.isEmpty()) {
            Log.verbose(String.format("Loaded mappings for version %s in %dms (%s)", this, (System.currentTimeMillis() - startTime), getVersionName()));
        } else {
            Log.verbose(String.format("Could not load mappings for version %s. Some features will be unavailable.", this));
        }
        this.isGettingLoaded = false;
        latch.countDown();
    }

    public void loadVersionMappings(Mappings type, String mod, @Nullable JsonObject data) {
        if (this.mapping == null) {
            this.mapping = new VersionMapping(this);
        }
        this.mapping.load(type, mod, data, this);

        if (getVersionId() == ProtocolDefinition.PRE_FLATTENING_VERSION_ID && Versions.PRE_FLATTENING_MAPPING == null) {
            Versions.PRE_FLATTENING_MAPPING = this.mapping;
        }
    }

}
