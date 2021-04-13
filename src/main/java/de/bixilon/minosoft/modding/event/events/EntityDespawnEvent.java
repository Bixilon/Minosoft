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

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketDestroyEntity;

public class EntityDespawnEvent extends PlayConnectionEvent {
    private final int[] entityIds;

    public EntityDespawnEvent(PlayConnection connection, int[] entityIds) {
        super(connection);
        this.entityIds = entityIds;
    }

    public EntityDespawnEvent(PlayConnection connection, PacketDestroyEntity pkg) {
        super(connection);
        this.entityIds = pkg.getEntityIds();
    }

    public Entity[] getEntities() {
        Entity[] ret = new Entity[this.entityIds.length];
        for (int i = 0; i < this.entityIds.length; i++) {
            ret[i] = getConnection().getWorld().getEntity(this.entityIds[i]);
        }
        return ret;
    }

    public int[] getEntityIds() {
        return this.entityIds;
    }
}
