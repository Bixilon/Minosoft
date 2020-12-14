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

import de.bixilon.minosoft.data.mappings.BlockId;
import de.bixilon.minosoft.data.mappings.blocks.actions.*;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.BlockActionEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class PacketBlockAction extends ClientboundPacket {
    BlockPosition position;
    BlockAction data;

    @Override
    public boolean read(InByteBuffer buffer) {
        // that's the only difference here
        if (buffer.getVersionId() < 6) {
            this.position = buffer.readBlockPositionShort();
        } else {
            this.position = buffer.readPosition();
        }
        short byte1 = buffer.readUnsignedByte();
        short byte2 = buffer.readUnsignedByte();
        BlockId blockId = buffer.getConnection().getMapping().getBlockIdById(buffer.readVarInt());

        this.data = switch (blockId.getIdentifier()) {
            case "noteblock" -> new NoteBlockAction(byte1, byte2); // ToDo: was replaced in 17w47a (346) with the block id
            case "sticky_piston", "piston" -> new PistonAction(byte1, byte2);
            case "chest", "ender_chest", "trapped_chest", "white_shulker_box", "shulker_box", "orange_shulker_box", "magenta_shulker_box", "light_blue_shulker_box", "yellow_shulker_box", "lime_shulker_box", "pink_shulker_box", "gray_shulker_box", "silver_shulker_box", "cyan_shulker_box", "purple_shulker_box", "blue_shulker_box", "brown_shulker_box", "green_shulker_box", "red_shulker_box", "black_shulker_box" -> new ChestAction(byte1, byte2);
            case "beacon" -> new BeaconAction(byte1, byte2);
            case "mob_spawner" -> new MobSpawnerAction(byte1, byte2);
            case "end_gateway" -> new EndGatewayAction(byte1, byte2);
            default -> null;
        };
        return true;
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public BlockAction getData() {
        return this.data;
    }

    @Override
    public void handle(Connection connection) {
        BlockActionEvent event = new BlockActionEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Block action received %s at %s", this.data, this.position));
    }
}
