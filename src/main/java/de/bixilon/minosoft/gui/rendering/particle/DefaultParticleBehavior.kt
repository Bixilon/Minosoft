/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.modding.event.events.ParticleSpawnEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object DefaultParticleBehavior {

    fun register(connection: PlayConnection, particleRenderer: ParticleRenderer) {
        val random = java.util.Random()
        val explosionParticleType = connection.registries.particleTypeRegistry[ExplosionParticle]!!
        val explosionEmitterParticleType = connection.registries.particleTypeRegistry[ExplosionEmitterParticle]!!
        val typesConfig = connection.profiles.particle.types
        val invokers = listOf(
            CallbackEventInvoker.of<ExplosionEvent> {
                if (typesConfig.explosions) {
                    return@of
                }
                if (it.power >= 2.0f) {
                    particleRenderer += ExplosionEmitterParticle(connection, Vec3d(it.position), explosionEmitterParticleType.default())
                } else {
                    particleRenderer += ExplosionParticle(connection, Vec3d(it.position), explosionParticleType.default())
                }
            },
            CallbackEventInvoker.of<ParticleSpawnEvent> {
                DefaultThreadPool += add@{
                    if (it.initiator == EventInitiators.SERVER && !typesConfig.packet) {
                        return@add
                    }
                    fun spawn(position: Vec3d, velocity: Vec3d) {
                        val particle = it.data.type.factory?.build(connection, position, velocity, it.data) ?: let { _ ->
                            Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Can not spawn particle: ${it.data.type}" }
                            return
                        }
                        particleRenderer += particle
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

        connection.registerEvents(*invokers.toTypedArray())
    }
}
