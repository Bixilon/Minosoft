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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.modding.event.events.ExperienceChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketSetExperience extends ClientboundPacket {
    float bar;
    int level;
    int total;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.bar = buffer.readFloat();
        if (buffer.getVersionId() < V_14W04A) {
            this.level = buffer.readUnsignedShort();
            this.total = buffer.readUnsignedShort();
            return true;
        }
        this.level = buffer.readVarInt();
        this.total = buffer.readVarInt();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        if (connection.fireEvent(new ExperienceChangeEvent(connection, this))) {
            return;
        }

        connection.getPlayer().setLevel(getLevel());
        connection.getPlayer().setTotalExperience(getTotal());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Level update received. Now at %d levels, totally %d exp", this.level, this.total));
    }

    public float getBar() {
        return this.bar;
    }

    public int getLevel() {
        return this.level;
    }

    public int getTotal() {
        return this.total;
    }
}
