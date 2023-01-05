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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.suspend

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class HappyVillagerParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : SuspendParticle(connection, position, velocity, data) {

    init {
        color = ChatColors.WHITE
    }

    companion object : ParticleFactory<HappyVillagerParticle> {
        override val identifier: ResourceLocation = "minecraft:happy_villager".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): HappyVillagerParticle {
            return HappyVillagerParticle(connection, position, velocity, data)
        }
    }
}
