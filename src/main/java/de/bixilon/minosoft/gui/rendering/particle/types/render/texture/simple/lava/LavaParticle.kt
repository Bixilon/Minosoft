/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.lava

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sqr
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class LavaParticle(connection: PlayConnection, position: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {

    override var scale: Float
        get() = super.scale * (1.0f - (floatAge / maxAge).sqr)
        set(value) {
            super.scale = value
        }

    init {
        gravityStrength = 0.75f
        friction = 0.999f
        velocity.x *= 0.8
        velocity.y = random.nextDouble() * 0.4f + 0.05f
        velocity.z *= 0.8
        scale *= random.nextFloat() * 2.0f + 0.2f
        maxAge = (16.0f / (random.nextFloat() * 0.8f + 0.2f)).toInt()
    }

    override fun tick() {
        super.tick()

        if (random.nextFloat() > (floatAge / maxAge)) {
            connection.world += SmokeParticle(connection, Vec3d(position), Vec3d(velocity))
        }
    }

    companion object : ParticleFactory<LavaParticle> {
        override val identifier: ResourceLocation = "minecraft:lava".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): LavaParticle {
            return LavaParticle(connection, position, data)
        }
    }
}
