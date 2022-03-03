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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple

import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.func.common.clamp
import glm_.vec3.Vec3d

abstract class AscendingParticle(
    connection: PlayConnection,
    position: Vec3d,
    velocity: Vec3d,
    velocityMultiplier: Vec3d,
    scaleMultiplier: Float,
    colorMultiplier: Float,
    baseAge: Int,
    gravityStrength: Float,
    physics: Boolean,
    data: ParticleData? = null,
) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {

    override var scale: Float
        get() = super.scale * (floatAge / maxAge * 32.0f).clamp(0.0f, 1.0f)
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
