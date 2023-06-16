/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.particle.types.render.RenderParticle
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class TextureParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : RenderParticle(connection, position, velocity, data) {
    abstract val texture: Texture?


    override fun addVertex(transparentMesh: ParticleMesh, particleMesh: ParticleMesh, time: Long) {
        val light = light

        val texture = texture ?: return
        when {
            texture.transparency == TextureTransparencies.TRANSLUCENT || color.alpha != 255 -> particleMesh
            else -> transparentMesh
        }.addVertex(getCameraPosition(time), scale, texture, color, light = light)
    }
}
