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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple

import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.TextureParticle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

abstract class SimpleTextureParticle(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, velocity: Vec3, data: ParticleData) : TextureParticle(connection, particleRenderer, position, velocity, data) {
    override var texture = particleRenderer.renderWindow.textures.allTextures[data.type.textures.first()]!!


    private fun checkSpriteTexture() {
        val totalTextures = data.type.textures.size
        if (totalTextures <= 1) {
            return
        }
        // calculate next texture
        val nextTextureResourceLocation = data.type.textures[age / (maxAge / totalTextures + 1)]
        if (texture.resourceLocation == nextTextureResourceLocation) {
            return
        }
        texture = particleRenderer.renderWindow.textures.allTextures[nextTextureResourceLocation]!!
    }

    override fun tick(deltaTime: Int) {
        super.tick(deltaTime)
        if (dead) {
            return
        }
        checkSpriteTexture()
    }
}
