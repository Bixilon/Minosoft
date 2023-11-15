/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entities.feature.item

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockMesh
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.models.item.ItemRenderUtil.getModel
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateXAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateZAssign

open class ItemFeature(
    renderer: EntityRenderer<*>,
    stack: ItemStack?,
    val display: DisplayPositions,
) : MeshedFeature<BlockMesh>(renderer) {
    private var matrix = Mat4()
    var stack: ItemStack? = stack
        set(value) {
            if (field == value) return
            field = value
            unload()
        }

    // TODO: observe stack

    override val layer get() = EntityLayer.Translucent // TODO

    override fun update(millis: Long, delta: Float) {
        if (!super.enabled) return unload()
        if (this.mesh == null) {
            val stack = this.stack ?: return unload()
            createMesh(stack)
        }
        updateMatrix()
    }

    private fun createMesh(stack: ItemStack) {
        val model = stack.item.item.getModel(renderer.renderer.connection) ?: return
        val mesh = BlockMesh(renderer.renderer.context)

        val tint = renderer.renderer.context.tints.getItemTint(stack)

        model.render(mesh, stack, tint)

        this.mesh = mesh
    }

    private fun updateMatrix() {
        this.matrix.reset()
        this.matrix
            .translateXAssign(-0.5f).translateZAssign(-0.5f)

        // TODO: rotate?

        this.matrix = renderer.matrix * matrix
    }

    override fun draw(mesh: BlockMesh) {
        renderer.renderer.context.system.reset(faceCulling = false)
        val shader = renderer.renderer.features.block.shader
        shader.matrix = matrix
        shader.tint = renderer.light.value
        super.draw(mesh)
    }
}
