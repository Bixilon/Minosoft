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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.unbaked.element.UnbakedElement.Companion.BLOCK_RESOLUTION
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
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

    fun preload(context: RenderContext) {
        check(state == SkeletalModelStates.DECLARED) { "Can not preload model in $state" }
        val mesh = SkeletalMesh(context, 1000)

        val outlinerMapping = calculateOutlinerMapping()

        for (element in model.elements) {
            element.bake(model, textures, outlinerMapping, mesh)
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
            return Vec3(this.x.toBlockCoordinate(), this.y / BLOCK_RESOLUTION, this.z.toBlockCoordinate())
        }

        inline fun Float.toBlockCoordinate(): Float {
            return this / BLOCK_RESOLUTION + 0.5f
        }
    }
}
