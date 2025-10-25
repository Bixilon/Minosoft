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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

class CloudArray(
    val layer: CloudLayer,
    val offset: Vec2i,
) {
    private val mesh = build()

    init {
        mesh.load()
    }

    private fun build(): Mesh {
        val offset = layer.clouds.context.camera.offset.offset
        val matrix = layer.clouds.matrix
        val matrixOffset = (offset * ARRAY_SIZE) and 0xFF

        val mesh = CloudMeshBuilder(layer.clouds.context)

        for (z in 0 until ARRAY_SIZE) {
            for (x in 0 until ARRAY_SIZE) {
                val matrixX = matrixOffset.x + x
                val matrixZ = matrixOffset.y + z

                if (!matrix[matrixX, matrixZ]) {
                    continue
                }

                val start = (this.offset * ARRAY_SIZE + Vec2i(x, z) + (layer.index * ARRAY_SIZE)) * CLOUD_SIZE

                val cull = booleanArrayOf(
                    matrix[matrixX + 0, matrixZ - 1], // NORTH
                    matrix[matrixX + 0, matrixZ + 1], // SOUTH
                    matrix[matrixX - 1, matrixZ + 0], // WEST
                    matrix[matrixX + 1, matrixZ + 0], // EAST
                )
                mesh.createCloud(start, start + CLOUD_SIZE, offset, layer.height.first, layer.height.last, layer.clouds.flat, cull)
            }
        }

        return mesh.bake()
    }


    fun draw() {
        mesh.draw()
    }

    fun unload() { // TODO????
    }

    companion object {
        const val CLOUD_SIZE = 16
        const val ARRAY_SIZE = 16
    }
}
