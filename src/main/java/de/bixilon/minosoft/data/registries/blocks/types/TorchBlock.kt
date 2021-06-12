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

package de.bixilon.minosoft.data.registries.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing.FlameParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

open class TorchBlock(resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) : Block(resourceLocation, registries, data) {
    protected val smokeParticle = registries.particleTypeRegistry[SmokeParticle]
    protected val flameParticle = registries.particleTypeRegistry[data["flame_particle"] ?: FlameParticle]


    private fun spawnSmokeParticles(connection: PlayConnection, blockPosition: Vec3i) {
        val particlePosition = Vec3d(0.5, 0.7, 0.5) + blockPosition
        smokeParticle?.let { connection.world += SmokeParticle(connection, Vec3d(particlePosition), Vec3d.EMPTY) }
        flameParticle?.let { connection.world += it.factory?.build(connection, Vec3d(particlePosition), Vec3d.EMPTY) }
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        spawnSmokeParticles(connection, blockPosition)
    }

}
