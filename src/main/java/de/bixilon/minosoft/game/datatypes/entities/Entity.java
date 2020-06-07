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

package de.bixilon.minosoft.game.datatypes.entities;

import de.bixilon.minosoft.game.datatypes.Slot;
import de.bixilon.minosoft.game.datatypes.Slots;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;

public interface Entity {
    Mobs getEntityType();

    int getId();

    Location getLocation();

    void setLocation(Location location);

    void setLocation(RelativeLocation location);

    Velocity getVelocity();

    void setVelocity(Velocity velocity);

    int getYaw();

    void setYaw(int yaw);

    int getPitch();

    void setPitch(int pitch);

    float getWidth();

    float getHeight();

    <T extends EntityMetaData> EntityMetaData getMetaData();

    void setMetaData(EntityMetaData data);

    void setEquipment(Slots.Entity slot, Slot data);

    Slot getEquipment(Slots.Entity slot);


}
