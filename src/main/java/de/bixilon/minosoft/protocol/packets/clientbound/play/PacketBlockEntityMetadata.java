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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketBlockEntityMetadata implements ClientboundPacket {
    BlockPosition position;
    Action action;
    CompoundTag nbt;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                position = buffer.readBlockPositionShort();
                action = Action.byId(buffer.readByte());
                nbt = buffer.readNBT();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving blockEntityMeta (position=%s, action=%s)", position.toString(), action.name()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public BlockPosition getPosition() {
        return position;
    }

    public Action getAction() {
        return action;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public enum Action {
        SPAWNER(1),
        COMMAND_BLOCK(2),
        SKULL(3),
        FLOWER_POT(4);

        final int id;

        Action(int id) {
            this.id = id;
        }

        public static Action byId(int id) {
            for (Action g : values()) {
                if (g.getId() == id) {
                    return g;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
