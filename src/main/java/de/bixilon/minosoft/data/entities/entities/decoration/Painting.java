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

package de.bixilon.minosoft.data.entities.entities.decoration;

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.mappings.Motive;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class Painting extends Entity {
    private final Directions direction;
    private final Motive motive;

    public Painting(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
        direction = Directions.NORTH;
        motive = new Motive("kebab");
    }

    public Painting(Connection connection, int entityId, UUID uuid, BlockPosition position, Directions direction, Motive motive) {
        super(connection, entityId, uuid, Location.fromPosition(position), new EntityRotation(0, 0, 0));
        this.direction = direction;
        this.motive = motive;
    }

    @EntityMetaDataFunction(identifier = "direction")
    public Directions getDirection() {
        return direction;
    }

    @EntityMetaDataFunction(identifier = "motive")
    public Motive getMotive() {
        return motive;
    }
}
