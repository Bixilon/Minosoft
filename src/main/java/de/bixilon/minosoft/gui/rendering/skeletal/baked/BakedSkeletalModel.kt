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

package de.bixilon.minosoft.gui.rendering.skeletal.baked

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.unbaked.ModelBakeUtil
import de.bixilon.minosoft.gui.rendering.models.unbaked.element.UnbakedElement
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class BakedSkeletalModel(
    val model: SkeletalModel,
    val textures: Int2ObjectOpenHashMap<AbstractTexture>,
) {
    lateinit var mesh: SkeletalMesh

    fun loadMesh(renderWindow: RenderWindow) {
        val mesh = SkeletalMesh(renderWindow, 1000)

        for (element in model.elements) {
            for ((direction, face) in element.faces) {
                val positions = direction.getPositions(element.from.fromBlockCoordinates(), element.to.fromBlockCoordinates())

                val uvDivider = Vec2(model.resolution.width, model.resolution.height)
                val texturePositions = ModelBakeUtil.getTextureCoordinates(face.uvStart / uvDivider, face.uvEnd / uvDivider)

                val origin = element.origin.fromBlockCoordinates()

                element.rotation.let {
                    val rad = -glm.radians(it)
                    for ((index, position) in positions.withIndex()) {
                        val out = Vec3(position)
                        out.rotateAssign(rad[0], Axes.X, origin, element.rescale)
                        out.rotateAssign(rad[1], Axes.Y, origin, element.rescale)
                        out.rotateAssign(rad[2], Axes.Z, origin, element.rescale)
                        positions[index] = out
                    }
                }

                var tintColor = 0xFFFFFF
                if (element.color > 0) {
                    tintColor = element.color
                }
                for ((index, textureIndex) in mesh.order) {
                    val indexPosition = positions[index].array
                    mesh.addVertex(indexPosition, texturePositions[textureIndex], 0, textures[face.texture]!!, tintColor, 0xFF)
                }
            }
        }
        mesh.load()
        this.mesh = mesh
    }

    fun Vec3.fromBlockCoordinates(): Vec3 {
        return (this / UnbakedElement.BLOCK_RESOLUTION) + 0.5f
    }
}
