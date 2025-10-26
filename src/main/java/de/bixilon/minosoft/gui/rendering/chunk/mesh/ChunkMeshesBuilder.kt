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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture

class ChunkMeshesBuilder(
    context: RenderContext,
    count: Int,
    entities: Int,
) : BlockVertexConsumer { // TODO: Don't inherit
    var opaque = ChunkMeshBuilder(context, count.opaqueCount()) // TODO: * floatsPerVertex * vertex per side
    var translucent = ChunkMeshBuilder(context, count.translucentCount())
    var text = ChunkMeshBuilder(context, 128)
    var entities: ArrayList<BlockEntityRenderer<*>> = ArrayList(entities)

    // used for frustum culling
    var minPosition = InSectionPosition(ChunkSize.SECTION_MAX_X, ChunkSize.SECTION_MAX_Y, ChunkSize.SECTION_MAX_Z)
    var maxPosition = InSectionPosition(0, 0, 0)


    fun addBlock(x: Int, y: Int, z: Int) {
        if (x < minPosition.x) {
            minPosition = minPosition.with(x = x)
        }
        if (y < minPosition.y) {
            minPosition = minPosition.with(y = y)
        }
        if (z < minPosition.z) {
            minPosition = minPosition.with(z = z)
        }

        if (x > maxPosition.x) {
            maxPosition = maxPosition.with(x = x)
        }
        if (y > maxPosition.y) {
            maxPosition = maxPosition.with(y = y)
        }
        if (z > maxPosition.z) {
            maxPosition = maxPosition.with(z = z)
        }
    }

    private fun ChunkMeshBuilder.takeIfNotEmpty(): ChunkMeshBuilder? {
        val data = data
        if (data.isEmpty) {
            data.free()
            return null
        }

        return this
    }


    fun build(position: SectionPosition): ChunkMeshes? {
        val opaque = opaque.takeIfNotEmpty()?.bake()
        val translucent = translucent.takeIfNotEmpty()?.bake()
        val text = text.takeIfNotEmpty()?.bake()
        val entities = entities.takeIf { it.isNotEmpty() }?.toTypedArray()

        if (opaque == null && translucent == null && text == null && entities == null) {
            return null
        }
        return ChunkMeshes(position, minPosition, maxPosition, opaque, translucent, text, entities)
    }

    fun drop() {
        opaque.drop()
        translucent.drop()
        text.drop()
        entities.forEach { it.drop() }
    }

    override val order get() = Broken()
    override fun ensureSize(floats: Int) = Unit
    override fun addVertex(position: Vec3f, uv: Vec2f, texture: ShaderTexture, tintColor: RGBColor, lightIndex: Int) = Broken()
    override fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) = Broken()
    override fun addVertex(x: Float, y: Float, z: Float, uv: Float, textureId: Float, lightTint: Float) = Broken()

    override fun get(transparency: TextureTransparencies) = when (transparency) {
        TextureTransparencies.TRANSLUCENT -> translucent
        else -> opaque
    }

    companion object {
        private fun Int.opaqueCount() = when { // Rounded mean counts of faces in a normal world
            this <= 32 -> 32
            this <= 128 -> 300
            this <= 512 -> 600
            this <= 3584 -> 1024
            this <= 4064 -> 512
            else -> 280
        }

        private fun Int.translucentCount() = when {
            this <= 32 -> 32
            this <= 4064 -> 256
            else -> 32
        }
    }
}
