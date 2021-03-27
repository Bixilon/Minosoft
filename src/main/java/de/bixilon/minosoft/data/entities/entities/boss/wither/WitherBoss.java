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

package de.bixilon.minosoft.data.entities.entities.boss.wither;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.monster.Monster;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

import java.util.UUID;

public class WitherBoss extends Monster {

    public WitherBoss(Connection connection, int entityId, UUID uuid, Vec3 position, EntityRotation rotation) {
        super(connection, entityId, uuid, position, rotation);
    }

    @EntityMetaDataFunction(name = "Center head target entity id")
    public int getCenterHeadTargetEntityId() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.WITHER_BOSS_CENTER_HEAD_TARGET_ENTITY_ID);
    }

    @EntityMetaDataFunction(name = "Left head target entity id")
    public int getLeftHeadTargetEntityId() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.WITHER_BOSS_LEFT_HEAD_TARGET_ENTITY_ID);
    }

    @EntityMetaDataFunction(name = "Right head target entity id")
    public int getRightHeadTargetEntityId() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.WITHER_BOSS_RIGHT_HEAD_TARGET_ENTITY_ID);
    }

    @EntityMetaDataFunction(name = "Invulnerable time")
    public int getInvulnerableTime() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.WITHER_BOSS_INVULNERABLE_TIME);
    }
}
