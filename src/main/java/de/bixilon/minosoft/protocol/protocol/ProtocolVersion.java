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

package de.bixilon.minosoft.protocol.protocol;

import java.util.TreeMap;

public enum ProtocolVersion {
    VERSION_1_7_10(new Protocol_1_7_10()),
    VERSION_1_8(new Protocol_1_8()),
    VERSION_1_9_4(new Protocol_1_9_4()),
    VERSION_1_10(new Protocol_1_10()),
    VERSION_1_11_2(new Protocol_1_11_2()),
    VERSION_1_12_2(new Protocol_1_12_2()),
    VERSION_1_13_2(new Protocol_1_13_2()),
    VERSION_1_14_4(new Protocol_1_14_4()),
    VERSION_1_15_2(new Protocol_1_15_2());

    public static final TreeMap<Integer, ProtocolVersion> versionMapping = new TreeMap<>();

    public static final ProtocolVersion[] versionMappingArray;

    static {
        for (ProtocolVersion v : values()) {
            versionMapping.put(v.getVersion(), v);
        }
        versionMappingArray = new ProtocolVersion[values().length];
        int counter = 0;
        for (ProtocolVersion v : versionMapping.values()) {
            versionMappingArray[counter] = v;
            counter++;
        }
    }


    private final int version;
    private final Protocol protocol;

    ProtocolVersion(Protocol protocol) {
        this.protocol = protocol;
        this.version = protocol.getProtocolVersion();
    }

    public static ProtocolVersion byId(int protocolNumber) {
        for (ProtocolVersion v : values()) {
            if (v.getVersion() == protocolNumber) {
                return v;
            }
        }
        return null;
    }

    public int getVersion() {
        return version;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public int getPacketCommand(Packets.Serverbound p) {
        return protocol.getPacketCommand(p);
    }

    public String getName() {
        return protocol.getName();
    }

}
