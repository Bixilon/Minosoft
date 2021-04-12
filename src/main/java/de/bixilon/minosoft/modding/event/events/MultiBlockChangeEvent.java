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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketMultiBlockChange;
import glm_.vec2.Vec2i;
import glm_.vec3.Vec3i;

import java.util.Map;

/**
 * Fired when at least block is changed
 */
public class MultiBlockChangeEvent extends PlayConnectionEvent {
    private final Map<Vec3i, BlockState> blocks;
    private final Vec2i chunkPosition;

    public MultiBlockChangeEvent(PlayConnection connection, Map<Vec3i, BlockState> blocks, Vec2i position) {
        super(connection);
        this.blocks = blocks;
        this.chunkPosition = position;
    }

    public MultiBlockChangeEvent(PlayConnection connection, PacketMultiBlockChange pkg) {
        super(connection);
        this.blocks = pkg.getBlocks();
        this.chunkPosition = pkg.getChunkPosition();
    }

    public Map<Vec3i, BlockState> getBlocks() {
        return this.blocks;
    }

    public Vec2i getChunkPosition() {
        return this.chunkPosition;
    }
}
