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

package de.bixilon.minosoft.gui.rendering.world.preparer

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.baked.block.GreedyBakedBlockModel
import de.bixilon.minosoft.gui.rendering.world.mesh.SingleWorldMesh
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.SECTION_SIZE
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.util.*


@Deprecated("TODO")
class GreedySectionPreparer(
    val renderWindow: RenderWindow,
) /*: SolidSectionPreparer*/ {

    private fun renderNormal(block: BlockState, directions: Directions?, position: Vec3i, section: ChunkSection, mesh: SingleWorldMesh, random: Random) {
        val neighbour = section.blocks[ChunkSection.getIndex(position.x, position.y, position.z)]
    }


    // base taken from https://0fps.net/2012/06/30/meshing-in-a-minecraft-game/
    @Deprecated("TODO")
    fun prepareSolid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>): WorldMesh {
        val mesh = SingleWorldMesh(renderWindow, 20000)

        val random = Random(0L)

        var currentBlock: BlockState?
        var compareBlock: BlockState?
        var start: Vec3i
        var end: Vec3i

        var i: Int
        var j: Int
        var k: Int
        var l: Int
        var w: Int
        var h: Int
        val stateMask: Array<BlockState?> = arrayOfNulls(SECTION_SIZE * SECTION_SIZE)
        val meshableMask = BooleanArray(SECTION_SIZE * SECTION_SIZE) { true }
        val endOffset = IntArray(3)

        for (direction in Directions.VALUES) {
            // Sweep over each direction
            val negative = direction.negative
            val axis = direction.axis.ordinal
            val nextAxis = (axis + 1) % 3
            val nextNextAxis = (axis + 2) % 3
            val position = IntArray(3)
            val checkOffset = IntArray(3)

            checkOffset[axis] = 1

            val offsetCheck = negative.decide(-1, 1)

            // Check each slice of the chunk one at a time

            position[axis] = -1
            while (position[axis] < SECTION_SIZE) {

                // Compute the mask
                var n = 0
                position[nextNextAxis] = 0
                while (position[nextNextAxis] < SECTION_SIZE) {
                    position[nextAxis] = 0
                    while (position[nextAxis] < SECTION_SIZE) {
                        if ((offsetCheck == 1 && position[axis] < 0) || (offsetCheck == -1 && position[axis] > SECTION_SIZE)) {
                            ++position[nextAxis]
                            n++
                            continue
                        }
                        currentBlock = if (position[axis] >= 0) section.blocks[ChunkSection.getIndex(position[0], position[1], position[2])] else null
                        compareBlock = if (position[axis] < SECTION_SIZE - 1) section.blocks[ChunkSection.getIndex(position[0] + checkOffset[0], position[1] + checkOffset[1], position[2] + checkOffset[2])] else null

                        // The mask is set to true if there is a visible face between those two blocks
                        val primaryBlock = if (negative) {
                            compareBlock
                        } else {
                            currentBlock
                        }
                        val model = primaryBlock?.blockModel

                        val meshable = model is GreedyBakedBlockModel
                                && model.canGreedyMesh
                                && model.greedyMeshableFaces[direction.ordinal]


                        val face = currentBlock == null
                                || compareBlock == null
                                || currentBlock != compareBlock
                                || !meshable

                        if (!meshable) {
                            meshableMask[n] = false
                        }

                        if (face) {
                            stateMask[n] = primaryBlock
                        }
                        n++

                        ++position[nextAxis]
                    }
                    ++position[nextNextAxis]
                }
                ++position[axis]
                n = 0

                // Generate a mesh from the mask using lexicographic ordering,
                // by looping over each block in this slice of the chunk
                j = 0
                while (j < SECTION_SIZE) {
                    i = 0
                    while (i < SECTION_SIZE) {
                        if (stateMask[n] != null) {
                            // Compute the width of this quad and store it in w
                            // This is done by searching along the current axis until mask[n + w] is false
                            w = 1
                            while (i + w < SECTION_SIZE && stateMask[n + w] == stateMask[n]) {
                                w++
                            }


                            // Compute the height of this quad and store it in h
                            // This is done by checking if every block next to this row (range 0 to w) is also part of the mask.
                            // For example, if w is 5 we currently have a quad of dimensions 1 x 5. To reduce triangle count,
                            // greedy meshing will attempt to expand this quad out to CHUNK_SIZE x 5, but will stop if it reaches a hole in the mask
                            var done = false

                            h = 1
                            while (j + h < SECTION_SIZE) {
                                k = 0
                                while (k < w) {
                                    val compareIndex = n + k + h * SECTION_SIZE
                                    if (stateMask[compareIndex] != stateMask[n] || !meshableMask[compareIndex]) {
                                        done = true
                                    }
                                    k++
                                }
                                if (done) {
                                    break
                                }
                                h++
                            }

                            position[nextAxis] = i
                            position[nextNextAxis] = j

                            // du and dv determine the size and orientation of this face
                            val du = IntArray(3)
                            du[nextAxis] = w
                            val dv = IntArray(3)
                            dv[nextNextAxis] = h

                            endOffset[0] = du[0] + dv[0]
                            endOffset[1] = du[1] + dv[1]
                            endOffset[2] = du[2] + dv[2]


                            if (!negative) {
                                position[axis] -= offsetCheck
                            }

                            start = Vec3i(position)

                            currentBlock = section.blocks[ChunkSection.getIndex(position[0], position[1], position[2])]!!

                            if (endOffset[0] == 0 && endOffset[1] == 0 && endOffset[2] == 0) {
                                // single render
                                renderNormal(currentBlock, direction, start, section, mesh, random)
                            } else {
                                endOffset[0] += position[0]
                                endOffset[1] += position[1]
                                endOffset[2] += position[2]

                                end = Vec3i(endOffset)

                                val model = currentBlock.blockModel
                                model as GreedyBakedBlockModel


                                model.greedyRender(start, end, direction, mesh, 0xFF)
                            }


                            if (!negative) {
                                position[axis] += offsetCheck
                            }

                            // Clear this part of the mask, so we don't add duplicate faces
                            l = 0
                            while (l < h) {
                                k = 0
                                while (k < w) {
                                    val index = n + k + l * SECTION_SIZE
                                    stateMask[index] = null
                                    meshableMask[index] = true
                                    ++k
                                }
                                ++l
                            }

                            // Increment counters and continue
                            i += w
                            n += w
                        } else {
                            i++
                            n++
                        }
                    }
                    ++j
                }
            }
        }

        TODO()
    }
}
