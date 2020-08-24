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
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.actions.*;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.lang.reflect.InvocationTargetException;

public class PacketBlockAction implements ClientboundPacket {
    BlockPosition position;
    BlockAction data;

    @Override
    public boolean read(InByteBuffer buffer) {
        // that's the only difference here
        if (buffer.getProtocolId() < 6) {
            position = buffer.readBlockPositionShort();
        } else {
            position = buffer.readPosition();
        }
        byte byte1 = buffer.readByte();
        byte byte2 = buffer.readByte();
        Class<? extends BlockAction> clazz;
        BlockId blockId = buffer.getConnection().getMapping().getBlockIdById(buffer.readVarInt());
        // beacon
        // end gateway
        clazz = switch (blockId.getIdentifier()) {
            case "noteblock" -> NoteBlockAction.class; // ToDo: was replaced in 17w47a (346) with the block id
            case "sticky_piston", "piston" -> PistonAction.class;
            case "chest", "ender_chest", "trapped_chest", "white_shulkerbox", "orange_shulkerbox", "magenta_shulkerbox", "light_blue_shulkerbox", "yellow_shulkerbox", "lime_shulkerbox", "pink_shulkerbox", "gray_shulkerbox", "silver_shulkerbox", "cyan_shulkerbox", "purple_shulkerbox", "blue_shulkerbox", "brown_shulkerbox", "green_shulkerbox", "red_shulkerbox", "black_shulkerbox" -> ChestAction.class;
            case "beacon" -> BeaconAction.class;
            case "mob_spawner" -> MobSpawnerAction.class;
            case "end_gateway" -> EndGatewayAction.class;
            default -> throw new IllegalStateException(String.format("Unexpected block action (blockId=%s)", blockId));
        };
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
