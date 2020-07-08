/*
 * Codename Minosoft
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

package de.bixilon.minosoft.game.datatypes.entities.objects;

import de.bixilon.minosoft.game.datatypes.entities.*;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class Painting extends EntityObject implements ObjectInterface {
    EntityMetaData metaData;
    int direction;
    Paintings painting;

    public Painting(int entityId, Location location, short yaw, short pitch, int additionalInt) {
        super(entityId, location, yaw, pitch, null);
        // objects do not spawn with metadata... reading additional info from the following int
    }

    public Painting(int entityId, Location location, short yaw, short pitch, int additionalInt, Velocity velocity) {
        super(entityId, location, yaw, pitch, velocity);
    }

    public Painting(int entityId, Location location, short yaw, short pitch, Velocity velocity, HashMap<Integer, EntityMetaData.MetaDataSet> sets, ProtocolVersion version) {
        super(entityId, location, yaw, pitch, velocity);
        this.metaData = new EntityMetaData(sets, version);
    }

    public Painting(int entityId, BlockPosition position, int direction, Paintings painting) {
        super(entityId, new Location(position.getX(), position.getY(), position.getZ()), (short) 0, (short) 0, null);
        this.direction = direction;
        this.painting = painting;
    }

    @Override
    public Entities getEntityType() {
        return Entities.PAINTING;
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
        //ToDo
        return 1.0F;
    }

    @Override
    public float getHeight() {
        //ToDo
        return 1.0F;
    }

    public Paintings getPainting() {
        return painting;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return EntityMetaData.class;
    }
}
