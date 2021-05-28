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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.minosoft.gui.rendering.particle.types.norender.ExplosionEmitterParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.ExplosionLargeParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.times
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.modding.event.events.ParticleSpawnEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3

object DefaultParticleBehavior {

    fun register(connection: PlayConnection, particleRenderer: ParticleRenderer) {
        val random = java.util.Random()
        val explosionParticleType = connection.registries.particleTypeRegistry[ExplosionLargeParticle]!!
        val explosionEmitterParticleType = connection.registries.particleTypeRegistry[ExplosionEmitterParticle]!!
        val invokers = listOf(
            CallbackEventInvoker.of<ExplosionEvent> {
                if (it.power >= 2.0f) {
                    particleRenderer.add(ExplosionEmitterParticle(connection, particleRenderer, it.position, explosionEmitterParticleType.simple()))
                } else {
                    particleRenderer.add(ExplosionLargeParticle(connection, particleRenderer, it.position, explosionParticleType.simple()))
                }
            },
            CallbackEventInvoker.of<ParticleSpawnEvent> {
                fun spawn(position: Vec3, velocity: Vec3) {
                    val particle = it.data.type.factory?.build(connection, particleRenderer, position, velocity, it.data) ?: let { _ ->
                        Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Can not spawn particle: ${it.data.type}" }
                        return
                    }
                    particleRenderer.add(particle)
                }
                // ToDo: long distance = always spawn?
                if (it.count == 0) {
                    val velocity = it.offset * it.speed

                    spawn(it.position, velocity)
                } else {
                    for (i in 0 until it.count) {
                        val offset = it.offset * { random.nextGaussian().toFloat() }
                        val velocity = Vec3(it.speed) * { random.nextGaussian().toFloat() }

                        spawn(it.position + offset, velocity)
                    }
                }
            },
        )

        connection.registerEvents(*invokers.toTypedArray())
    }
}
