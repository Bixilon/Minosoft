/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.camera.visibility

import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumCulling
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition

class WorldVisibility(
    val camera: Camera,
) {

    fun isInViewDistance(position: ChunkPosition): Boolean {
        return position.isInViewDistance(camera.context.session.world.view.viewDistance, camera.view.view.eyePosition.blockPosition.chunkPosition)
    }

    fun isChunkVisible(position: ChunkPosition): Boolean {
        if (!isInViewDistance(position)) return false
        if (position !in camera.frustum) return false

        val dimension = camera.context.session.world.dimension

        for (height in dimension.minSection..dimension.maxSection) {
            if (camera.occlusion.isSectionOccluded(SectionPosition.of(position, height))) continue
            return true
        }

        return false
    }

    fun isChunkVisible(chunk: Chunk): Boolean {
        if (!isInViewDistance(chunk.position)) return false
        if (chunk.position !in camera.frustum) return false


        for (height in chunk.sections.lowest..chunk.sections.highest) {
            if (camera.occlusion.isSectionOccluded(SectionPosition.of(chunk.position, height))) continue
            return true
        }

        return false
    }

    fun isSectionVisible(section: ChunkSection): Boolean = isSectionVisible(SectionPosition.of(section), section.blocks.minPosition, section.blocks.maxPosition)

    fun isSectionVisible(position: SectionPosition, min: InSectionPosition = FrustumCulling.SECTION_MIN_POSITION, max: InSectionPosition = FrustumCulling.SECTION_MIN_POSITION) = when {
        !isInViewDistance(position.chunkPosition) -> false
        camera.occlusion.isSectionOccluded(position) -> false
        !camera.frustum.containsChunkSection(position, min, max) -> false // TODO: Isn't that case done with occlusion?
        else -> true
    }
}
