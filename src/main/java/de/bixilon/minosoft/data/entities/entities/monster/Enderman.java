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

package de.bixilon.minosoft.data.entities.entities.monster;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.protocol.network.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.Versions.V_1_8_9;

public class Enderman extends AbstractSkeleton {

    public Enderman(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    @EntityMetaDataFunction(identifier = "carriedBlock")
    @Nullable
    public Block getCarriedBlock() {
        if (this.versionId <= V_1_8_9) { // ToDo: No clue here
            return this.connection.getMapping().getBlockById(this.metaData.getSets().getInt(EntityMetaDataFields.LEGACY_ENDERMAN_CARRIED_BLOCK) << 4 | this.metaData.getSets().getInt(EntityMetaDataFields.LEGACY_ENDERMAN_CARRIED_BLOCK_DATA));
        }
        return this.metaData.getSets().getBlock(EntityMetaDataFields.ENDERMAN_CARRIED_BLOCK);
    }

    @EntityMetaDataFunction(identifier = "isScreaming")
    public boolean isScreaming() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENDERMAN_IS_SCREAMING);
    }

    @EntityMetaDataFunction(identifier = "isStarring")
    public boolean isStarring() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENDERMAN_IS_STARRING);
    }
}
