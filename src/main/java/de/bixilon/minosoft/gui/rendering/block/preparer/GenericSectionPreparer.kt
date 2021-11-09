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

package de.bixilon.minosoft.gui.rendering.block.preparer

import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType


class GenericSectionPreparer(
    val renderWindow: RenderWindow,
    private val preparer: AbstractSectionPreparer = CullSectionPreparer(renderWindow),
) : AbstractSectionPreparer {

    override fun prepare(section: ChunkSection): ChunkSectionMesh {
        val startTime = System.nanoTime()

        val mesh = preparer.prepare(section)

        val time = System.nanoTime()
        val delta = time - startTime
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Preparing took ${delta}ns, ${delta / 1000}µs, ${delta / 1000_000}ms" }

        return mesh
    }
}
