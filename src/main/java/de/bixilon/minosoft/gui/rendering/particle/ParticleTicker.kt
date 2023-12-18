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

import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ParticleTicker(val renderer: ParticleRenderer) {
    private val particles = renderer.particles
    private val context = renderer.context
    private var task: RepeatedTask? = null


    private fun canTick(): Boolean {
        if (context.connection.state != PlayConnectionStates.PLAYING) return false
        if (!renderer.enabled) return false
        if (!context.state.running) return false


        return true
    }

    private fun Particle.tick(viewDistance: Int, cameraPosition: ChunkPosition, millis: Long) {
        if (!chunkPosition.isInViewDistance(viewDistance, cameraPosition)) { // ToDo: Check fog distance
            dead = true
        }
        if (dead) return
        ignoreAll { tryTick(millis) }
    }

    fun tick(collect: Boolean) {
        if (!canTick()) return

        val camera = context.connection.camera.entity.physics.positionInfo
        val cameraPosition = camera.chunkPosition
        val viewDistance = context.connection.world.view.particleViewDistance
        val start = millis()
        var time = start


        particles.lock.lock()
        renderer.queue.add(particles.particles)

        val iterator = particles.particles.iterator()
        var index = 0
        for (particle in iterator) {
            particle.tick(viewDistance, cameraPosition, time)

            if (particle.dead) {
                iterator.remove()
                continue
            }
            if (collect) {
                particle.addVertex(renderer.mesh, renderer.translucentMesh, time)
            }
            if (index % 1000 == 0) {
                // check periodically if time is exceeded
                time = millis()
                if (time - start > MAX_TICK_TIME) {
                    break
                }
            }
            index++
        }
        renderer.queue.add(particles.particles)
        particles.lock.unlock()
    }

    private fun unregister() {
        val task = this.task ?: return
        TaskScheduler -= task
        this.task = null
    }

    private fun register() {
        if (this.task != null) unregister()
        val task = RepeatedTask(ProtocolDefinition.TICK_TIME, maxDelay = ProtocolDefinition.TICK_TIME / 2) { tick(false) }
        this.task = task
        TaskScheduler += task
    }


    fun init() {
        context.connection::state.observe(this) {
            unregister()
            if (it == PlayConnectionStates.PLAYING) {
                register()
            }
        }
    }

    companion object {
        const val MAX_TICK_TIME = 5
    }
}
