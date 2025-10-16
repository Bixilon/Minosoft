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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.TextureParticle
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file.FileTexture
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class SimpleTextureParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : TextureParticle(session, position, velocity, data) {
    override var texture = this.data.type.loadedTextures.getOrNull(0)
    var spriteDisabled = false


    private fun checkSpriteTexture() {
        if (age > maxAge) {
            // Should not happen
            return
        }
        val totalTextures = data.type.textures.size
        if (totalTextures <= 1 || spriteDisabled) {
            return
        }
        // calculate next texture
        val next = data.type.loadedTextures[age / (maxAge / totalTextures + 1)]
        if (texture?.nullCast<FileTexture>()?.file == next) {
            return
        }
        texture = next
    }

    fun setRandomSprite() {
        val textures = data.type.loadedTextures
        if (textures.isEmpty()) return
        texture = textures.random()
    }

    override fun tick() {
        super.tick()
        if (dead) {
            return
        }
        checkSpriteTexture()
    }
}
