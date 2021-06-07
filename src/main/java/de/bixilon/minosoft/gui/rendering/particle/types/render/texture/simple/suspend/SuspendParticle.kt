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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.suspend

import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.data.text.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

abstract class SuspendParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, velocity, data) {

    init {
        this.color = (random.nextFloat() * 0.1f + 0.2f).asGray()
        spacing = Vec3(0.2f)
        super.scale *= random.nextFloat() * 0.6f + 0.5f
        this.velocity *= 0.019999999552965164
        maxAge = (20 / (random.nextFloat() * 0.8f + 0.2f)).toInt()
        movement = false
    }

    override fun realTick() {
        super.realTick()
        if (dead) {
            return
        }
        position += velocity

        velocity *= 0.99f
    }
}
