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

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.ExplosionParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import glm_.vec3.Vec3

class ExplosionEmitterParticle(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, data: ParticleData) : NoRenderParticle(connection, particleRenderer, position, Vec3.EMPTY, data) {
    private val explosionParticleType = connection.registries.particleTypeRegistry[ExplosionParticle]!!

    init {
        maxAge = MAX_AGE
        movement = false
    }

    override fun realTick() {
        super.realTick()
        for (i in 0 until 6) {
            val position = position + { (random.nextFloat() - random.nextFloat()) * 4.0f }

            particleRenderer.add(ExplosionParticle(connection, particleRenderer, position, explosionParticleType.simple(), floatAge / MAX_AGE))
        }
    }

    companion object : ParticleFactory<ExplosionEmitterParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:explosion_emitter".asResourceLocation()
        private const val MAX_AGE = 9

        override fun build(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, velocity: Vec3, data: ParticleData): ExplosionEmitterParticle {
            return ExplosionEmitterParticle(connection, particleRenderer, position, data)
        }
    }
}
