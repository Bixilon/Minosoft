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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell

import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.MMath
import glm_.vec3.Vec3
import kotlin.random.Random

abstract class SpellParticle(connection: PlayConnection, position: Vec3, velocity: Vec3, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3(0.5f - Random.nextFloat(), velocity.y, 0.5 - Random.nextFloat()), data) {

    init {
        friction = 0.96f
        gravityStrength = -0.1f
        accelerateIfYBlocked = true
        this.velocity.y *= 0.20000000298023224f
        if (velocity.x == 0.0f && velocity.z == 0.0f) {
            this.velocity.x *= 0.10000000149011612f
            this.velocity.z *= 0.10000000149011612f
        }
        super.scale *= 0.75f
        maxAge = (8.0f / (random.nextFloat() * 0.8f + 0.2f)).toInt()
        this.physics = false

        // ToDo: Toggle if using spyglass
    }

    override fun realTick() {
        super.realTick()

        color = color.with(alpha = MMath.lerp(0.05f, color.floatAlpha, 1.0f))
    }
}
