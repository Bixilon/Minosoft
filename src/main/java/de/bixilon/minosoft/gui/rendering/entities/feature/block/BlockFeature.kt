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

package de.bixilon.minosoft.gui.rendering.entities.feature.block

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.entities.feature.mesh.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import kotlin.time.Duration

open class BlockFeature(
    renderer: EntityRenderer<*>,
    state: BlockState?,
    val scale: Vec3f = DEFAULT_SCALE,
) : MeshedFeature<Mesh>(renderer) {
    private var matrix = MMat4f()
    var state: BlockState? = state
        set(value) {
            if (field == value) return
            field = value
            unload = true
        }

    override val layer get() = EntityLayer.Translucent // TODO

    override fun update(delta: Duration) {
        super.update(delta)
        if (this.mesh == null) {
            val state = this.state ?: return
            createMesh(state)
        }
        updateMatrix()
    }

    private fun createMesh(state: BlockState) {
        val mesh = BlockMeshBuilder(renderer.renderer.context)
        val model = (state.block.model ?: state.model) ?: return mesh.drop()
        // TODO: block entity support?

        val tint = renderer.renderer.context.tints.getBlockTint(state, BlockPosition(), null, null) // TODO

        model.render(mesh, state, tint, null, null)

        this.mesh = mesh.bake()
    }

    private fun updateMatrix() {
        this.matrix.clearAssign()
        this.matrix.apply {
            scaleAssign(scale)
            translateXAssign(-0.5f); translateZAssign(-0.5f)
        }

        // TODO: rotate?

        this.matrix = renderer.matrix * matrix
    }

    override fun draw(mesh: Mesh) {
        renderer.renderer.context.system.set(layer.settings)
        val shader = renderer.renderer.features.block.shader
        draw(mesh, shader)
    }

    protected open fun draw(mesh: Mesh, shader: BlockShader) {
        shader.use()
        shader.matrix = matrix.unsafe
        shader.tint = renderer.light.value
        super.draw(mesh)
    }

    companion object {
        val DEFAULT_SCALE = Vec3f(0.99f)
    }
}
