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

package de.bixilon.minosoft.data.entities.entities.animal;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.TamableAnimal;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class Wolf extends TamableAnimal {
    public Wolf(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    public boolean isBegging() {
        return metaData.getSets().getBoolean(EntityMetaDataFields.WOLF_IS_BEGGING);
    }

    public RGBColor getCollarColor() {
        return ChatColors.getColorById(metaData.getSets().getInt(EntityMetaDataFields.WOLF_COLLAR_COLOR));
    }

    public int getAngerTime() {
        return metaData.getSets().getInt(EntityMetaDataFields.WOLF_ANGER_TIME);
    }

    @Override
    public float getHealth() {
        if (versionId > 562) {
            return super.getHealth();
        }
        return metaData.getSets().getFloat(EntityMetaDataFields.WOLF_HEALTH);
    }
}
