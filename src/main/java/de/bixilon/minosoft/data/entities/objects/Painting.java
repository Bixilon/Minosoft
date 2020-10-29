/*
 * Minosoft
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

package de.bixilon.minosoft.data.entities.objects;

import de.bixilon.minosoft.data.entities.EntityObject;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.ObjectInterface;
import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.data.mappings.Motive;
import de.bixilon.minosoft.data.world.BlockPosition;

import java.util.UUID;

public class Painting extends EntityObject implements ObjectInterface {
    EntityMetaData metaData;
    int direction;
    Motive motive;

    public Painting(int entityId, UUID uuid, Location location, short yaw, short pitch, int additionalInt) {
        super(entityId, uuid, location, yaw, pitch);
    }

    public Painting(int entityId, UUID uuid, Location location, short yaw, short pitch, short headYaw, EntityMetaData.MetaDataHashMap sets, int versionId) {
        super(entityId, uuid, location, yaw, pitch, headYaw);
        this.metaData = new EntityMetaData(sets, versionId);
    }

    public Painting(int entityId, UUID uuid, BlockPosition position, int direction, Motive motive) {
        super(entityId, uuid, new Location(position.getX(), position.getY(), position.getZ()), 0, 0, 0);
        this.direction = direction;
        this.motive = motive;
    }

    public Painting(int entityId, UUID uuid, Motive motive, BlockPosition position, int direction) {
        super(entityId, uuid, new Location(position.getX(), position.getY(), position.getZ()), 0, 0);
        this.direction = direction;
        this.motive = motive;
    }

    @Override
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public float getWidth() {
        // ToDo
        return 1.0F;
    }

    @Override
    public float getHeight() {
        // ToDo
        return 1.0F;
    }

    public Motive getMotive() {
        return motive;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return EntityMetaData.class;
    }
}
