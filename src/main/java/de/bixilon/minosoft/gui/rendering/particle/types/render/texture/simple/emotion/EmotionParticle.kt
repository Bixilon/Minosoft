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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.emotion

import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.MMath
import glm_.vec3.Vec3d

abstract class EmotionParticle(connection: PlayConnection, position: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {

    override var scale: Float
        get() = super.scale * MMath.clamp(floatAge / maxAge * 32.0f, 0.0f, 1.0f)
        set(value) {
            super.scale = value
        }

    init {
        accelerateIfYBlocked = true
        friction = 0.86f
        this.velocity *= 0.009999999776482582

        this.velocity.y += 0.1
        super.scale *= 1.5f
        maxAge = 16
        this.physics = false
    }

}
