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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.explosion

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asGray
import de.bixilon.minosoft.data.world.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class PoofParticle(session: PlaySession, position: Vec3d, data: ParticleData? = null, velocity: MVec3d) : SimpleTextureParticle(session, position, MVec3d(velocity.x + (Math.random() * 2.0 - 1.0) * 0.05, velocity.y + (Math.random() * 2.0 - 1.0) * 0.05, velocity.z + (Math.random() * 2.0 - 1.0) * 0.05), data) {

    init {
        this.gravityStrength = -0.1f
        this.friction = 0.9f
        this.color = ((random.nextFloat() * 0.3f) + 0.7f).asGray().rgba()
        this.scale = 0.1f * (random.nextFloat() * random.nextFloat() * 0.6f + 1.0f)
        maxAge = (16.0f / random.nextFloat() * 0.8f + 0.2f).toInt() + 2
    }

    companion object : ParticleFactory<PoofParticle> {
        override val identifier: ResourceLocation = "minecraft:poof".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData): PoofParticle {
            return PoofParticle(session, position, data, velocity)
        }
    }
}
