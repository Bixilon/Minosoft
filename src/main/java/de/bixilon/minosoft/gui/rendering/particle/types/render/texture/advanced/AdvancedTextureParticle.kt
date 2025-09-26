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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

abstract class AdvancedTextureParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : SimpleTextureParticle(session, position, velocity, data) {
    var minUV: Vec2f = Vec2f(0.0f, 0.0f)
    var maxUV: Vec2f = Vec2f(1.0f, 1.0f)

    override fun addVertex(mesh: ParticleMesh, translucentMesh: ParticleMesh, time: ValueTimeMark) {
        val light = light

        val texture = texture ?: return
        when {
            texture.transparency == TextureTransparencies.TRANSLUCENT || color.alpha != 255 -> translucentMesh
            else -> mesh
        }.addVertex(getCameraPosition(time), scale, texture, color, minUV, maxUV, light.index)
    }
}
