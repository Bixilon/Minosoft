/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.boss.wither

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.monster.Monster
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class WitherBoss(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Monster(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val centerHeadTargetEntityId: Int?
        get() = data.get(CENTER_TARGET_DATA, null)

    @get:SynchronizedEntityData
    val leftHeadTargetEntityId: Int?
        get() = data.get(LEFT_TARGET_DATA, null)

    @get:SynchronizedEntityData
    val rightHeadTargetEntityId: Int?
        get() = data.get(RIGHT_TARGET_DATA, null)

    @get:SynchronizedEntityData
    val invulnerableTime: Int
        get() = data.get(INVULNERABILITY_TIME_DATA, 0)


    companion object : EntityFactory<WitherBoss> {
        override val identifier: ResourceLocation = KUtil.minecraft("wither")
        private val CENTER_TARGET_DATA = EntityDataField("WITHER_BOSS_CENTER_HEAD_TARGET_ENTITY_ID")
        private val LEFT_TARGET_DATA = EntityDataField("WITHER_BOSS_LEFT_HEAD_TARGET_ENTITY_ID")
        private val RIGHT_TARGET_DATA = EntityDataField("WITHER_BOSS_RIGHT_HEAD_TARGET_ENTITY_ID")
        private val INVULNERABILITY_TIME_DATA = EntityDataField("WITHER_BOSS_INVULNERABLE_TIME")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): WitherBoss {
            return WitherBoss(connection, entityType, data, position, rotation)
        }
    }
}
