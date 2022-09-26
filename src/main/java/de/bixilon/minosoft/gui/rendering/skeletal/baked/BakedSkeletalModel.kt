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

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.unbaked.ModelBakeUtil
import de.bixilon.minosoft.gui.rendering.models.unbaked.element.UnbakedElement.Companion.BLOCK_RESOLUTION
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.*

class BakedSkeletalModel(
    val model: SkeletalModel,
    val textures: Int2ObjectOpenHashMap<ShaderTexture>,
) {
    lateinit var mesh: SkeletalMesh
    var state: SkeletalModelStates = SkeletalModelStates.DECLARED
        private set

    private fun calculateOutlinerMapping(): Map<UUID, Int> {
        val mapping: Object2IntOpenHashMap<UUID> = Object2IntOpenHashMap()
        var offset = 0

        fun addOutliner(child: Any, parentId: Int) {
            if (child is UUID) {
                mapping[child] = parentId
                return
            }
            if (child !is SkeletalOutliner) {
                throw IllegalArgumentException()
            }
            if (mapping.containsKey(child.uuid)) {
                return
            }
            val id = offset++
            mapping[child.uuid] = id

            for (childChild in child.children) {
                addOutliner(childChild, id)
            }
        }

        for (outliner in this.model.outliner) {
            addOutliner(outliner, -1)
        }

        return mapping
    }

    fun preload(renderWindow: RenderWindow) {
        check(state == SkeletalModelStates.DECLARED) { "Can not preload model in $state" }
        val mesh = SkeletalMesh(renderWindow, 1000)

        val outlinerMapping = calculateOutlinerMapping()


        for (element in model.elements) {
            if (!element.visible) {
                continue
            }
            val inflate = (element.inflate / BLOCK_RESOLUTION) / 2
            for ((direction, face) in element.faces) {
                val positions = direction.getPositions(element.from.fromBlockCoordinates() - inflate, element.to.fromBlockCoordinates() + inflate)

                val uvDivider = Vec2(model.resolution.width, model.resolution.height)
                val texturePositions = ModelBakeUtil.getTextureCoordinates(face.uvStart / uvDivider, face.uvEnd / uvDivider)

                val origin = element.origin.fromBlockCoordinates()

                element.rotation.let {
                    val rad = -GLM.radians(it)
                    for ((index, position) in positions.withIndex()) {
                        val out = Vec3(position)
                        out.rotateAssign(rad[0], Axes.X, origin, element.rescale)
                        out.rotateAssign(rad[1], Axes.Y, origin, element.rescale)
                        out.rotateAssign(rad[2], Axes.Z, origin, element.rescale)
                        positions[index] = out
                    }
                }
                val outlinerId = outlinerMapping[element.uuid] ?: 0

                var flags = 0
                if (element.transparency && face.transparency) {
                    flags = flags or 0x01
                }

                val texture = textures[face.texture]!!
                val transform = Float.fromBits(outlinerId)
                val textureShaderId = Float.fromBits(texture.shaderId)
                val floatFlags = Float.fromBits(flags)
                for ((index, textureIndex) in mesh.order) {
                    val indexPosition = positions[index].array
                    val transformedUV = texture.transformUV(texturePositions[textureIndex])
                    mesh.addVertex(indexPosition, transformedUV, transform, textureShaderId, floatFlags)
                }
            }
        }
        this.mesh = mesh
        state = SkeletalModelStates.PRE_LOADED
    }

    fun load() {
        check(state == SkeletalModelStates.PRE_LOADED) { "Can not load model in state: $state" }
        mesh.load()
        state = SkeletalModelStates.LOADED
    }

    fun unload() {
        check(state == SkeletalModelStates.LOADED) { "Can not unload model in state $state" }
        mesh.unload()
        state = SkeletalModelStates.UNLOADED
    }

    companion object {

        fun Vec3.fromBlockCoordinates(): Vec3 {
            return Vec3(this.x / BLOCK_RESOLUTION + 0.5f, this.y / BLOCK_RESOLUTION, this.z / BLOCK_RESOLUTION + 0.5f)
        }
    }
}
