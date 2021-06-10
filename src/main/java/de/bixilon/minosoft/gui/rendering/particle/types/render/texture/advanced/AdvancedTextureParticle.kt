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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced

import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec2.Vec2
import glm_.vec3.Vec3d

abstract class AdvancedTextureParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, velocity, data) {
    var minUV: Vec2 = Vec2(0.0f, 0.0f)
    var maxUV: Vec2 = Vec2(1.0f, 1.0f)

    override fun addVertex(particleMesh: ParticleMesh) {
        texture?.let {
            particleMesh.addVertex(cameraPosition, scale, it, color, minUV, maxUV)
        }
    }
}
