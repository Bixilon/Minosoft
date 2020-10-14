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

import de.bixilon.minosoft.data.mappings.BlockId;
import de.bixilon.minosoft.data.mappings.blocks.actions.*;
import de.bixilon.minosoft.data.world.BlockPosition;
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
            case "chest", "ender_chest", "trapped_chest", "white_shulker_box", "shulker_box", "orange_shulker_box", "magenta_shulker_box", "light_blue_shulker_box", "yellow_shulker_box", "lime_shulker_box", "pink_shulker_box", "gray_shulker_box", "silver_shulker_box", "cyan_shulker_box", "purple_shulker_box", "blue_shulker_box", "brown_shulker_box", "green_shulker_box", "red_shulker_box", "black_shulker_box" -> ChestAction.class;
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

    public BlockPosition getPosition() {
        return position;
    }

    public BlockAction getData() {
        return data;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Block action received %s at %s", data, position));
    }
}
