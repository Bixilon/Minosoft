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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.animated

import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

abstract class AnimatedParticle(connection: PlayConnection, position: Vec3d, gravityStrength: Float, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {
    var targetColor: RGBColor? = null

    init {
        this.friction = 0.91f
        this.gravityStrength = gravityStrength
    }

    override fun tick() {
        super.tick()

        if (age > maxAge / 2) {
            color = color.with(alpha = (floatAge - (maxAge / 2)) / maxAge)

            targetColor?.let {
                this.color = this.color.with(
                    red = this.color.floatRed + (it.floatRed - this.color.floatRed) * 0.2f,
                    green = this.color.floatGreen + (it.floatGreen - this.color.floatGreen) * 0.2f,
                    blue = this.color.floatBlue + (it.floatBlue - this.color.floatBlue) * 0.2f,
                )
            }
        }
    }
}
