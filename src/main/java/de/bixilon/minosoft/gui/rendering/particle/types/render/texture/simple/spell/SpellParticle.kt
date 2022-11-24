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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class SpellParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d(0.5f - kotlin.random.Random.nextDouble(), velocity.y, 0.5 - kotlin.random.Random.nextDouble()), data) {

    init {
        friction = 0.96f
        gravityStrength = -0.1f
        accelerateIfYBlocked = true
        this.velocity.y *= 0.2
        if (velocity.x == 0.0 && velocity.z == 0.0) {
            this.velocity.x *= 0.1
            this.velocity.z *= 0.1
        }
        super.scale *= 0.75f
        maxAge = (8.0f / (random.nextFloat() * 0.8f + 0.2f)).toInt()
        this.physics = false

        // ToDo: Toggle if using spyglass
    }

    override fun tick() {
        super.tick()

        color = color.with(alpha = interpolateLinear(0.05f, color.floatAlpha, 1.0f))
    }
}
