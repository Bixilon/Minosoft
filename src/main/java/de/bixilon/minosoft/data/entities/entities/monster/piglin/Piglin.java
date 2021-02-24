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

package de.bixilon.minosoft.data.entities.entities.monster.piglin;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A;

public class Piglin extends AbstractPiglin {

    public Piglin(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    @EntityMetaDataFunction(name = "Is immune to zombification")
    @Override
    public boolean isImmuneToZombification() {
        if (this.versionId < V_20W27A) {
            return super.isImmuneToZombification();
        }
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.PIGLIN_IMMUNE_TO_ZOMBIFICATION);
    }

    @EntityMetaDataFunction(name = "Is baby")
    public boolean isBaby() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.PIGLIN_IS_BABY);
    }

    @EntityMetaDataFunction(name = "Is charging crossbow")
    public boolean isChargingCrossbow() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.PIGLIN_IS_CHARGING_CROSSBOW);
    }

    @EntityMetaDataFunction(name = "Is dancing")
    public boolean isDancing() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.PIGLIN_IS_DANCING);
    }

}
