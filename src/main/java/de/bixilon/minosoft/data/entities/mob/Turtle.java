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

package de.bixilon.minosoft.data.entities.mob;

import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.Mob;
import de.bixilon.minosoft.data.entities.MobInterface;
import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.data.entities.meta.TurtleMetaData;

import java.util.UUID;

public class Turtle extends Mob implements MobInterface {
    TurtleMetaData metaData;

    public Turtle(int entityId, UUID uuid, Location location, short yaw, short pitch, short headYaw, EntityMetaData.MetaDataHashMap sets, int versionId) {
        super(entityId, uuid, location, yaw, pitch, headYaw);
        this.metaData = new TurtleMetaData(sets, versionId);
    }

    @Override
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = (TurtleMetaData) metaData;
    }

    @Override
    public float getWidth() {
        if (metaData.isAdult()) {
            return 1.2F;
        }
        return 0.36F;
    }

    @Override
    public float getHeight() {
        if (metaData.isAdult()) {
            return 0.4F;
        }
        return 0.12F;
    }

    @Override
    public int getMaxHealth() {
        return 30;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return TurtleMetaData.class;
    }
}
