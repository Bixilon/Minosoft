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

package de.bixilon.minosoft.gui.rendering.particle.types.norender

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.world.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.explosion.ExplosionParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ExplosionEmitterParticle(session: PlaySession, position: Vec3d, data: ParticleData? = null) : NoRenderParticle(session, position, MVec3d(), data) {
    private val explosionParticleType = session.registries.particleType[ExplosionParticle]

    init {
        maxAge = MAX_AGE
        movement = false
    }

    override fun tick() {
        super.tick()
        val particle = session.world.particle ?: return
        if (explosionParticleType == null) {
            dead = true
            return
        }
        for (i in 0 until 6) {
            val position = position + { (random.nextDouble() - random.nextDouble()) * 4.0 }

            particle += ExplosionParticle(session, position, explosionParticleType.default(), floatAge / MAX_AGE)
        }
    }

    companion object : ParticleFactory<ExplosionEmitterParticle> {
        override val identifier: ResourceLocation = "minecraft:explosion_emitter".toResourceLocation()
        private const val MAX_AGE = 9

        override fun build(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData): ExplosionEmitterParticle {
            return ExplosionEmitterParticle(session, position, data)
        }
    }
}
