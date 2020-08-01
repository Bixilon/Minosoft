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

package de.bixilon.minosoft.game.datatypes.objectLoader.versions;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class Version {
    final String versionName;
    final int protocolVersion;
    VersionMapping mapping;

    final HashBiMap<Packets.Serverbound, Integer> serverboundPacketMapping;
    final HashBiMap<Packets.Clientbound, Integer> clientboundPacketMapping;


    public Version(String versionName, int protocolVersion, HashBiMap<Packets.Serverbound, Integer> serverboundPacketMapping, HashBiMap<Packets.Clientbound, Integer> clientboundPacketMapping) {
        this.versionName = versionName;
        this.protocolVersion = protocolVersion;
        this.serverboundPacketMapping = serverboundPacketMapping;
        this.clientboundPacketMapping = clientboundPacketMapping;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Packets.Clientbound getPacketByCommand(int command) { // state must be play!
        return clientboundPacketMapping.inverse().get(command);
    }

    public int getCommandByPacket(Packets.Serverbound packet) {
        return serverboundPacketMapping.get(packet);
    }

    public int getCommandByPacket(Packets.Clientbound packet) {
        return clientboundPacketMapping.get(packet);
    }


    public HashBiMap<Packets.Clientbound, Integer> getClientboundPacketMapping() {
        return clientboundPacketMapping;
    }

    public HashBiMap<Packets.Serverbound, Integer> getServerboundPacketMapping() {
        return serverboundPacketMapping;
    }

    @Override
    public int hashCode() {
        return getProtocolVersion();
    }

    public VersionMapping getMapping() {
        return mapping;
    }

    public void setMapping(VersionMapping mapping) {
        this.mapping = mapping;
    }
}
