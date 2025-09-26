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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.enchant

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.pow

abstract class EnchantedGlyphParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : SimpleTextureParticle(session, position, MVec3d(), data) {
    private val initialPosition = position

    init {
        this.velocity(velocity)

        super.scale = 0.1f * (random.nextFloat() * 0.5f + 0.2f)

        val colorMultiplier = random.nextFloat() * 0.6f + 0.4f
        this.color = RGBAColor(colorMultiplier * 0.9f, colorMultiplier * 0.9f, colorMultiplier)

        this.physics = false

        this.maxAge = (random.nextFloat() * 10.0f).toInt() + 30
        movement = false
        spriteDisabled = true
        setRandomSprite()
    }

    override fun postTick() {
        super.postTick()
        if (dead) {
            return
        }

        val ageDivisor = 1.0 - floatAge / maxAge
        val ageDivisor2 = (1.0 - ageDivisor).pow(3)

        val nextPosition = initialPosition.mutable()
        nextPosition += velocity * ageDivisor
        nextPosition.y -= ageDivisor2 * 1.2f
        this.position = nextPosition.unsafe
    }
}
