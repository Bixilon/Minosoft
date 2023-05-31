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

package de.bixilon.minosoft.data.world.positions

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

object ChunkPositionUtil {

    val BlockPosition.sectionHeight: SectionHeight
        get() = y.sectionHeight

    val BlockPosition.chunkPosition: ChunkPosition
        get() = ChunkPosition(x shr 4, z shr 4)


    val Vec3d.chunkPosition: ChunkPosition
        get() = ChunkPosition(x.toInt() shr 4, z.toInt() shr 4)

    val BlockPosition.inChunkPosition: InChunkPosition
        get() = Vec3i(x and 0x0F, y, this.z and 0x0F)

    val BlockPosition.inChunkSectionPosition: InChunkSectionPosition
        get() = Vec3i(x and 0x0F, y.inSectionHeight, z and 0x0F)
}
