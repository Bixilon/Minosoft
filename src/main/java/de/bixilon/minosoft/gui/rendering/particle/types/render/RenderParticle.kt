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

package de.bixilon.minosoft.gui.rendering.particle.types.render

import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import kotlin.math.abs

abstract class RenderParticle(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, velocity: Vec3, data: ParticleData) : Particle(connection, particleRenderer, position, velocity, data) {
    protected var scale: Float = 0.1f * (random.nextFloat() * 0.5f + 0.5f) * 2.0f
    protected var color: RGBColor = ChatColors.WHITE

    // growing
    protected var nextScale: Float = scale
    protected var scalePerMillisecond = Float.NEGATIVE_INFINITY


    override fun tick(deltaTime: Int) {
        super.tick(deltaTime)
        if (dead) {
            return
        }
        grow(deltaTime)
    }

    fun grow(scale: Float, time: Long) {
        nextScale = scale
        scalePerMillisecond = (scale - this.scale) / time
    }

    private fun grow(deltaTime: Int) {
        if (scalePerMillisecond == Float.NEGATIVE_INFINITY) {
            return
        }
        val deltaScale = nextScale - scale
        if (abs(deltaScale) > GROW_LOWER_LIMIT) {
            // we need to grow
            val scaleAdd = scalePerMillisecond * deltaTime

            // checke if the delta gets bigger (aka. we'd grew to much)
            val nextScale = scale + scaleAdd
            if (abs(this.nextScale - nextScale) > deltaScale) {
                // abort scaling and avoid getting called another time
                scale = nextScale
                scalePerMillisecond = Float.NEGATIVE_INFINITY
                return
            }
            // we can grow
            scale = nextScale
        }
    }

    companion object {
        const val GROW_LOWER_LIMIT = 0.001f
    }


}
