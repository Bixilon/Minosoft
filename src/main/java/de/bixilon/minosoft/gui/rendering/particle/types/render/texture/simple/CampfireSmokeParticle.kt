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
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.assign
import de.bixilon.minosoft.gui.rendering.util.VecUtil.millis
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.millis
import glm_.vec3.Vec3

class CampfireSmokeParticle(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, velocity: Vec3, data: ParticleData, signal: Boolean) : SimpleTextureParticle(connection, particleRenderer, position, Vec3.EMPTY, data) {

    init {
        scale *= 3.0f
        spacing = Vec3(0.25f)
        maxAge = random.nextInt(50).millis
        if (signal) {
            maxAge += 280.millis
            color = color.with(alpha = 0.95f)
        } else {
            maxAge += 80.millis
            color = color.with(alpha = 0.90f)
        }

        gravityStrength = 3.0E-6f

        this.velocity assign Vec3(velocity.x, velocity.y + (random.nextFloat() / 500.0f), velocity.z)
        movement = false
        spriteDisabled = true
        setRandomSprite()
    }

    override fun realTick() {
        super.realTick()
        val horizontal = { (random.nextFloat() / 5000.0f * (if (random.nextBoolean()) 1.0f else -1.0f)) }
        velocity.x += horizontal()
        velocity.y -= gravityStrength
        velocity.z += horizontal()

        if (age >= maxAge - 60.millis) {
            color = color.with(alpha = color.floatAlpha - 0.015f)
        }
        if (color.alpha == 0) {
            dead = true
        }
    }

    override fun postTick(deltaTime: Int) {
        super.postTick(deltaTime)
        move(velocity.millis * (deltaTime / 1000.0f))
    }


    object CosySmokeParticleFactory : ParticleFactory<CampfireSmokeParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:campfire_cosy_smoke".asResourceLocation()

        override fun build(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, velocity: Vec3, data: ParticleData): CampfireSmokeParticle {
            return CampfireSmokeParticle(connection, particleRenderer, position, velocity, data, false)
        }
    }


    object SignalSmokeParticleFactory : ParticleFactory<CampfireSmokeParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:campfire_signal_smoke".asResourceLocation()

        override fun build(connection: PlayConnection, particleRenderer: ParticleRenderer, position: Vec3, velocity: Vec3, data: ParticleData): CampfireSmokeParticle {
            return CampfireSmokeParticle(connection, particleRenderer, position, velocity, data, true)
        }
    }
}
