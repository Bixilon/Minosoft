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

package de.bixilon.minosoft.physics.handlers.movement

import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeOperations
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeModifier
import de.bixilon.minosoft.data.registries.effects.attributes.integrated.IntegratedAttributeModifiers
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkPosition
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics

class PowderSnowHandler(
    private val physics: LivingEntityPhysics<*>,
) : Tickable {
    private val entity = physics.entity
    private val container = entity.attributes.getOrCreate(MinecraftAttributes.MOVEMENT_SPEED)
    private var slowness = 0.0f

    private fun calculateSlowness(): Float {
        val ticks = entity.ticksFrozen
        if (ticks <= 0) {
            return 0.0f
        }
        if (physics.positionInfo.chunk?.get(physics.getLandingPosition().inChunkPosition) == null) return 0.0f

        val scale = minOf(ticks, 140) / 140.0f
        return scale * -0.05f
    }

    override fun tick() {
        val slowness = calculateSlowness()
        if (slowness == this.slowness || (slowness > 0.0f && IntegratedAttributeModifiers.POWDER_SNOW_SLOW !in container)) {
            return
        }
        this.slowness = slowness
        container -= IntegratedAttributeModifiers.POWDER_SNOW_SLOW
        container += AttributeModifier("Powder snow slow", IntegratedAttributeModifiers.POWDER_SNOW_SLOW, slowness.toDouble(), AttributeOperations.ADD)
    }
}
