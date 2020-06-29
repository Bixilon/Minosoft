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

public class PacketBlockEntityMetadata implements ClientboundPacket {
    BlockPosition position;
    Action_1_7_10 action_1_7_10;
    Action_1_8 action_1_8;
    CompoundTag nbt;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                position = buffer.readBlockPositionShort();
                action_1_7_10 = Action_1_7_10.byId(buffer.readByte());
                nbt = buffer.readNBT(true);
                return true;
            case VERSION_1_8:
                position = buffer.readPosition();
                action_1_8 = Action_1_8.byId(buffer.readByte());
                nbt = buffer.readNBT();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving blockEntityMeta (position=%s, action=%s)", position.toString(), ((action_1_7_10 == null) ? action_1_8.name() : action_1_7_10.name())));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public BlockPosition getPosition() {
        return position;
    }

    public Action_1_7_10 getAction1_7_10() {
        return action_1_7_10;
    }

    public Action_1_8 getAction1_8() {
        return action_1_8;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public enum Action_1_7_10 {
        SPAWNER(1),
        COMMAND_BLOCK(2),
        SKULL(3),
        FLOWER_POT(4);

        final int id;

        Action_1_7_10(int id) {
            this.id = id;
        }

        public static Action_1_7_10 byId(int id) {
            for (Action_1_7_10 a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum Action_1_8 {
        SPAWNER(1),
        COMMAND_BLOCK(2),
        BEACON(3),
        SKULL(4),
        FLOWER_POT(5),
        BANNER(6);

        final int id;

        Action_1_8(int id) {
            this.id = id;
        }

        public static Action_1_8 byId(int id) {
            for (Action_1_8 a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
