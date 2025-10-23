/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.random.RandomUtil.nextInt
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks.Companion.ticks

class LightningBolt(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d) : Entity(session, entityType, data, position, EntityRotation(0.0f, 0.0f)) {
    val duration = random.nextInt(50, 150).ticks

    companion object : EntityFactory<LightningBolt> {
        override val identifier: ResourceLocation = minecraft("lightning_bolt")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): LightningBolt {
            return LightningBolt(session, entityType, data, position)
        }
    }
}
