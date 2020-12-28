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
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;

public class Version {
    private final int versionId;
    private final int protocolId;
    private final HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping;
    private final HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping;
    String versionName;
    VersionMapping mapping;
    boolean isGettingLoaded;

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
}
