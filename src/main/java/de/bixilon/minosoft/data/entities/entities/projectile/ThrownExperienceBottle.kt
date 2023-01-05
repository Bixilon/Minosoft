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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class ThrownExperienceBottle(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : ThrowableItemProjectile(connection, entityType, data, position, rotation) {
    override val gravity: Float = 0.07f
    override val defaultItemType: ResourceLocation
        get() = DEFAULT_ITEM

    companion object : EntityFactory<ThrownExperienceBottle> {
        private val DEFAULT_ITEM = KUtil.minecraft("experience_bottle")
        override val identifier: ResourceLocation = KUtil.minecraft("experience_bottle")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ThrownExperienceBottle {
            return ThrownExperienceBottle(connection, entityType, data, position, rotation)
        }
    }
}
