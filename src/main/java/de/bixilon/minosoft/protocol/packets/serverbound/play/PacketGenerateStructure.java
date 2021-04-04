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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W22A;

public class PacketGenerateStructure implements PlayServerboundPacket {
    private final Vec3i position;
    private final int levels;
    private final boolean keepJigsaw;

    public PacketGenerateStructure(Vec3i position, int levels, boolean keepJigsaw) {
        this.position = position;
        this.levels = levels;
        this.keepJigsaw = keepJigsaw;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writePosition(this.position);
        buffer.writeVarInt(this.levels);
        if (buffer.getVersionId() <= V_20W22A) {
            buffer.writeBoolean(this.keepJigsaw);
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending generate structure packet (position=%s, levels=%d, keepJigsaw=%s)", this.position, this.levels, this.keepJigsaw));
    }
}
