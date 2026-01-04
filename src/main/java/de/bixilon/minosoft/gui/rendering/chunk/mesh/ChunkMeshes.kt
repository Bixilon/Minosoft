/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.kutil.enums.inline.IntInlineSet
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumResults
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.types.ChunkMeshTypeMap

class ChunkMeshes(
    val section: ChunkSection,
    val position: SectionPosition,
    val min: InSectionPosition,
    val max: InSectionPosition,

    val details: IntInlineSet,

    val meshes: ChunkMeshTypeMap,
    val entities: Array<BlockEntityRenderer>?,
) {
    val center = BlockPosition.of(position, InSectionPosition(8, 8, 8))

    var delta = BlockPosition()
    var result = FrustumResults.FULLY_INSIDE

    var distance = 0
    var sort = 0

    fun load() {
        meshes.forEach { _, mesh -> mesh.load() }
        entities?.forEach { it.load() }
    }

    fun unload() {
        meshes.forEach { _, mesh -> mesh.unload() }
    }

    fun drop() {
        meshes.forEach { _, mesh -> mesh.drop() }
    }
}
