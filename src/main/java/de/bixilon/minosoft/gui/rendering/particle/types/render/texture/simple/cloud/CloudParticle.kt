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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.cloud

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asGray
import de.bixilon.minosoft.data.world.WorldEntities
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.lang.Float.max

open class CloudParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, position, Vec3d.EMPTY, data) {

    init {
        friction = 0.96f
        this.velocity *= 0.1
        this.velocity += velocity

        this.color = (1.0f - random.nextFloat() * 0.3).asGray()

        super.scale *= 1.875f

        maxAge = max((8.0f / (random.nextFloat() * 0.8f + 0.3f)).toInt() * 2.5f, 1.0f).toInt()
        physics = false
    }

    override fun tick() {
        super.tick()
        if (dead) {
            return
        }
        connection.world.entities.getClosestInRadius(position, 2.0, WorldEntities.CHECK_CLOSEST_PLAYER)?.let {
            val y = it.position.y
            if (this.position.y <= y) {
                return@let
            }
            this.position.y += (y - this.position.y) * 0.2
            this.velocity.y += (it.velocity.y - this.velocity.y) * 0.2

        }
    }


    companion object : ParticleFactory<CloudParticle> {
        override val identifier: ResourceLocation = "minecraft:cloud".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): CloudParticle {
            return CloudParticle(connection, position, velocity, data)
        }
    }
}
