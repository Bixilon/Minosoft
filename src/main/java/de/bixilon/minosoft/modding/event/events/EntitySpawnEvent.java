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

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;

public class EntitySpawnEvent extends PlayConnectionEvent {
    private final Entity entity;

    public EntitySpawnEvent(PlayConnection connection, Entity entity) {
        super(connection);
        this.entity = entity;
    }

    public EntitySpawnEvent(PlayConnection connection, PacketSpawnMob pkg) {
        super(connection);
        this.entity = pkg.getEntity();
    }

    public EntitySpawnEvent(PlayConnection connection, PacketSpawnWeatherEntity pkg) {
        super(connection);
        this.entity = pkg.getEntity();
    }

    public EntitySpawnEvent(PlayConnection connection, PacketSpawnPlayer pkg) {
        super(connection);
        this.entity = pkg.getEntity();
    }

    public EntitySpawnEvent(PlayConnection connection, PacketSpawnExperienceOrb pkg) {
        super(connection);
        this.entity = pkg.getEntity();
    }

    public EntitySpawnEvent(PlayConnection connection, PacketSpawnObject pkg) {
        super(connection);
        this.entity = pkg.getEntity();
    }

    public EntitySpawnEvent(PlayConnection connection, PacketSpawnPainting pkg) {
        super(connection);
        this.entity = pkg.getEntity();
    }

    public Entity getEntity() {
        return this.entity;
    }
}
