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

import de.bixilon.minosoft.data.mappings.blocks.actions.BlockAction;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketBlockAction;
import glm_.vec3.Vec3i;

/**
 * Fired when a block actions happens (like opening a chest, changing instrument/note/etc on a note block, etc)
 */
public class BlockActionEvent extends CancelableEvent {
    private final Vec3i position;
    private final BlockAction data;

    public BlockActionEvent(Connection connection, Vec3i position, BlockAction data) {
        super(connection);
        this.position = position;
        this.data = data;
    }

    public BlockActionEvent(Connection connection, PacketBlockAction pkg) {
        super(connection);
        this.position = pkg.getPosition();
        this.data = pkg.getData();
    }

    public Vec3i getPosition() {
        return this.position;
    }

    public BlockAction getData() {
        return this.data;
    }
}
