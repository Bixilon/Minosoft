/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import glm_.vec3.Vec3

class EnchantedHitParticle(connection: PlayConnection, position: Vec3, velocity: Vec3, data: ParticleData? = null) : DamageParticle(connection, position, velocity, data) {

    init {
        color = color.with(red = color.floatRed * 0.3f, green = color.floatGreen * 0.8f)
    }

    companion object : ParticleFactory<EnchantedHitParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:enchanted_hit".asResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3, velocity: Vec3, data: ParticleData): EnchantedHitParticle {
            return EnchantedHitParticle(connection, position, velocity, data)
        }
    }
}
