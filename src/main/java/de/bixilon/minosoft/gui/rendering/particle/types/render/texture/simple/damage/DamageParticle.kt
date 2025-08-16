/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage

import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asGray
import de.bixilon.minosoft.data.world.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class DamageParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : SimpleTextureParticle(session, position, MVec3d.EMPTY, data) {

    override var scale: Float
        get() = super.scale * (floatAge / maxAge * 32.0f).clamp(0.0f, 1.0f)
        set(value) {
            super.scale = value
        }

    init {
        friction = 0.7f
        gravityStrength = 0.5f
        this.velocity *= 0.1
        this.velocity += velocity * 1.2f // ToDo: This is 0.4 in minecraft
        color = (random.nextFloat() * 0.3 + 0.6).asGray().rgba()
        super.scale *= 0.75f
        maxAge = (6.0f / (random.nextFloat() * 0.8f + 0.6f)).toInt().coerceAtLeast(1)
        physics = false
        tick()
    }

    final override fun tick() {
        super.tick()
        color = color.with(green = this.color.greenf * 0.96f, blue = this.color.bluef * 0.96f)
    }
}
