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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.gui.rendering.particle.types.norender.ExplosionEmitterParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.explosion.ExplosionParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.times
import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.modding.event.events.ParticleSpawnEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

object DefaultParticleBehavior {

    fun register(connection: PlayConnection, particleRenderer: ParticleRenderer) {
        val random = java.util.Random()
        val explosionParticleType = connection.registries.particleType[ExplosionParticle]!!
        val explosionEmitterParticleType = connection.registries.particleType[ExplosionEmitterParticle]!!
        val typesConfig = connection.profiles.particle.types
        val invokers = listOf(
            CallbackEventListener.of<ExplosionEvent> {
                if (typesConfig.explosions) {
                    return@of
                }
                if (it.power >= 2.0f) {
                    particleRenderer += ExplosionEmitterParticle(connection, Vec3d(it.position), explosionEmitterParticleType.default())
                } else {
                    particleRenderer += ExplosionParticle(connection, Vec3d(it.position), explosionParticleType.default())
                }
            },
            CallbackEventListener.of<ParticleSpawnEvent> {
                DefaultThreadPool += add@{
                    fun spawn(position: Vec3d, velocity: Vec3d) {
                        val factory = it.data.type.factory ?: return
                        particleRenderer += factory.build(connection, position, velocity, it.data) ?: return
                    }
                    // ToDo: long distance = always spawn?
                    if (it.count == 0) {
                        val velocity = it.offset * it.speed

                        spawn(it.position, Vec3d(velocity))
                    } else {
                        for (i in 0 until it.count) {
                            val offset = Vec3d(it.offset) * { random.nextGaussian() }
                            val velocity = Vec3d(it.speed) * { random.nextGaussian() }

                            spawn(it.position + offset, velocity)
                        }
                    }
                }
            },
        )

        connection.register(*invokers.toTypedArray())
    }
}
