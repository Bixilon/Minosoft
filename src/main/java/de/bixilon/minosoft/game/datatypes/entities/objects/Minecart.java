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
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.entities.meta.MinecartMetaData;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class Minecart extends EntityObject implements ObjectInterface {
    final MinecartType type;
    MinecartMetaData metaData;

    public Minecart(int entityId, Location location, short yaw, short pitch, int additionalInt) {
        super(entityId, location, yaw, pitch, null);
        type = MinecartType.byType(additionalInt);
    }

    public Minecart(int entityId, Location location, short yaw, short pitch, int additionalInt, Velocity velocity) {
        super(entityId, location, yaw, pitch, velocity);
        type = MinecartType.byType(additionalInt);
    }

    public Minecart(int entityId, Location location, short yaw, short pitch, Velocity velocity, HashMap<Integer, EntityMetaData.MetaDataSet> sets, ProtocolVersion version) {
        super(entityId, location, yaw, pitch, velocity);
        this.metaData = new MinecartMetaData(sets, version);
        type = MinecartType.EMPTY; // ToDo
    }

    @Override
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = (MinecartMetaData) metaData;
    }

    @Override
    public float getWidth() {
        return 0.98F;
    }

    @Override
    public float getHeight() {
        return 0.7F;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return MinecartMetaData.class;
    }

    public MinecartType getType() {
        return type;
    }

    public enum MinecartType {
        EMPTY(0),
        CHEST(1),
        FURNACE(2),
        TNT(3),
        SPAWNER(4),
        HOPPER(5),
        COMMAND_BLOCK(6);

        final int id;

        MinecartType(int id) {
            this.id = id;
        }

        public static MinecartType byType(int type) {
            for (MinecartType t : values()) {
                if (t.getId() == type) {
                    return t;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
