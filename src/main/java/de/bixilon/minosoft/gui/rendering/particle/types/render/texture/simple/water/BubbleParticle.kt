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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.water

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.assign
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class BubbleParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {

    init {
        this.spacing = Vec3(0.02f)

        this.scale *= random.nextFloat() * 0.6f + 0.2f

        this.velocity assign (velocity * 0.2) + (Vec3d.of { random.nextDouble() * 2.0 - 1.0 } * 0.02)
        this.maxAge = (8.0f / random.nextFloat() * 0.8f + 0.2f).toInt()

        movement = false
    }

    override fun tick() {
        super.tick()
        if (dead) {
            return
        }
        this.velocity.y += 0.002
        forceMove(velocity)
        velocity *= 0.85

        // ToDo: Check if in water: Kill particle
    }


    companion object : ParticleFactory<BubbleParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:bubble".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): BubbleParticle {
            return BubbleParticle(connection, position, velocity, data)
        }
    }
}
