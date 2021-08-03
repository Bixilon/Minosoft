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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3d

class AmbientEntityEffectParticle(connection: PlayConnection, position: Vec3d, color: RGBColor, data: ParticleData? = null) : SpellParticle(connection, position, Vec3d(color.red, color.green, color.blue), data) {

    init {
        this.color = color.with(alpha = 0.15f)
    }

    companion object : ParticleFactory<AmbientEntityEffectParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:ambient_entity_effect".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): AmbientEntityEffectParticle {
            return AmbientEntityEffectParticle(connection, position, color = RGBColor(velocity.x, velocity.y, velocity.z), data)
        }
    }
}
