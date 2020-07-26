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

package de.bixilon.minosoft.game.datatypes.entities.mob;

import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.Mob;
import de.bixilon.minosoft.game.datatypes.entities.MobInterface;
import de.bixilon.minosoft.game.datatypes.entities.Velocity;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.entities.meta.PufferfishMetaData;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class Pufferfish extends Mob implements MobInterface {
    PufferfishMetaData metaData;

    public Pufferfish(int entityId, Location location, short yaw, short pitch, Velocity velocity, MetaDataHashMap sets, ProtocolVersion version) {
        super(entityId, location, yaw, pitch, velocity);
        this.metaData = new PufferfishMetaData(sets, version);
    }

    @Override
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = (PufferfishMetaData) metaData;
    }

    @Override
    public float getWidth() {
        switch (metaData.getPufferState()) {
            case UN_PUFFED:
                return 0.35F;
            case SEMI_PUFFED:
                return 0.5F;
            case FULLY_PUFFED:
                return 0.7F;
        }
        return 0.35F;
    }

    @Override
    public float getHeight() {
        switch (metaData.getPufferState()) {
            case UN_PUFFED:
                return 0.35F;
            case SEMI_PUFFED:
                return 0.5F;
            case FULLY_PUFFED:
                return 0.7F;
        }
        return 0.35F;
    }

    @Override
    public int getMaxHealth() {
        return 3;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return PufferfishMetaData.class;
    }
}
