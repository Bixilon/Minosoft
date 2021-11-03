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

package de.bixilon.minosoft.gui.rendering.particle.types.norender

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.ExplosionParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3d

class ExplosionEmitterParticle(connection: PlayConnection, position: Vec3d, data: ParticleData? = null) : NoRenderParticle(connection, position, Vec3d.EMPTY, data) {
    private val explosionParticleType = connection.registries.particleTypeRegistry[ExplosionParticle]

    init {
        maxAge = MAX_AGE
        movement = false
    }

    override fun tick() {
        super.tick()
        explosionParticleType ?: let {
            dead = true
            return
        }
        for (i in 0 until 6) {
            val position = position + { (random.nextDouble() - random.nextDouble()) * 4.0 }

            connection.world += ExplosionParticle(connection, position, explosionParticleType.default(), floatAge / MAX_AGE)
        }
    }

    companion object : ParticleFactory<ExplosionEmitterParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:explosion_emitter".toResourceLocation()
        private const val MAX_AGE = 9

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): ExplosionEmitterParticle {
            return ExplosionEmitterParticle(connection, position, data)
        }
    }
}
