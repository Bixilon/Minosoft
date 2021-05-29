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
import de.bixilon.minosoft.data.text.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.MMath
import glm_.vec3.Vec3

abstract class AscendingParticle(
    connection: PlayConnection,
    position: Vec3,
    velocity: Vec3,
    velocityMultiplier: Vec3,
    scaleMultiplier: Float,
    colorMultiplier: Float,
    baseAge: Int,
    gravityStrength: Float,
    physics: Boolean,
    data: ParticleData? = null,
) : SimpleTextureParticle(connection, position, Vec3.EMPTY, data) {

    override var scale: Float
        get() = super.scale * MMath.clamp(floatAge / maxAge * 32.0f, 0.0f, 1.0f)
        set(value) {
            super.scale = value
        }

    init {
        friction = 0.96f
        this.gravityStrength = gravityStrength
        accelerateIfYBlocked = true
        this.velocity *= velocityMultiplier
        this.velocity += velocity
        color = (random.nextFloat() * colorMultiplier).asGray()
        super.scale *= 0.75f * scaleMultiplier
        super.maxAge = ((baseAge.toFloat() / (random.nextFloat() * 0.8f + 0.2f)).toInt() * scaleMultiplier).toInt().coerceAtLeast(1)
        this.physics = physics
    }
}
