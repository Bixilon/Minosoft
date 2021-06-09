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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.assign
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import glm_.pow
import glm_.vec3.Vec3d

class PortalParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SimpleTextureParticle(connection, Vec3d(position), Vec3d.EMPTY, data) {
    private val startPosition = Vec3d(position)

    override var scale: Float
        get() = super.scale * (1.0f - (1.0f - (floatAge / maxAge)).pow(2))
        set(value) {
            super.scale = value
        }

    init {
        this.velocity assign velocity
        this.position assign position

        this.scale = 0.1f * (random.nextFloat() * 0.2f + 0.5f)

        (random.nextFloat() * 0.6f + 0.4f).let {
            this.color = RGBColor(it * 0.9f, it * 0.3f, it)
        }

        this.maxAge = (random.nextInt(10) + 40)
        movement = false
    }

    override fun move(velocity: Vec3d) {
        this.position += velocity
    }

    override fun realTick() {
        super.realTick()
        if (dead) {
            return
        }

        val lifeTime = floatAge / maxAge
        val velocityMultiplier = 1.0f - (-lifeTime + lifeTime * lifeTime * 2.0f)

        this.position assign (startPosition + velocity * velocityMultiplier)
        this.position.y += 1.0 - lifeTime
    }


    companion object : ParticleFactory<PortalParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:portal".asResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): PortalParticle {
            return PortalParticle(connection, position, velocity, data)
        }
    }
}
