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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.water

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class WarpedSporeParticle(session: PlaySession, position: Vec3d, data: ParticleData? = null) : WaterSuspendParticle(session, position, MVec3d(0.0, (random.nextDouble() * -1.9 * random.nextDouble() * 0.1), 0.0), data) {

    init {
        color = RGBAColor(0.1f, 0.1f, 0.3f)
        spacing = Vec3f(0.001f)
    }


    companion object : ParticleFactory<WarpedSporeParticle> {
        override val identifier: ResourceLocation = "minecraft:warped_spore".toResourceLocation()
        private val random = Random()

        override fun build(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData): WarpedSporeParticle {
            return WarpedSporeParticle(session, position, data)
        }
    }
}
