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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.enchant

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.assign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import kotlin.math.pow

abstract class EnchantedGlyphParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {
    private val startPosition = Vec3d(position)

    init {
        this.velocity assign velocity

        super.scale = 0.1f * (random.nextFloat() * 0.5f + 0.2f)

        val colorMultiplier = random.nextFloat() * 0.6f + 0.4f
        this.color = RGBColor(colorMultiplier * 0.9f, colorMultiplier * 0.9f, colorMultiplier)

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
        this.position assign (startPosition + velocity * ageDivisor)
        this.position.y -= ageDivisor2 * 1.2f
    }
}
