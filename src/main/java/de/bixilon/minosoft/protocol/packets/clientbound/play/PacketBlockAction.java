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

import de.bixilon.minosoft.game.datatypes.objectLoader.blockIds.BlockId;
import de.bixilon.minosoft.game.datatypes.objectLoader.blockIds.BlockIds;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.actions.*;
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
        // that's the only difference here
        if (buffer.getVersion().getVersionNumber() >= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            position = buffer.readPosition();
        } else {
            position = buffer.readBlockPositionShort();
        }
        byte byte1 = buffer.readByte();
        byte byte2 = buffer.readByte();
        Class<? extends BlockAction> clazz;
        BlockId blockId = BlockIds.getBlockId(buffer.readVarInt(), buffer.getVersion());
        switch (blockId.getIdentifier()) {
            case "noteblock":
                clazz = NoteBlockAction.class;
                break;
            case "sticky_piston":
            case "piston":
                clazz = PistonAction.class;
                break;
            case "chest":
            case "ender_chest":
            case "trapped_chest":
            case "white_shulkerbox":
            case "orange_shulkerbox":
            case "magenta_shulkerbox":
            case "light_blue_shulkerbox":
            case "yellow_shulkerbox":
            case "lime_shulkerbox":
            case "pink_shulkerbox":
            case "gray_shulkerbox":
            case "silver_shulkerbox":
            case "cyan_shulkerbox":
            case "purple_shulkerbox":
            case "blue_shulkerbox":
            case "brown_shulkerbox":
            case "green_shulkerbox":
            case "red_shulkerbox":
            case "black_shulkerbox":
                clazz = ChestAction.class;
                break;
            case "beacon":
                // beacon
                clazz = BeaconAction.class;
                break;
            case "mob_spawner":
                clazz = MobSpawnerAction.class;
                break;
            case "end_gateway":
                // end gateway
                clazz = EndGatewayAction.class;
                break;
            default:
                throw new IllegalStateException(String.format("Unexpected block action (blockId=%s)", blockId));
        }
        try {
            data = clazz.getConstructor(byte.class, byte.class).newInstance(byte1, byte2);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Block action received %s at %s", data, position));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
