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

package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

open class BrewingStandBlock(resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) : Block(resourceLocation, registries, data) {
    private val smokeParticle = registries.particleTypeRegistry[SmokeParticle]

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)

        smokeParticle?.let {
            connection.world += SmokeParticle(
                connection,
                blockPosition.toVec3d + Vec3d(0.4, 0.7, 0.4) + Vec3d.horizontal({ random.nextDouble() * 0.2 }, random.nextDouble() * 0.3),
                Vec3d.EMPTY,
                it.default(),
            )
        }
    }
}

