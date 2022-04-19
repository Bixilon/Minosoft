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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.emotion

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class AngryVillagerParticle(connection: PlayConnection, position: Vec3d, data: ParticleData? = null) : EmotionParticle(connection, position + Vec3d(0.0, 0.5, 0.0), data) {

    init {
        color = ChatColors.WHITE
    }

    companion object : ParticleFactory<AngryVillagerParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:angry_villager".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): AngryVillagerParticle {
            return AngryVillagerParticle(connection, position, data)
        }
    }
}
