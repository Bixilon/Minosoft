/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.PacketBlockChange;
import glm_.vec3.Vec3i;

/**
 * Fired when one block is changed
 */
public class BlockChangeEvent extends PlayConnectionEvent {
    private final Vec3i position;
    private final BlockState block;

    public BlockChangeEvent(PlayConnection connection, Vec3i position, BlockState block) {
        super(connection);
        this.position = position;
        this.block = block;
    }

    public BlockChangeEvent(PlayConnection connection, PacketBlockChange pkg) {
        super(connection);
        this.position = pkg.getBlockPosition();
        this.block = pkg.getBlock();
    }

    public Vec3i getPosition() {
        return this.position;
    }

    public BlockState getBlock() {
        return this.block;
    }
}
