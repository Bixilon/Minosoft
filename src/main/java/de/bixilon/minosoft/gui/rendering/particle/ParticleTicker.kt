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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.particle.mesh.ParticleMeshBuilder
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class ParticleTicker(val renderer: ParticleRenderer) {
    private val particles = renderer.particles
    private val context = renderer.context


    private fun canTick(): Boolean {
        if (context.session.state != PlaySessionStates.PLAYING) return false
        if (!renderer.enabled) return false
        if (!context.state.running) return false


        return true
    }

    private fun Particle.tick(viewDistance: Int, cameraPosition: ChunkPosition, millis: ValueTimeMark) {
        if (!chunkPosition.isInViewDistance(viewDistance, cameraPosition)) { // ToDo: Check fog distance
            dead = true
        }
        if (dead) return
        ignoreAll { tryTick(millis) }
    }

    fun tick(mesh: ParticleMeshBuilder?, transparent: ParticleMeshBuilder?, translucent: ParticleMeshBuilder?) {
        if (!canTick()) return

        val camera = context.session.camera.entity.physics.positionInfo
        val cameraPosition = camera.chunkPosition
        val viewDistance = context.session.world.view.particleViewDistance
        val start = now()
        var time = start


        particles.lock.lock()
        renderer.queue.addTo(particles.particles)

        val iterator = particles.particles.iterator()
        var index = 0
        for (particle in iterator) {
            particle.tick(viewDistance, cameraPosition, time)

            if (particle.dead) {
                iterator.remove()
                continue
            }
            if (mesh != null && transparent != null && translucent != null) {
                particle.addVertex(mesh, transparent, translucent, time)
            }
            if (index % 1000 == 0) { // don't spam the os with time calls
                time = now()
                if (time - start > MAX_TICK_TIME) {
                    break
                }
            }
            index++
        }
        renderer.queue.addTo(particles.particles)
        particles.lock.unlock()
    }

    fun init() {
        context.session.ticker += { tick(null, null, null) }
    }

    companion object {
        val MAX_TICK_TIME = 5.milliseconds
    }
}
