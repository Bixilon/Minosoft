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

import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.BlockBreakAnimationS2CP;
import glm_.vec3.Vec3i;

public class BlockBreakAnimationEvent extends CancelableEvent {
    private final int entityId;
    private final Vec3i position;
    private final int stage;

    public BlockBreakAnimationEvent(PlayConnection connection, int entityId, Vec3i position, int stage) {
        super(connection);
        this.entityId = entityId;
        this.position = position;
        this.stage = stage;
    }

    public BlockBreakAnimationEvent(PlayConnection connection, BlockBreakAnimationS2CP pkg) {
        super(connection);
        this.entityId = pkg.getAnimationId();
        this.position = pkg.getBlockPosition();
        this.stage = pkg.getStage();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Vec3i getPosition() {
        return this.position;
    }

    public int getStage() {
        return this.stage;
    }
}
