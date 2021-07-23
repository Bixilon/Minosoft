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

package de.bixilon.minosoft.gui.rendering.input.camera

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class EntityPositionInfo(
    val connection: PlayConnection,
    val entity: Entity,
) {
    lateinit var blockPosition: Vec3i
        private set
    lateinit var velocityPosition: Vec3i
        private set
    lateinit var chunkPosition: Vec2i
        private set
    lateinit var inChunkSectionPosition: Vec3i
        private set
    var sectionHeight: Int = 0
        private set
    var biome: Biome? = null
        private set

    init {
        update()
    }

    fun update() {
        blockPosition = entity.position.blockPosition
        velocityPosition = Vec3i(entity.position.x, entity.position.y + -0.5000001f, entity.position.z)
        chunkPosition = blockPosition.chunkPosition
        inChunkSectionPosition = blockPosition.inChunkSectionPosition
        sectionHeight = blockPosition.sectionHeight
        biome = connection.world.getBiome(blockPosition)
    }
}
