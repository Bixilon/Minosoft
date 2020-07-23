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

import de.bixilon.minosoft.game.datatypes.blocks.actions.*;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.lang.reflect.InvocationTargetException;

public class PacketBlockAction implements ClientboundPacket {
    BlockPosition position;
    BlockAction data;


    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                // that's the only difference here
                if (buffer.getVersion().getVersionNumber() >= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
                    position = buffer.readPosition();
                } else {
                    position = buffer.readBlockPositionShort();
                }
                byte byte1 = buffer.readByte();
                byte byte2 = buffer.readByte();
                Class<? extends BlockAction> clazz;
                int actionId = buffer.readVarInt();
                switch (actionId) {
                    case 25:
                        // noteblock
                        clazz = NoteBlockAction.class;
                        break;
                    case 29:
                    case 33:
                        // piston
                        clazz = PistonAction.class;
                        break;
                    case 54:
                    case 130:
                    case 146:
                    case 219:
                    case 220:
                    case 221:
                    case 222:
                    case 223:
                    case 224:
                    case 225:
                    case 226:
                    case 227:
                    case 228:
                    case 229:
                    case 230:
                    case 231:
                    case 232:
                    case 233:
                    case 234:
                        // chest, shulker box
                        clazz = ChestAction.class;
                        break;
                    case 138:
                        // beacon
                        clazz = BeaconAction.class;
                        break;
                    case 52:
                        // mob spawner
                        clazz = MobSpawnerAction.class;
                        break;
                    case 209:
                        // end gateway
                        clazz = EndGatewayAction.class;
                        break;
                    default:
                        throw new IllegalStateException(String.format("Unexpected block action value: %d", actionId));
                }
                try {
                    data = clazz.getConstructor(byte.class, byte.class).newInstance(byte1, byte2);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Block action received %s at %s", data.toString(), position.toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
