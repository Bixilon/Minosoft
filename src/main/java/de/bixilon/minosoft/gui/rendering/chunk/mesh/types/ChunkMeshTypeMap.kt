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

package de.bixilon.minosoft.gui.rendering.chunk.mesh.types

import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshBuilder

@JvmInline
value class ChunkMeshTypeMap(
    val data: Array<ChunkMesh?>,
) {
    val size get() = data.sumOf { if (it == null) 0 else 1 }

    constructor() : this(arrayOfNulls(ChunkMeshTypes.VALUES.size))

    inline fun forEach(consumer: (type: ChunkMeshTypes, mesh: ChunkMesh) -> Unit) {
        for ((index, mesh) in data.withIndex()) {
            if (mesh == null) continue
            consumer.invoke(ChunkMeshTypes[index], mesh)
        }
    }

    operator fun get(type: ChunkMeshTypes) = data[type.ordinal]


    private fun ChunkMeshBuilder.takeIfNotEmpty(): ChunkMeshBuilder? {
        val data = _data ?: return null
        if (data.isEmpty) {
            drop(true)
            return null
        }

        return this
    }

    operator fun set(type: ChunkMeshTypes, builder: ChunkMeshBuilder?) {
        if (builder == null) return
        data[type.ordinal] = builder.takeIfNotEmpty()?.bake()
    }
}
