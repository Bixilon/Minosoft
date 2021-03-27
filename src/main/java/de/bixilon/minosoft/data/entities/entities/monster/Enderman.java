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
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_8_9;

public class Enderman extends AbstractSkeleton {

    public Enderman(Connection connection, int entityId, UUID uuid, Vec3 position, EntityRotation rotation) {
        super(connection, entityId, uuid, position, rotation);
    }

    @EntityMetaDataFunction(name = "Carried block")
    @Nullable
    public BlockState getCarriedBlock() {
        if (this.versionId <= V_1_8_9) { // ToDo: No clue here
            return this.connection.getMapping().getBlockState(this.metaData.getSets().getInt(EntityMetaDataFields.LEGACY_ENDERMAN_CARRIED_BLOCK) << 4 | this.metaData.getSets().getInt(EntityMetaDataFields.LEGACY_ENDERMAN_CARRIED_BLOCK_DATA));
        }
        return this.metaData.getSets().getBlock(EntityMetaDataFields.ENDERMAN_CARRIED_BLOCK);
    }

    @EntityMetaDataFunction(name = "Is screaming")
    public boolean isScreaming() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENDERMAN_IS_SCREAMING);
    }

    @EntityMetaDataFunction(name = "Is starring")
    public boolean isStarring() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENDERMAN_IS_STARRING);
    }
}
