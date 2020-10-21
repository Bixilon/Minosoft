/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.versions;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;

public class Version {
    final String versionName;
    final int protocolVersion;
    int sortingId;
    final HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping;
    final HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping;
    VersionMapping mapping;
    boolean isGettingLoaded;

    public Version(String versionName, int protocolVersion, int sortingId, HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping, HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping) {
        this.versionName = versionName;
        this.protocolVersion = protocolVersion;
        this.sortingId = sortingId;
        this.serverboundPacketMapping = serverboundPacketMapping;
        this.clientboundPacketMapping = clientboundPacketMapping;
    }

    public Packets.Clientbound getPacketByCommand(ConnectionStates state, int command) {
        if (clientboundPacketMapping.containsKey(state) && clientboundPacketMapping.get(state).containsValue(command)) {
            return clientboundPacketMapping.get(state).inverse().get(command);
        }
        return null;
    }

    public Integer getCommandByPacket(Packets.Serverbound packet) {
        if (serverboundPacketMapping.containsKey(packet.getState()) && serverboundPacketMapping.get(packet.getState()).containsKey(packet)) {
            return serverboundPacketMapping.get(packet.getState()).get(packet);
        }
        return null;
    }

    public HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> getClientboundPacketMapping() {
        return clientboundPacketMapping;
    }

    public HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> getServerboundPacketMapping() {
        return serverboundPacketMapping;
    }

    public VersionMapping getMapping() {
        return mapping;
    }

    public void setMapping(VersionMapping mapping) {
        this.mapping = mapping;
    }

    public boolean isGettingLoaded() {
        return isGettingLoaded;
    }

    public void setGettingLoaded(boolean gettingLoaded) {
        isGettingLoaded = gettingLoaded;
    }

    public boolean isFlattened() {
        return protocolVersion > ProtocolDefinition.FLATTING_VERSION_ID;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getSortingId() {
        return sortingId;
    }

    public void setSortingId(int sortingId) {
        this.sortingId = sortingId;
    }

    @Override
    public int hashCode() {
        return getProtocolVersion();
    }

    public int getProtocolVersion() {
        return protocolVersion;
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
        return getVersionName().equals(versionName);
    }

    @Override
    public String toString() {
        return getVersionName();
    }
}
