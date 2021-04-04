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

import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketPlayerBlockPlacement implements PlayServerboundPacket {
    private final Vec3i position;
    private final byte direction;
    private final float cursorX;
    private final float cursorY;
    private final float cursorZ;
    ItemStack item;
    Hands hand;
    boolean insideBlock;

    public PacketPlayerBlockPlacement(Vec3i position, byte direction, ItemStack item, float cursorX, float cursorY, float cursorZ) {
        this.position = position;
        this.direction = direction;
        this.item = item;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
    }

    // >= 1.9
    public PacketPlayerBlockPlacement(Vec3i position, byte direction, Hands hand, float cursorX, float cursorY, float cursorZ) {
        this.position = position;
        this.direction = direction;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
    }

    // >= 1.14
    public PacketPlayerBlockPlacement(Vec3i position, byte direction, Hands hand, float cursorX, float cursorY, float cursorZ, boolean insideBlock) {
        this.position = position;
        this.direction = direction;
        this.hand = hand;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
        this.insideBlock = insideBlock;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        if (buffer.getVersionId() >= V_19W03A) {
            buffer.writeVarInt(this.hand.ordinal());
        }
        if (buffer.getVersionId() < V_14W04A) {
            buffer.writeVec3iByte(this.position);
        } else {
            buffer.writePosition(this.position);
        }
        if (buffer.getVersionId() < V_15W31A) {
            buffer.writeByte(this.direction);
            buffer.writeItemStack(this.item);
        } else {
            buffer.writeVarInt(this.direction);
            if (buffer.getVersionId() < V_19W03A) {
                buffer.writeVarInt(this.hand.ordinal());
            }
        }

        if (buffer.getVersionId() >= V_19W03A) {
            buffer.writeBoolean(this.insideBlock);
        }

        if (buffer.getVersionId() < V_16W39C) {
            buffer.writeByte((byte) (this.cursorX * 15.0F));
            buffer.writeByte((byte) (this.cursorY * 15.0F));
            buffer.writeByte((byte) (this.cursorZ * 15.0F));
        } else {
            buffer.writeFloat(this.cursorX);
            buffer.writeFloat(this.cursorY);
            buffer.writeFloat(this.cursorZ);
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Send player block placement (position=%s, direction=%d, item=%s, cursor=%s %s %s)", this.position, this.direction, this.item, this.cursorX, this.cursorY, this.cursorZ));
    }
}
