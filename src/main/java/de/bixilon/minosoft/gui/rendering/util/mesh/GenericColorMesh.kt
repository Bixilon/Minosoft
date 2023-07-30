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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes

open class GenericColorMesh(context: RenderContext, primitiveType: PrimitiveTypes = context.system.quadType, initialCacheSize: Int = 1000) : Mesh(context, GenericColorMeshStruct, primitiveType, initialCacheSize) {

    fun addVertex(position: Vec3, color: RGBColor?) {
        data.add(position.array)
        data.add((color ?: ChatColors.WHITE).rgba.buffer())
    }

    fun addVertex(position: Vec3, color: Float) {
        data.add(position.array)
        data.add(color)
    }

    data class GenericColorMeshStruct(
        val position: Vec3,
        val color: RGBColor,
    ) {
        companion object : MeshStruct(GenericColorMeshStruct::class)
    }
}
