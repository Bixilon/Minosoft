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

import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.blocks.actions.*;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.modding.event.events.BlockActionEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W03B;

public class PacketBlockAction extends ClientboundPacket {
    BlockPosition position;
    BlockAction data;

    @Override
    public boolean read(InByteBuffer buffer) {
        // that's the only difference here
        if (buffer.getVersionId() < V_14W03B) {
            this.position = buffer.readBlockPositionShort();
        } else {
            this.position = buffer.readPosition();
        }
        short byte1 = buffer.readUnsignedByte();
        short byte2 = buffer.readUnsignedByte();
        Block blockId = buffer.getConnection().getMapping().getBlockRegistry().get(buffer.readVarInt());
        if (blockId == null) {
            return true;
        }

        this.data = switch (blockId.getIdentifier().getFullIdentifier()) {
            case "minecraft:noteblock" -> new NoteBlockAction(byte1, byte2); // ToDo: was replaced in 17w47a (346) with the block id
            case "minecraft:sticky_piston", "minecraft:piston" -> new PistonAction(byte1, byte2);
            case "minecraft:chest", "minecraft:ender_chest", "minecraft:trapped_chest", "minecraft:white_shulker_box", "minecraft:shulker_box", "minecraft:orange_shulker_box", "minecraft:magenta_shulker_box", "minecraft:light_blue_shulker_box", "minecraft:yellow_shulker_box", "minecraft:lime_shulker_box", "minecraft:pink_shulker_box", "minecraft:gray_shulker_box", "minecraft:silver_shulker_box", "minecraft:cyan_shulker_box", "minecraft:purple_shulker_box", "minecraft:blue_shulker_box", "minecraft:brown_shulker_box", "minecraft:green_shulker_box", "minecraft:red_shulker_box", "minecraft:black_shulker_box" -> new ChestAction(byte1, byte2);
            case "minecraft:beacon" -> new BeaconAction(byte1, byte2);
            case "minecraft:mob_spawner" -> new MobSpawnerAction(byte1, byte2);
            case "minecraft:end_gateway" -> new EndGatewayAction(byte1, byte2);
            default -> null;
        };
        return true;
    }

    @Override
    public void handle(Connection connection) {
        BlockActionEvent event = new BlockActionEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public BlockAction getData() {
        return this.data;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Block action received %s at %s", this.data, this.position));
    }
}
