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

import de.bixilon.minosoft.game.datatypes.entities.EntityObject;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.ObjectInterface;
import de.bixilon.minosoft.game.datatypes.entities.Velocity;
import de.bixilon.minosoft.game.datatypes.entities.meta.AreaEffectCloudMetaData;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;


public class AreaEffectCloud extends EntityObject implements ObjectInterface {
    AreaEffectCloudMetaData metaData;

    public AreaEffectCloud(int entityId, UUID uuid, Location location, short yaw, short pitch, int additionalInt) {
        super(entityId, location, yaw, pitch, null);
        // objects do not spawn with metadata... reading additional info from the following int
    }

    public AreaEffectCloud(int entityId, UUID uuid, Location location, short yaw, short pitch, int additionalInt, Velocity velocity) {
        super(entityId, location, yaw, pitch, velocity);
    }

    public AreaEffectCloud(int entityId, UUID uuid, Location location, short yaw, short pitch, Velocity velocity, EntityMetaData.MetaDataHashMap sets, int protocolId) {
        super(entityId, location, yaw, pitch, velocity);
        this.metaData = new AreaEffectCloudMetaData(sets, protocolId);
    }

    @Override
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = (AreaEffectCloudMetaData) metaData;
    }

    @Override
    public float getWidth() {
        return 2.0F * metaData.getRadius();
    }

    @Override
    public float getHeight() {
        return 0.5F;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return AreaEffectCloudMetaData.class;
    }
}
