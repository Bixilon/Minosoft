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
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockMesh
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockShader
import de.bixilon.minosoft.gui.rendering.entities.feature.item.ItemFeature.ItemRenderDistance.Companion.getCount
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.models.item.ItemRenderUtil.getModel
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.EMPTY_INSTANCE
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateXAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateZAssign
import java.util.*

open class ItemFeature(
    renderer: EntityRenderer<*>,
    stack: ItemStack?,
    val display: DisplayPositions,
    val many: Boolean = true,
) : MeshedFeature<BlockMesh>(renderer) {
    private var matrix = Mat4()
    private var displayMatrix: Mat4 = Mat4.EMPTY_INSTANCE
    private var distance: ItemRenderDistance? = null
    var stack: ItemStack? = stack
        set(value) {
            if (field == value) return
            field = value
            unload()
        }

    // TODO: observe stack

    override val layer get() = EntityLayer.Translucent // TODO

    override fun update(millis: Long, delta: Float) {
        if (!_enabled) return unload()
        updateDistance()
        if (this.mesh == null) {
            val stack = this.stack ?: return unload()
            createMesh(stack)
        }
        updateMatrix()
    }

    private fun updateDistance() {
        val distance = ItemRenderDistance.of(renderer.distance)
        if (distance == this.distance) return
        unload()
        this.distance = distance
    }

    private fun createMesh(stack: ItemStack) {
        val distance = this.distance ?: return
        val model = stack.item.item.getModel(renderer.renderer.connection) ?: return
        val display = model.getDisplay(display)
        this.displayMatrix = display?.matrix ?: Mat4.EMPTY_INSTANCE
        val mesh = BlockMesh(renderer.renderer.context)

        val tint = renderer.renderer.context.tints.getItemTint(stack)

        val count = if (many) distance.getCount(stack.item.count) else 1
        val spread = maxOf(0.1f, count / 30.0f)

        model.render(mesh, stack, tint) // 0 without offset

        if (count > 1) {
            val random = Random(1234567890123456789L)
            for (i in 0 until count - 1) {
                mesh.offset.x = random.nextFloat(-spread, spread)
                mesh.offset.y = random.nextFloat(-spread, spread)
                mesh.offset.z = random.nextFloat(-spread, spread)

                model.render(mesh, stack, tint)
            }
        }
        // TODO: enchantment glint, ...

        this.mesh = mesh
    }

    private fun updateMatrix() {
        this.matrix.reset()
        this.matrix
            .translateXAssign(-0.5f)
            .translateZAssign(-0.5f)


        this.matrix = renderer.matrix * displayMatrix * matrix
    }

    override fun draw(mesh: BlockMesh) {
        renderer.renderer.context.system.set(layer.settings)
        val shader = renderer.renderer.features.block.shader
        draw(mesh, shader)
    }


    protected open fun draw(mesh: BlockMesh, shader: BlockShader) {
        shader.use()
        shader.matrix = matrix
        shader.tint = renderer.light.value
        super.draw(mesh)
    }

    override fun unload() {
        this.displayMatrix = Mat4.EMPTY_INSTANCE
        super.unload()
    }

    private enum class ItemRenderDistance(distance: Double) {
        CLOSE(10.0),
        MID(20.0),
        FAR(30.0),
        EXTREME(48.0),
        ;

        val distance = distance * distance

        companion object {

            fun of(distance: Double) = when {
                distance < CLOSE.distance -> CLOSE
                distance < MID.distance -> MID
                distance < FAR.distance -> FAR
                distance < EXTREME.distance -> EXTREME
                else -> null
            }

            fun ItemRenderDistance.getCount(count: Int) = when (this) {
                CLOSE -> when {
                    count <= 12 -> count
                    else -> 16
                }

                MID -> when {
                    count <= 4 -> count
                    count < 16 -> 5
                    count < 32 -> 6
                    count < 48 -> 7
                    else -> 8
                }

                FAR -> when {
                    count <= 2 -> count
                    count < 32 -> 3
                    else -> 4
                }

                EXTREME -> when {
                    count <= 1 -> count
                    count <= 32 -> 1
                    else -> 2
                }
            }
        }
    }
}
