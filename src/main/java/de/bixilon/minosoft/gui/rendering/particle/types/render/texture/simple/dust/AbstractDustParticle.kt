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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust

import de.bixilon.kutil.math.MMath.clamp
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

abstract class AbstractDustParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: DustParticleData) : SimpleTextureParticle(connection, position, velocity, data) {

    override var scale: Float
        get() = super.scale * clamp(floatAge / maxAge * 32.0f, 0.0f, 1.0f)
        set(value) {
            super.scale = value
        }

    init {
        this.friction = 0.96f
        this.velocity *= 0.1f

        val brightness = random.nextFloat() * 0.4f + 0.6f
        this.color = RGBColor(
            red = colorMix(data.color.floatRed, brightness),
            green = colorMix(data.color.floatGreen, brightness),
            blue = colorMix(data.color.floatBlue, brightness),
        )
        super.scale *= 0.75f * data.scale

        maxAge = ((8.0f / (random.nextFloat() * 0.8f + 0.2f)) * data.scale).coerceAtLeast(1.0f).toInt()

        this.accelerateIfYBlocked = true
    }

    private fun colorMix(color: Float, brightness: Float): Float {
        return (random.nextFloat() * 0.2f + 0.8f) * color * brightness
    }

    override fun tick() {
        super.tick()
        forceMove(velocity)

        velocity *= 0.99f
    }
}
